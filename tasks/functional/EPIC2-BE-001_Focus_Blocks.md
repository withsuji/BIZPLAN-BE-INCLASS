# EPIC2-BE-001: 포커스 블록 생성 및 동적 차단 규칙 구현

## 1. 개요 및 목적
사용자의 집중 시간을 보호하기 위한 포커스 블록(Focus Block)을 자동 또는 수동으로 생성하고, 알림 및 미팅 예약을 차단하는 규칙을 구현한다.

## 2. 상세 요구사항

### 2.1. 포커스 블록 생성
- **수동 생성**: 사용자가 원하는 시간대에 직접 포커스 블록 생성
- **자동 생성**: 
  - 일주일 기준으로 최소 10시간의 포커스 블록 확보
  - 가용 슬롯 중 가장 긴 연속 시간을 우선 선택
  - 사용자의 선호 시간대 고려
- **반복 패턴**: 
  - 매일, 매주, 격주 등 반복 패턴 설정 가능
  - 특정 요일 제외 설정 (예: 월수금만)

### 2.2. 차단 규칙
- **미팅 차단**: 
  - 포커스 블록 시간대에는 새로운 미팅 예약 불가
  - 기존 미팅은 유지 (수동 조정 필요 경고)
- **알림 차단 (DND)**: 
  - 포커스 블록 동안 Slack, Email 등 알림 자동 음소거
  - Calendar API를 통해 DND 상태 설정
- **예외 처리**: 
  - 긴급 미팅(VIP 참석자)은 경고 표시 후 예약 허용
  - 사용자가 수동으로 포커스 블록 이동/삭제 가능

### 2.3. 포커스 블록 유형
- **Deep Work**: 2-4시간 연속 집중 시간
- **Shallow Work**: 30분-1시간 작은 작업 시간
- **Learning**: 학습/교육 시간
- **Personal**: 개인 시간 (운동, 휴식 등)

### 2.4. 통계 및 리포트
- **주간 포커스 시간 합계**: 목표 대비 달성률 표시
- **방해 받은 횟수**: 포커스 블록 중 예약 시도 횟수
- **생산성 점수**: 포커스 시간 비율 기반 점수 계산

### 2.5. 데이터 모델
```yaml
FocusBlock:
  - id: UUID
  - user_id: FK
  - title: string
  - description: text
  - start_time: timestamp (UTC)
  - end_time: timestamp (UTC)
  - timezone: string (IANA)
  - type: DEEP_WORK | SHALLOW_WORK | LEARNING | PERSONAL
  - recurrence_rule: string (RFC 5545 RRULE)
  - is_auto_generated: boolean
  - is_strict: boolean (엄격 차단 여부)
  - created_at: timestamp
  - updated_at: timestamp

FocusBlockViolation:
  - id: UUID
  - focus_block_id: FK
  - meeting_request_id: FK
  - requester_user_id: FK
  - attempted_at: timestamp
  - action: BLOCKED | WARNED | OVERRIDDEN
  - reason: string

UserFocusPreference:
  - id: UUID
  - user_id: FK
  - weekly_target_hours: integer
  - preferred_time_slots: JSON
  - enable_auto_block: boolean
  - enable_dnd: boolean
  - created_at: timestamp
  - updated_at: timestamp
```

## 3. Task Definition (YAML)

```yaml
task_id: "EPIC2-BE-001"
title: "포커스 블록 생성 및 동적 차단 규칙 엔진 구현"
summary: >
  사용자의 집중 시간을 보호하는 포커스 블록을 자동/수동으로 생성하고,
  미팅 및 알림 차단 규칙을 적용하는 시스템을 구축한다.
type: "functional"

epic: "EPIC_2_FOCUS_AI"
req_ids: ["REQ-FUNC-002"]
component: ["backend.focus", "backend.calendar"]

context:
  srs_section: "3.2 Focus Service"
  tech_stack: ["Spring Boot", "MySQL", "Redis"]

requirements:
  description: "효과적인 포커스 시간 보호 및 차단 규칙 적용"
  kpis:
    - "포커스 블록 차단 성공률 100%"
    - "자동 생성 포커스 블록이 사용자 선호도와 80% 이상 일치"
    - "주간 포커스 시간 목표 달성률 70% 이상"

design_constraints:
  - "포커스 블록은 캘린더 이벤트와 동일하게 취급 (CalendarEvent의 is_focus_block=true)"
  - "외부 캘린더에도 포커스 블록을 동기화하여 타인이 볼 수 있도록 함"
  - "긴급 미팅은 차단하지 않되, 사용자에게 알림 발송"

steps_hint:
  - "FocusBlock, FocusBlockViolation, UserFocusPreference Entity 및 Repository 생성"
  - "FocusBlockService 구현 (CRUD 및 자동 생성 로직)"
  - "AutoFocusBlockScheduler 구현 (매주 일요일 밤 실행)"
  - "FocusBlockEnforcer 구현 (미팅 예약 시 차단 검증)"
  - "DND Integration (Slack API, Gmail API 연동)"
  - "Recurrence Rule Parser 구현 (RFC 5545 RRULE 파싱)"
  - "POST /api/v1/focus/blocks API 엔드포인트 구현"
  - "GET /api/v1/focus/stats API 엔드포인트 구현 (통계)"
  - "Violation Logging 구현"
  - "단위 테스트 및 통합 테스트 작성"

preconditions:
  - "EPIC1-BE-001 (Calendar Sync) 완료"

postconditions:
  - "사용자는 포커스 블록을 생성하고 관리할 수 있다."
  - "포커스 블록 시간대에 미팅 예약 시도 시 차단된다."
  - "자동 생성 기능을 활성화하면 매주 포커스 블록이 생성된다."
  - "주간 포커스 시간 통계를 조회할 수 있다."

dependencies: ["EPIC1-BE-001"]

parallelizable: true
estimated_effort: "L"
priority: "Must"
agent_profile: ["backend"]

risk_notes:
  - "외부 캘린더 동기화 시 포커스 블록이 일반 이벤트로 보일 수 있음 (제목으로 구분)"
  - "반복 패턴이 복잡해질수록 버그 발생 가능성 증가"
  - "DND 통합 시 외부 API 의존성 증가"
```

