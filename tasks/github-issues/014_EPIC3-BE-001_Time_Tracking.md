---
issue_number: 014
epic: EPIC-3
task_id: EPIC3-BE-001
title: "[BE] 캘린더 이벤트 기반 자동 시간 기록"
labels: ["backend", "billing", "time-tracking", "priority:must", "size:XL"]
assignees: []
milestone: "MVP-Phase-2-Backend-Complete"
dependencies: ["#010"]
parallel_group: "group-3-billing"
estimated_effort: "XL"
difficulty: "중"
estimated_duration: "8일"
---

# Issue #014: 캘린더 이벤트 기반 자동 시간 기록

## 📋 개요
캘린더 이벤트를 기반으로 시간 기록(Time Entry)을 자동으로 생성하고, 프로젝트/클라이언트별로 시간을 추적하여 청구 가능한 데이터를 만든다.

## 🎯 목표
- 이벤트 종료 시 자동 시간 기록 생성
- 프로젝트/클라이언트 자동 매핑
- 청구 가능 여부(billable) 관리
- 시간 반올림 (15분 단위)

---

## 📝 상세 요구사항

### 1. 자동 생성 트리거
- 캘린더 이벤트 종료 시
- billable 태그가 있는 이벤트만
- 이벤트 제목에서 프로젝트 추출

### 2. 시간 카테고리
| 카테고리 | 설명 | 청구 |
|----------|------|------|
| BILLABLE | 클라이언트 청구 | ✓ |
| NON_BILLABLE | 내부 업무 | ✗ |
| OVERTIME | 야간/주말 | 할증 |

### 3. 반올림 규칙
- 15분 단위 반올림
- 23분 → 30분
- 7분 → 15분

---

## 🔧 구현 가이드

### 데이터 모델

```sql
CREATE TABLE projects (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    client_id BIGINT,
    code VARCHAR(50) UNIQUE,
    name VARCHAR(255) NOT NULL,
    default_rate DECIMAL(10,2) DEFAULT 0,
    currency VARCHAR(3) DEFAULT 'USD',
    budget_hours DECIMAL(10,2),
    status ENUM('ACTIVE', 'ARCHIVED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (client_id) REFERENCES clients(id)
);

CREATE TABLE clients (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    company_name VARCHAR(255) NOT NULL,
    contact_name VARCHAR(100),
    contact_email VARCHAR(255),
    default_rate DECIMAL(10,2),
    currency VARCHAR(3) DEFAULT 'USD',
    payment_terms ENUM('NET_15', 'NET_30', 'NET_60') DEFAULT 'NET_30',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE time_entries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    calendar_event_id BIGINT UNIQUE,
    project_id BIGINT NOT NULL,
    client_id BIGINT,
    date DATE NOT NULL,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    duration_hours DECIMAL(5,2) NOT NULL,
    description TEXT,
    billable BOOLEAN DEFAULT TRUE,
    rate DECIMAL(10,2) NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    category ENUM('BILLABLE', 'NON_BILLABLE', 'OVERTIME') DEFAULT 'BILLABLE',
    approval_status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
    invoice_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (calendar_event_id) REFERENCES calendar_events(id),
    FOREIGN KEY (project_id) REFERENCES projects(id),
    INDEX idx_user_date (user_id, date)
);
```

### 핵심 서비스

```java
@Service
@RequiredArgsConstructor
@Transactional
public class TimeEntryService {
    
    private final TimeEntryRepository timeEntryRepository;
    private final ProjectRepository projectRepository;
    private final CalendarEventRepository eventRepository;
    
    @EventListener
    public void onEventCompleted(CalendarEventCompletedEvent event) {
        CalendarEvent calendarEvent = event.getCalendarEvent();
        
        if (!shouldCreateTimeEntry(calendarEvent)) {
            return;
        }
        
        // 중복 체크
        if (timeEntryRepository.existsByCalendarEventId(calendarEvent.getId())) {
            log.info("TimeEntry already exists for event: {}", calendarEvent.getId());
            return;
        }
        
        TimeEntry entry = createTimeEntryFromEvent(calendarEvent);
        timeEntryRepository.save(entry);
        
        log.info("TimeEntry created: eventId={}, duration={}h", 
            calendarEvent.getId(), entry.getDurationHours());
    }
    
    private TimeEntry createTimeEntryFromEvent(CalendarEvent event) {
        Project project = matchProject(event);
        BigDecimal duration = calculateDuration(event.getStartAt(), event.getEndAt());
        BigDecimal rate = project != null ? project.getDefaultRate() : BigDecimal.ZERO;
        
        return TimeEntry.builder()
            .userId(event.getUserId())
            .calendarEventId(event.getId())
            .projectId(project != null ? project.getId() : null)
            .clientId(project != null ? project.getClientId() : null)
            .date(event.getStartAt().toLocalDate())
            .startTime(event.getStartAt())
            .endTime(event.getEndAt())
            .durationHours(duration)
            .description(event.getTitle())
            .billable(project != null)
            .rate(rate)
            .amount(duration.multiply(rate))
            .category(TimeCategory.BILLABLE)
            .build();
    }
    
    /**
     * 15분 단위 반올림
     */
    private BigDecimal calculateDuration(LocalDateTime start, LocalDateTime end) {
        long minutes = Duration.between(start, end).toMinutes();
        long roundedMinutes = Math.round(minutes / 15.0) * 15;
        return BigDecimal.valueOf(roundedMinutes).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }
    
    /**
     * 이벤트 제목/설명에서 프로젝트 매칭
     */
    private Project matchProject(CalendarEvent event) {
        // 1. 이벤트 제목에서 프로젝트 코드 찾기
        String title = event.getTitle();
        Pattern pattern = Pattern.compile("\\[([A-Z0-9-]+)\\]");
        Matcher matcher = pattern.matcher(title);
        
        if (matcher.find()) {
            String code = matcher.group(1);
            return projectRepository.findByCode(code).orElse(null);
        }
        
        // 2. 참석자 이메일 도메인으로 클라이언트 매칭
        // ...
        
        return null;
    }
    
    public List<TimeEntry> getEntriesByDateRange(Long userId, LocalDate start, LocalDate end) {
        return timeEntryRepository.findByUserIdAndDateBetween(userId, start, end);
    }
    
    public BigDecimal calculateBillableAmount(Long projectId, LocalDate start, LocalDate end) {
        return timeEntryRepository.sumBillableAmountByProjectAndPeriod(
            projectId, start.atStartOfDay(), end.plusDays(1).atStartOfDay()
        );
    }
}
```

---

## ✅ 완료 조건

| # | 조건 | 검증 방법 |
|---|------|----------|
| AC-1 | 자동 시간 기록 생성 | 이벤트 종료 시 TimeEntry 생성 |
| AC-2 | 15분 반올림 | 23분 → 0.5시간 확인 |
| AC-3 | 프로젝트 매핑 | [PROJ-01] 형식 인식 |
| AC-4 | 청구 가능 토글 | billable 필드 수정 API |
| AC-5 | 기간별 조회 | 날짜 범위 필터링 동작 |
| AC-6 | 자동 생성 정확도 90% | 수동 수정률 < 20% |

---

## 🔗 의존성

**선행 작업**: #010 (Calendar Sync)  
**후행 작업**: #017 (Invoice Generation)  
**병렬 가능**: #012 (Policy), #013 (Focus)

---

## 🏷️ 라벨
`backend`, `billing`, `time-tracking`, `priority:must`, `size:XL`

---

## 📚 참고 문서
- Task Spec: `tasks/functional/EPIC3-BE-001_Time_Tracking.md`
- REQ-IDs: REQ-FUNC-020



