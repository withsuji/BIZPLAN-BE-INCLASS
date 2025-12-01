---
issue_number: 015
epic: EPIC-2
task_id: EPIC2-BE-002
title: "[BE] LLM 요약 결과 저장 및 Action Item 관리"
labels: ["backend", "meeting", "summary", "action-item", "priority:must", "size:L"]
assignees: []
milestone: "MVP-Phase-2-Backend-Complete"
dependencies: ["#011"]
parallel_group: "group-3-ai"
estimated_effort: "L"
difficulty: "중"
estimated_duration: "5일"
---

# Issue #015: LLM 요약 결과 저장 및 Action Item 관리

## 📋 개요
LLM이 생성한 회의 요약 결과를 받아 DB에 구조화하여 저장하고, 액션 아이템을 추출하여 관리 가능한 형태로 제공한다.

## 🎯 목표
- 요약 결과 수신 및 DB 저장
- Action Item 상태 관리
- 버전 관리 (수정 히스토리)
- 기한 리마인더 발송

---

## 📝 상세 요구사항

### 1. 요약 데이터 저장
- Python 서비스로부터 Webhook 수신
- 필수 필드 검증
- 버전 히스토리 관리

### 2. Action Item 관리
| 상태 | 설명 |
|------|------|
| TODO | 미완료 |
| IN_PROGRESS | 진행 중 |
| DONE | 완료 |
| CANCELLED | 취소 |

### 3. 리마인더
- 기한 1일 전 알림
- 기한 경과 시 Overdue 표시

---

## 🔧 구현 가이드

### 데이터 모델

```sql
CREATE TABLE meeting_summaries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    meeting_id BIGINT NOT NULL,
    version INT NOT NULL DEFAULT 1,
    summary_text TEXT NOT NULL,
    key_decisions JSON,
    participants JSON,
    confidence_score DECIMAL(3,2),
    generated_by ENUM('AI', 'USER') DEFAULT 'AI',
    is_latest BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (meeting_id) REFERENCES calendar_events(id),
    UNIQUE KEY uk_meeting_version (meeting_id, version)
);

CREATE TABLE action_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    meeting_summary_id BIGINT NOT NULL,
    text TEXT NOT NULL,
    owner_user_id BIGINT,
    owner_name VARCHAR(100),
    due_date DATE,
    status ENUM('TODO', 'IN_PROGRESS', 'DONE', 'CANCELLED') DEFAULT 'TODO',
    priority ENUM('LOW', 'MEDIUM', 'HIGH') DEFAULT 'MEDIUM',
    reminded_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (meeting_summary_id) REFERENCES meeting_summaries(id),
    INDEX idx_owner_status (owner_user_id, status, due_date)
);
```

### 핵심 서비스

```java
@Service
@RequiredArgsConstructor
@Transactional
public class MeetingSummaryService {
    
    private final MeetingSummaryRepository summaryRepository;
    private final ActionItemRepository actionItemRepository;
    private final UserRepository userRepository;
    
    /**
     * LLM 요약 결과 저장
     */
    public MeetingSummary saveSummary(Long meetingId, SummaryResultDto dto) {
        // 기존 버전 비활성화
        summaryRepository.deactivateLatestVersion(meetingId);
        
        // 새 버전 저장
        int nextVersion = summaryRepository.getNextVersion(meetingId);
        
        MeetingSummary summary = MeetingSummary.builder()
            .meetingId(meetingId)
            .version(nextVersion)
            .summaryText(dto.getSummary())
            .keyDecisions(dto.getKeyDecisions())
            .participants(dto.getParticipants())
            .confidenceScore(dto.getConfidenceScore())
            .generatedBy(GeneratedBy.AI)
            .isLatest(true)
            .build();
        
        summary = summaryRepository.save(summary);
        
        // Action Items 저장
        for (ActionItemDto itemDto : dto.getActionItems()) {
            ActionItem item = ActionItem.builder()
                .meetingSummaryId(summary.getId())
                .text(itemDto.getText())
                .ownerName(itemDto.getOwner())
                .ownerUserId(matchUserByName(itemDto.getOwner()))
                .dueDate(parseDueDate(itemDto.getDueDate()))
                .priority(Priority.MEDIUM)
                .build();
            
            actionItemRepository.save(item);
        }
        
        return summary;
    }
    
    /**
     * Action Item 상태 변경
     */
    public ActionItem updateActionItemStatus(Long itemId, ActionItemStatus status) {
        ActionItem item = actionItemRepository.findById(itemId)
            .orElseThrow(() -> new NotFoundException("Action item not found"));
        
        item.setStatus(status);
        
        if (status == ActionItemStatus.DONE) {
            item.setCompletedAt(LocalDateTime.now());
        }
        
        return actionItemRepository.save(item);
    }
    
    /**
     * 요약 수정 (새 버전 생성)
     */
    public MeetingSummary updateSummary(Long meetingId, UpdateSummaryRequest request) {
        MeetingSummary latest = summaryRepository.findLatestByMeetingId(meetingId)
            .orElseThrow(() -> new NotFoundException("Summary not found"));
        
        // 기존 버전 비활성화
        latest.setIsLatest(false);
        summaryRepository.save(latest);
        
        // 새 버전 생성
        MeetingSummary newVersion = MeetingSummary.builder()
            .meetingId(meetingId)
            .version(latest.getVersion() + 1)
            .summaryText(request.getSummaryText())
            .keyDecisions(request.getKeyDecisions())
            .participants(latest.getParticipants())
            .confidenceScore(latest.getConfidenceScore())
            .generatedBy(GeneratedBy.USER)
            .isLatest(true)
            .build();
        
        return summaryRepository.save(newVersion);
    }
    
    private Long matchUserByName(String name) {
        if (name == null) return null;
        return userRepository.findByNameContaining(name)
            .map(User::getId)
            .orElse(null);
    }
}
```

### 리마인더 스케줄러

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class ActionItemReminderScheduler {
    
    private final ActionItemRepository actionItemRepository;
    private final NotificationService notificationService;
    
    @Scheduled(cron = "0 0 9 * * *") // 매일 오전 9시
    public void sendDueDateReminders() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        
        List<ActionItem> items = actionItemRepository
            .findByDueDateAndStatusAndRemindedAtIsNull(tomorrow, ActionItemStatus.TODO);
        
        for (ActionItem item : items) {
            if (item.getOwnerUserId() != null) {
                notificationService.sendActionItemReminder(item);
                item.setRemindedAt(LocalDateTime.now());
                actionItemRepository.save(item);
            }
        }
        
        log.info("Sent {} action item reminders", items.size());
    }
    
    @Scheduled(cron = "0 0 0 * * *") // 매일 자정
    public void markOverdueItems() {
        LocalDate today = LocalDate.now();
        int updated = actionItemRepository.markOverdue(today);
        log.info("Marked {} action items as overdue", updated);
    }
}
```

---

## ✅ 완료 조건

| # | 조건 | 검증 방법 |
|---|------|----------|
| AC-1 | 요약 저장 | POST /summary 호출 시 DB 저장 |
| AC-2 | Action Item 추출 | 요약에서 Action Item 분리 저장 |
| AC-3 | 상태 변경 | PUT /action-items/{id} 동작 |
| AC-4 | 버전 관리 | 수정 시 새 버전 생성 |
| AC-5 | 리마인더 발송 | 기한 1일 전 알림 |
| AC-6 | 담당자 매핑 | 이름으로 User ID 자동 매칭 |

---

## 🔗 의존성

**선행 작업**: #011 (LLM Pipeline)  
**후행 작업**: 없음  
**병렬 가능**: #012, #013, #014

---

## 🏷️ 라벨
`backend`, `meeting`, `summary`, `action-item`, `priority:must`, `size:L`

---

## 📚 참고 문서
- Task Spec: `tasks/functional/EPIC2-BE-002_Summary_Storage.md`
- REQ-IDs: REQ-FUNC-011



