# EPIC1-BE-001: Google/Outlook Calendar 양방향 동기화 및 Webhook 수신부

## 1. 개요 및 목적
외부 캘린더 시스템(Google Calendar, Microsoft Outlook)과의 양방향 동기화를 구현하고, 실시간 이벤트 변경 사항을 Webhook을 통해 수신하는 기능을 구축한다.

## 2. 상세 요구사항

### 2.1. Calendar API 연동
- **Google Calendar API**: 
  - Events List, Get, Insert, Update, Delete API 연동
  - Watch API를 통한 Webhook 등록
  - 사용자별 OAuth 토큰을 사용한 인증
- **Microsoft Graph API**: 
  - Calendar Events API 연동
  - Subscription API를 통한 Webhook 등록
  - OAuth 2.0 토큰 기반 인증

### 2.2. 양방향 동기화
- **Pull Sync**: 주기적으로(5분마다) 외부 캘린더 변경 사항 가져오기
- **Push Sync**: 내부에서 생성/수정된 이벤트를 외부 캘린더에 반영
- **Conflict Resolution**: 동시 수정 발생 시 최신 타임스탬프 우선 정책
- **Sync Status**: 동기화 상태 추적 (last_sync_at, sync_status, error_message)

### 2.3. Webhook 수신 및 처리
- **Endpoint**: POST `/api/v1/calendar/webhook/{provider}`
- **Google**: X-Goog-Channel-Token 및 X-Goog-Resource-State 헤더 검증
- **Microsoft**: clientState 및 validationToken 검증
- **Event Processing**: 
  - 이벤트 생성/수정/삭제를 큐(SQS 또는 Redis)에 추가
  - 백그라운드 워커에서 비동기 처리
- **Idempotency**: 중복 Webhook 요청 처리 (event_id + updated_at 기준)

### 2.4. 데이터 모델
```yaml
CalendarConnection:
  - id: UUID
  - user_id: FK
  - provider: GOOGLE | OUTLOOK
  - calendar_id: string (외부 캘린더 ID)
  - access_token: encrypted
  - refresh_token: encrypted
  - webhook_channel_id: string
  - webhook_expiration: timestamp
  - last_sync_at: timestamp
  - status: ACTIVE | ERROR | DISABLED

CalendarEvent:
  - id: UUID
  - calendar_connection_id: FK
  - external_event_id: string
  - title: string
  - description: text
  - start_time: timestamp (UTC)
  - end_time: timestamp (UTC)
  - timezone: string (IANA)
  - location: string
  - attendees: JSON
  - status: CONFIRMED | TENTATIVE | CANCELLED
  - is_focus_block: boolean
  - created_at: timestamp
  - updated_at: timestamp
  - synced_at: timestamp
```

## 3. Task Definition (YAML)

```yaml
task_id: "EPIC1-BE-001"
title: "Google/Outlook Calendar 양방향 동기화 및 Webhook 수신 구현"
summary: >
  외부 캘린더 시스템과의 실시간 양방향 동기화 메커니즘을 구축한다.
type: "functional"

epic: "EPIC_1_CALENDAR_CORE"
req_ids: ["REQ-FUNC-001", "REQ-FUNC-003"]
component: ["backend.calendar", "backend.sync"]

context:
  srs_section: "3.1 External Systems (Google/Outlook Calendar)"
  tech_stack: ["Spring Boot", "Google Calendar API", "Microsoft Graph API", "MySQL"]

requirements:
  description: "외부 캘린더와의 실시간 동기화 및 충돌 없는 데이터 관리"
  kpis:
    - "동기화 지연 시간 < 30초"
    - "Webhook 처리 성공률 99% 이상"
    - "API 호출 실패 시 재시도 성공률 95% 이상"

design_constraints:
  - "외부 API Rate Limit 준수 (Google: 1,000 QPM, Microsoft: 10,000 RPM)"
  - "Webhook 재시도 로직 포함 (지수 백오프, 최대 5회)"
  - "PII 데이터는 암호화하여 저장"

steps_hint:
  - "CalendarConnection, CalendarEvent Entity 및 Repository 생성"
  - "Google Calendar API Client 구현 (google-api-client)"
  - "Microsoft Graph API Client 구현 (Microsoft Graph SDK)"
  - "CalendarSyncService 구현 (Pull Sync 로직)"
  - "CalendarWebhookController 구현 (Webhook 수신)"
  - "Webhook 검증 로직 구현 (서명/토큰 검증)"
  - "SyncQueue 구현 (Redis 또는 SQS)"
  - "Background Worker 구현 (Scheduled Task)"
  - "Conflict Resolution 로직 구현"
  - "Error Handling 및 Retry 메커니즘 구현"

preconditions:
  - "EPIC4-SYS-001 (DB) 완료"
  - "EPIC4-SYS-002 (OAuth) 완료"

postconditions:
  - "사용자가 Google/Outlook 캘린더를 연결할 수 있다."
  - "외부 캘린더의 이벤트가 30초 이내에 동기화된다."
  - "내부에서 생성한 이벤트가 외부 캘린더에 반영된다."
  - "Webhook을 통해 실시간 변경 사항을 수신한다."

dependencies: ["EPIC4-SYS-001", "EPIC4-SYS-002"]

parallelizable: false
estimated_effort: "XL"
priority: "Must"
agent_profile: ["backend"]

risk_notes:
  - "외부 API 장애 시 동기화 중단 (Fallback 전략 필요)"
  - "Webhook 엔드포인트가 공개되므로 보안 강화 필요 (HMAC 검증)"
  - "대량 이벤트 동기화 시 성능 저하 가능 (배치 처리 고려)"
```

