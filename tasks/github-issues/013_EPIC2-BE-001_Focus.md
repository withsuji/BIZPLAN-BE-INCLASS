---
issue_number: 013
epic: EPIC-2
task_id: EPIC2-BE-001
title: "[BE] 포커스 블록 생성 및 동적 차단 규칙"
labels: ["backend", "focus", "calendar", "priority:must", "size:L"]
assignees: []
milestone: "MVP-Phase-2-Backend-Complete"
dependencies: ["#010"]
parallel_group: "group-3-calendar"
estimated_effort: "L"
difficulty: "중"
estimated_duration: "5일"
---

# Issue #013: 포커스 블록 생성 및 동적 차단 규칙

## 📋 개요
사용자의 집중 시간을 보호하기 위한 포커스 블록(Focus Block)을 자동 또는 수동으로 생성하고, 알림 및 미팅 예약을 차단하는 규칙을 구현한다.

## 🎯 목표
- 수동/자동 포커스 블록 생성
- 미팅 예약 차단 로직
- 반복 패턴 지원 (RFC 5545 RRULE)
- 주간 포커스 시간 통계

---

## 📝 상세 요구사항

### 1. 포커스 블록 유형
| 유형 | 설명 | 기본 시간 |
|------|------|----------|
| DEEP_WORK | 집중 업무 | 2-4시간 |
| SHALLOW_WORK | 일반 작업 | 30분-1시간 |
| LEARNING | 학습/교육 | 1-2시간 |
| PERSONAL | 개인 시간 | 유동적 |

### 2. 차단 규칙
- 포커스 블록 시간대 미팅 예약 차단
- 긴급 미팅(VIP) 예외 처리
- 차단 시도 로깅

### 3. 자동 생성
- 주간 최소 10시간 목표
- 가용 슬롯 중 최장 연속 시간 우선
- 사용자 선호 시간대 반영

---

## 🔧 구현 가이드

### 데이터 모델

```sql
CREATE TABLE focus_blocks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(100),
    type ENUM('DEEP_WORK', 'SHALLOW_WORK', 'LEARNING', 'PERSONAL') DEFAULT 'DEEP_WORK',
    start_at TIMESTAMP NOT NULL,
    end_at TIMESTAMP NOT NULL,
    timezone VARCHAR(64) DEFAULT 'UTC',
    recurrence_rule VARCHAR(500),
    is_auto_generated BOOLEAN DEFAULT FALSE,
    is_strict BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_user_time (user_id, start_at, end_at)
);

CREATE TABLE focus_block_violations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    focus_block_id BIGINT NOT NULL,
    requester_user_id BIGINT,
    meeting_title VARCHAR(255),
    attempted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    action ENUM('BLOCKED', 'WARNED', 'OVERRIDDEN') NOT NULL,
    reason VARCHAR(500),
    FOREIGN KEY (focus_block_id) REFERENCES focus_blocks(id)
);
```

### 핵심 서비스

```java
@Service
@RequiredArgsConstructor
public class FocusBlockService {
    
    private final FocusBlockRepository focusBlockRepository;
    private final CalendarEventRepository eventRepository;
    
    public FocusBlock createFocusBlock(CreateFocusBlockRequest request) {
        // 시간 충돌 검사
        List<CalendarEvent> conflicts = eventRepository.findByUserAndTimeRange(
            request.getUserId(), request.getStartAt(), request.getEndAt()
        );
        
        if (!conflicts.isEmpty()) {
            throw new ConflictException("Focus block conflicts with existing events");
        }
        
        FocusBlock block = FocusBlock.builder()
            .userId(request.getUserId())
            .title(request.getTitle())
            .type(request.getType())
            .startAt(request.getStartAt())
            .endAt(request.getEndAt())
            .recurrenceRule(request.getRecurrenceRule())
            .isStrict(request.isStrict())
            .build();
        
        return focusBlockRepository.save(block);
    }
    
    public FocusBlockCheckResult checkMeetingAllowed(Long userId, LocalDateTime start, LocalDateTime end) {
        List<FocusBlock> blocks = focusBlockRepository.findByUserAndTimeRange(userId, start, end);
        
        if (blocks.isEmpty()) {
            return FocusBlockCheckResult.allowed();
        }
        
        FocusBlock conflict = blocks.get(0);
        
        if (conflict.isStrict()) {
            logViolation(conflict, FocusBlockViolation.Action.BLOCKED);
            return FocusBlockCheckResult.blocked(conflict);
        } else {
            logViolation(conflict, FocusBlockViolation.Action.WARNED);
            return FocusBlockCheckResult.warned(conflict);
        }
    }
    
    public WeeklyFocusStats getWeeklyStats(Long userId, LocalDate weekStart) {
        LocalDateTime start = weekStart.atStartOfDay();
        LocalDateTime end = weekStart.plusDays(7).atStartOfDay();
        
        List<FocusBlock> blocks = focusBlockRepository.findByUserAndTimeRange(
            userId, start, end
        );
        
        long totalMinutes = blocks.stream()
            .mapToLong(b -> Duration.between(b.getStartAt(), b.getEndAt()).toMinutes())
            .sum();
        
        return WeeklyFocusStats.builder()
            .userId(userId)
            .weekStart(weekStart)
            .totalFocusMinutes(totalMinutes)
            .blockCount(blocks.size())
            .goalMinutes(600) // 10시간 목표
            .build();
    }
}
```

---

## ✅ 완료 조건

| # | 조건 | 검증 방법 |
|---|------|----------|
| AC-1 | 포커스 블록 CRUD | API 동작 확인 |
| AC-2 | 미팅 차단 | 포커스 시간 미팅 시도 시 ERROR |
| AC-3 | 반복 패턴 | RRULE 기반 반복 블록 생성 |
| AC-4 | 주간 통계 | 포커스 시간 합계 API |
| AC-5 | 차단 로깅 | 차단 시도 기록 저장 |

---

## 🔗 의존성

**선행 작업**: #010 (Calendar Sync)  
**후행 작업**: 없음  
**병렬 가능**: #012 (Policy Engine), #014 (Time Tracking)

---

## 🏷️ 라벨
`backend`, `focus`, `calendar`, `priority:must`, `size:L`

---

## 📚 참고 문서
- Task Spec: `tasks/functional/EPIC2-BE-001_Focus_Blocks.md`
- REQ-IDs: REQ-FUNC-002



