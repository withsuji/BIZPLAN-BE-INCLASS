---
issue_number: 016
epic: EPIC-1
task_id: EPIC1-BE-003
title: "[BE] 가용 슬롯 계산 알고리즘 및 최적 시간대 제안"
labels: ["backend", "calendar", "scheduling", "algorithm", "priority:must", "size:XL", "critical-path"]
assignees: []
milestone: "MVP-Phase-2-Backend-Complete"
dependencies: ["#012"]
parallel_group: "group-4-slot"
estimated_effort: "XL"
difficulty: "상"
estimated_duration: "8일"
---

# Issue #016: 가용 슬롯 계산 알고리즘 및 최적 시간대 제안

## 📋 개요
여러 참석자의 캘린더, 업무시간, 포커스 블록을 고려하여 미팅 가능한 시간대(슬롯)를 계산하고, 우선순위에 따라 최적의 슬롯을 제안하는 알고리즘을 구현한다.

## 🎯 목표
- 다중 참석자 가용 슬롯 계산
- 스코어링 기반 최적 슬롯 추천
- 대체 슬롯 제안 (제약 완화)
- 성능 최적화 (캐싱)

---

## 📝 상세 요구사항

### 1. 입력 파라미터
- 참석자 목록 (user_ids)
- 기간 (start_date, end_date)
- 미팅 시간 (duration_minutes)
- 타임존 (timezone)

### 2. 슬롯 스코어링

| 기준 | 점수 |
|------|------|
| 선호 시간대 매치 | +50 |
| 업무시간 중앙 | +30 |
| 버퍼 타임 확보 | +20 |
| 타임존 차이 큰 경우 | -20 |
| 금요일 오후 | -10 |
| 이른 아침/늦은 저녁 | -30 |

### 3. 제약 조건
- 최대 조회 범위: 3개월
- 최대 참석자: 10명
- 슬롯 단위: 15분
- 미팅 시간: 15분 ~ 4시간

---

## 🔧 구현 가이드

### 핵심 알고리즘

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class SlotCalculatorService {
    
    private final CalendarEventRepository eventRepository;
    private final FocusBlockRepository focusBlockRepository;
    private final PolicyEngine policyEngine;
    private final HolidayService holidayService;
    private final RedisTemplate<String, Object> redisTemplate;
    
    public List<AvailableSlot> calculateAvailableSlots(SlotSearchRequest request) {
        validateRequest(request);
        
        // 캐시 확인
        String cacheKey = buildCacheKey(request);
        List<AvailableSlot> cached = getCachedSlots(cacheKey);
        if (cached != null) {
            return cached;
        }
        
        List<AvailableSlot> slots = new ArrayList<>();
        LocalDate currentDate = request.getStartDate();
        
        while (!currentDate.isAfter(request.getEndDate())) {
            // 공휴일 체크
            if (isHolidayForAnyAttendee(request.getAttendeeIds(), currentDate)) {
                currentDate = currentDate.plusDays(1);
                continue;
            }
            
            // 해당 날짜의 슬롯 계산
            List<AvailableSlot> dailySlots = calculateDailySlots(
                request.getAttendeeIds(),
                currentDate,
                request.getDurationMinutes(),
                request.getTimezone()
            );
            
            slots.addAll(dailySlots);
            currentDate = currentDate.plusDays(1);
        }
        
        // 스코어링 및 정렬
        slots.forEach(slot -> slot.setScore(calculateScore(slot, request)));
        slots.sort(Comparator.comparingInt(AvailableSlot::getScore).reversed());
        
        // 상위 N개만 반환
        List<AvailableSlot> result = slots.stream()
            .limit(request.getMaxResults())
            .collect(Collectors.toList());
        
        // 캐시 저장
        cacheSlots(cacheKey, result);
        
        return result;
    }
    
    private List<AvailableSlot> calculateDailySlots(
            List<Long> attendeeIds,
            LocalDate date,
            int durationMinutes,
            String timezone) {
        
        // 1. 공통 업무시간 계산 (교집합)
        TimeRange commonWorkingHours = calculateCommonWorkingHours(attendeeIds, date);
        if (commonWorkingHours == null) {
            return List.of();
        }
        
        // 2. 모든 참석자의 이벤트 조회 (병렬)
        List<TimeRange> busyPeriods = attendeeIds.parallelStream()
            .flatMap(userId -> {
                List<CalendarEvent> events = eventRepository.findByUserAndDate(userId, date);
                List<FocusBlock> blocks = focusBlockRepository.findByUserAndDate(userId, date);
                
                return Stream.concat(
                    events.stream().map(e -> new TimeRange(e.getStartAt(), e.getEndAt())),
                    blocks.stream().map(b -> new TimeRange(b.getStartAt(), b.getEndAt()))
                );
            })
            .collect(Collectors.toList());
        
        // 3. Busy 시간대 병합
        List<TimeRange> mergedBusyPeriods = mergeBusyPeriods(busyPeriods);
        
        // 4. Free 시간대 계산
        List<TimeRange> freePeriods = calculateFreePeriods(commonWorkingHours, mergedBusyPeriods);
        
        // 5. 슬롯으로 분할
        List<AvailableSlot> slots = new ArrayList<>();
        for (TimeRange free : freePeriods) {
            slots.addAll(splitIntoSlots(free, durationMinutes, date, timezone));
        }
        
        return slots;
    }
    
    private int calculateScore(AvailableSlot slot, SlotSearchRequest request) {
        int score = 50; // 기본 점수
        
        LocalTime slotTime = slot.getStartTime().toLocalTime();
        
        // 선호 시간대 보너스
        if (isPreferredTime(request.getAttendeeIds(), slotTime)) {
            score += 50;
        }
        
        // 업무시간 중앙 보너스
        if (slotTime.isAfter(LocalTime.of(10, 0)) && slotTime.isBefore(LocalTime.of(16, 0))) {
            score += 30;
        }
        
        // 금요일 오후 페널티
        if (slot.getStartTime().getDayOfWeek() == DayOfWeek.FRIDAY
            && slotTime.isAfter(LocalTime.of(14, 0))) {
            score -= 10;
        }
        
        // 이른 아침/늦은 저녁 페널티
        if (slotTime.isBefore(LocalTime.of(9, 0)) || slotTime.isAfter(LocalTime.of(17, 0))) {
            score -= 30;
        }
        
        return score;
    }
    
    private List<TimeRange> mergeBusyPeriods(List<TimeRange> periods) {
        if (periods.isEmpty()) return List.of();
        
        periods.sort(Comparator.comparing(TimeRange::getStart));
        
        List<TimeRange> merged = new ArrayList<>();
        TimeRange current = periods.get(0);
        
        for (int i = 1; i < periods.size(); i++) {
            TimeRange next = periods.get(i);
            if (current.overlaps(next)) {
                current = current.merge(next);
            } else {
                merged.add(current);
                current = next;
            }
        }
        merged.add(current);
        
        return merged;
    }
    
    private List<AvailableSlot> splitIntoSlots(
            TimeRange free, 
            int durationMinutes, 
            LocalDate date,
            String timezone) {
        
        List<AvailableSlot> slots = new ArrayList<>();
        LocalDateTime current = free.getStart();
        
        while (current.plusMinutes(durationMinutes).isBefore(free.getEnd()) 
               || current.plusMinutes(durationMinutes).equals(free.getEnd())) {
            
            // 15분 단위 정렬
            int minute = current.getMinute();
            int alignedMinute = (minute / 15) * 15;
            LocalDateTime aligned = current.withMinute(alignedMinute).withSecond(0).withNano(0);
            
            if (aligned.isBefore(current)) {
                aligned = aligned.plusMinutes(15);
            }
            
            if (aligned.plusMinutes(durationMinutes).isAfter(free.getEnd())) {
                break;
            }
            
            slots.add(AvailableSlot.builder()
                .startTime(aligned)
                .endTime(aligned.plusMinutes(durationMinutes))
                .date(date)
                .timezone(timezone)
                .build());
            
            current = aligned.plusMinutes(15);
        }
        
        return slots;
    }
}
```

### API 엔드포인트

```java
@RestController
@RequestMapping("/api/v1/schedule")
@RequiredArgsConstructor
public class SlotController {
    
    private final SlotCalculatorService slotService;
    
    @GetMapping("/slots")
    public ResponseEntity<List<AvailableSlot>> getAvailableSlots(
            @RequestParam List<Long> attendeeIds,
            @RequestParam @DateTimeFormat(iso = DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DATE) LocalDate endDate,
            @RequestParam int durationMinutes,
            @RequestParam(defaultValue = "Asia/Seoul") String timezone,
            @RequestParam(defaultValue = "10") int maxResults) {
        
        SlotSearchRequest request = SlotSearchRequest.builder()
            .attendeeIds(attendeeIds)
            .startDate(startDate)
            .endDate(endDate)
            .durationMinutes(durationMinutes)
            .timezone(timezone)
            .maxResults(maxResults)
            .build();
        
        List<AvailableSlot> slots = slotService.calculateAvailableSlots(request);
        return ResponseEntity.ok(slots);
    }
}
```

---

## ✅ 완료 조건

| # | 조건 | 검증 방법 |
|---|------|----------|
| AC-1 | 슬롯 계산 정확도 100% | 거짓 양성 0% |
| AC-2 | 계산 시간 < 500ms | 5명, 2주 범위 기준 |
| AC-3 | 스코어링 동작 | 상위 슬롯이 선호 시간대 포함 |
| AC-4 | 캐싱 동작 | 반복 요청 시 50ms 이하 |
| AC-5 | 다중 참석자 지원 | 10명까지 처리 |
| AC-6 | 15분 단위 정렬 | 슬롯 시작 시간 00/15/30/45분 |

---

## 🔗 의존성

**선행 작업**: #012 (Policy Engine) 🚨 **Critical Path**  
**후행 작업**: 없음  
**병렬 가능**: #017 (Invoice)

---

## 🏷️ 라벨
`backend`, `calendar`, `scheduling`, `algorithm`, `priority:must`, `size:XL`, `critical-path`

---

## 📚 참고 문서
- Task Spec: `tasks/functional/EPIC1-BE-003_Slot_Algorithm.md`
- REQ-IDs: REQ-FUNC-003



