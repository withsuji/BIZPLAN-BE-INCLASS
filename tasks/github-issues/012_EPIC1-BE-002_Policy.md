---
issue_number: 012
epic: EPIC-1
task_id: EPIC1-BE-002
title: "[BE] 타임존 정규화 및 업무시간/공휴일 정책 엔진"
labels: ["backend", "calendar", "policy", "timezone", "priority:must", "size:L", "critical-path"]
assignees: []
milestone: "MVP-Phase-2-Backend-Complete"
dependencies: ["#010"]
parallel_group: "group-3-calendar"
estimated_effort: "L"
difficulty: "중"
estimated_duration: "5일"
---

# Issue #012: 타임존 정규화 및 업무시간/공휴일 정책 엔진

## 📋 개요
다양한 타임존의 사용자 간 일정 조율을 위한 타임존 정규화 로직과 업무시간, 공휴일, 개인 선호도를 반영한 정책 엔진을 구현한다.

## 🎯 목표
- IANA 타임존 기반 시간 정규화
- 사용자별 업무시간/휴식시간 관리
- 국가/개인별 공휴일 처리
- 일정 충돌 및 정책 위반 감지

---

## 📝 상세 요구사항

### 1. 타임존 정규화
- DB 저장: UTC 기준
- API 응답: 사용자 타임존으로 변환
- DST(서머타임) 자동 처리
- IANA Timezone DB 사용

### 2. 업무시간 정책
- 요일별 시작/종료 시간 설정
- 점심시간 등 휴식 시간 설정
- 미팅 선호 시간대
- 미팅 간 버퍼 시간

### 3. 공휴일 관리
- 국가별 공휴일 데이터
- 사용자 정의 휴일
- 공휴일 API 연동 (Calendarific)

### 4. 충돌 감지
- 시간 중복 검사
- 포커스 블록 보호
- 정책 위반 경고 (ERROR/WARNING/INFO)

---

## 🔧 구현 가이드

### 데이터 모델

```sql
CREATE TABLE user_policies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    timezone VARCHAR(64) NOT NULL DEFAULT 'UTC',
    working_hours JSON NOT NULL,
    break_times JSON,
    preferred_meeting_hours JSON,
    buffer_minutes INT DEFAULT 10,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE holidays (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    country_code VARCHAR(2) NOT NULL,
    date DATE NOT NULL,
    name VARCHAR(255) NOT NULL,
    is_public BOOLEAN DEFAULT TRUE,
    year INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_country_date (country_code, date)
);

CREATE TABLE user_holidays (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    date DATE NOT NULL,
    name VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    UNIQUE KEY uk_user_date (user_id, date)
);
```

### 핵심 서비스 구현

```java
@Service
@RequiredArgsConstructor
public class PolicyEngine {
    
    private final UserPolicyRepository policyRepository;
    private final HolidayService holidayService;
    private final CalendarEventRepository eventRepository;
    
    /**
     * 정책 검증 결과
     */
    public PolicyValidationResult validateMeetingTime(
            Long userId, 
            LocalDateTime start, 
            LocalDateTime end,
            String timezone) {
        
        UserPolicy policy = policyRepository.findByUserId(userId)
            .orElseGet(() -> UserPolicy.defaultPolicy(userId));
        
        List<PolicyViolation> violations = new ArrayList<>();
        
        // 1. 타임존 변환
        ZonedDateTime zonedStart = start.atZone(ZoneId.of("UTC"))
            .withZoneSameInstant(ZoneId.of(policy.getTimezone()));
        ZonedDateTime zonedEnd = end.atZone(ZoneId.of("UTC"))
            .withZoneSameInstant(ZoneId.of(policy.getTimezone()));
        
        // 2. 업무시간 검증
        if (!isWithinWorkingHours(policy, zonedStart, zonedEnd)) {
            violations.add(new PolicyViolation(
                ViolationType.OUTSIDE_WORKING_HOURS,
                Severity.WARNING,
                "Meeting is outside working hours"
            ));
        }
        
        // 3. 공휴일 검증
        if (holidayService.isHoliday(userId, zonedStart.toLocalDate())) {
            violations.add(new PolicyViolation(
                ViolationType.HOLIDAY,
                Severity.WARNING,
                "Meeting is on a holiday"
            ));
        }
        
        // 4. 기존 일정 충돌 검증
        List<CalendarEvent> conflicts = findConflicts(userId, start, end);
        if (!conflicts.isEmpty()) {
            violations.add(new PolicyViolation(
                ViolationType.TIME_CONFLICT,
                Severity.ERROR,
                "Conflicts with existing events: " + conflicts.size()
            ));
        }
        
        // 5. 포커스 블록 검증
        if (conflictsWithFocusBlock(userId, start, end)) {
            violations.add(new PolicyViolation(
                ViolationType.FOCUS_BLOCK_CONFLICT,
                Severity.ERROR,
                "Conflicts with focus block"
            ));
        }
        
        // 6. 버퍼 타임 검증
        if (!hasBufferTime(userId, start, end, policy.getBufferMinutes())) {
            violations.add(new PolicyViolation(
                ViolationType.INSUFFICIENT_BUFFER,
                Severity.INFO,
                "Insufficient buffer time between meetings"
            ));
        }
        
        return new PolicyValidationResult(violations);
    }
    
    private boolean isWithinWorkingHours(UserPolicy policy, ZonedDateTime start, ZonedDateTime end) {
        DayOfWeek dayOfWeek = start.getDayOfWeek();
        WorkingHours hours = policy.getWorkingHoursFor(dayOfWeek);
        
        if (hours == null || !hours.isEnabled()) {
            return false;
        }
        
        LocalTime meetingStart = start.toLocalTime();
        LocalTime meetingEnd = end.toLocalTime();
        
        return !meetingStart.isBefore(hours.getStart()) 
            && !meetingEnd.isAfter(hours.getEnd());
    }
}
```

---

## ✅ 완료 조건

| # | 조건 | 검증 방법 |
|---|------|----------|
| AC-1 | 타임존 변환 정확 | UTC → 사용자 TZ 변환 테스트 |
| AC-2 | 업무시간 검증 | 업무시간 외 미팅 시 WARNING |
| AC-3 | 공휴일 검증 | 공휴일 미팅 시 WARNING |
| AC-4 | 충돌 감지 | 중복 시간대 ERROR |
| AC-5 | 포커스 블록 보호 | 포커스 블록 중첩 시 ERROR |
| AC-6 | API 응답 시간 < 50ms | 정책 검증 성능 테스트 |

---

## 🔗 의존성

**선행 작업**: #010 (Calendar Sync)  
**후행 작업**: #016 (Slot Algorithm)  
**병렬 가능**: #013 (Focus Blocks), #014 (Time Tracking)

---

## 🏷️ 라벨
`backend`, `calendar`, `policy`, `timezone`, `priority:must`, `size:L`, `critical-path`

---

## 📚 참고 문서
- Task Spec: `tasks/functional/EPIC1-BE-002_Policy_Engine.md`
- REQ-IDs: REQ-FUNC-001, REQ-FUNC-002



