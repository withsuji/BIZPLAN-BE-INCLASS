# EPIC2-BE-002: 요약 결과 파싱 및 구조화 저장 (Action Item 추출)

## 1. 개요 및 목적
LLM이 생성한 회의 요약 결과를 받아 DB에 구조화하여 저장하고, 액션 아이템을 추출하여 관리 가능한 형태로 제공한다.

## 2. 상세 요구사항

### 2.1. 요약 결과 수신 및 저장
- **API 연동**: 
  - Python LLM Service로부터 요약 결과를 Webhook 또는 Polling으로 수신
  - Java Backend에서 요약 데이터를 파싱하여 DB에 저장
- **데이터 검증**: 
  - 필수 필드 누락 시 에러 처리
  - 날짜 형식, 담당자 이메일 등 유효성 검증
- **버전 관리**: 
  - 사용자가 요약을 재생성하거나 수정할 경우 버전 히스토리 저장
  - 최신 버전만 기본 조회

### 2.2. Action Item 관리
- **추출 및 파싱**: 
  - 액션 아이템을 별도 테이블에 저장
  - 담당자(owner)를 User ID와 매핑
  - 기한(due_date)을 Date 타입으로 변환
- **상태 관리**: 
  - TODO, IN_PROGRESS, DONE, CANCELLED
  - 사용자가 체크박스로 상태 변경 가능
- **알림 및 리마인더**: 
  - 기한 1일 전 담당자에게 알림 발송
  - 기한 경과 시 Overdue 상태로 변경 및 알림
- **통합**: 
  - Jira, Asana 등 외부 Task 관리 시스템으로 Export 가능 (향후)

### 2.3. 요약 조회 및 수정
- **조회 API**: 
  - GET `/api/v1/meetings/{id}/summary`: 최신 요약 조회
  - GET `/api/v1/meetings/{id}/summary/versions`: 버전 히스토리 조회
- **수정 API**: 
  - PATCH `/api/v1/meetings/{id}/summary`: 요약 내용 수정
  - PUT `/api/v1/action-items/{id}`: 액션 아이템 수정
- **재생성 API**: 
  - POST `/api/v1/meetings/{id}/summary/regenerate`: 요약 재생성 요청

### 2.4. 검색 및 필터링
- **전체 검색**: 
  - 요약 내용, 액션 아이템에서 키워드 검색
- **필터링**: 
  - 날짜 범위, 참석자, 액션 아이템 상태, 담당자별 필터링
- **통계**: 
  - 완료된 액션 아이템 수 / 전체 액션 아이템 수
  - 평균 완료 시간

### 2.5. 데이터 모델
```yaml
MeetingSummary:
  - id: UUID
  - meeting_id: FK (CalendarEvent)
  - version: integer
  - summary_text: text
  - key_decisions: JSON (array of strings)
  - participants: JSON (array of strings)
  - confidence_score: decimal
  - generated_by: AI | USER
  - generated_at: timestamp
  - is_latest: boolean
  - created_at: timestamp
  - updated_at: timestamp

ActionItem:
  - id: UUID
  - meeting_summary_id: FK
  - text: text
  - owner_user_id: FK (User) nullable
  - owner_name: string
  - due_date: date
  - status: TODO | IN_PROGRESS | DONE | CANCELLED
  - priority: LOW | MEDIUM | HIGH
  - reminded_at: timestamp nullable
  - completed_at: timestamp nullable
  - created_at: timestamp
  - updated_at: timestamp
```

## 3. Task Definition (YAML)

```yaml
task_id: "EPIC2-BE-002"
title: "LLM 요약 결과 파싱 및 구조화 저장 (Action Item 관리)"
summary: >
  LLM 생성 요약을 DB에 저장하고, 액션 아이템을 추출하여
  관리 가능한 형태로 제공하는 백엔드 로직을 구현한다.
type: "functional"

epic: "EPIC_2_FOCUS_AI"
req_ids: ["REQ-FUNC-011"]
component: ["backend.meeting", "backend.summary"]

context:
  srs_section: "3.2 Meeting/Summary Service"
  tech_stack: ["Spring Boot", "MySQL", "Redis"]

requirements:
  description: "구조화된 요약 저장 및 액션 아이템 생명주기 관리"
  kpis:
    - "요약 저장 성공률 100%"
    - "액션 아이템 완료율 70% 이상"
    - "리마인더 발송 정확도 100%"

design_constraints:
  - "요약 데이터는 버전 관리하여 수정 히스토리 추적 가능"
  - "액션 아이템의 담당자는 User ID로 매핑 (이메일 기반 자동 매칭)"
  - "기한 경과 액션 아이템은 자동으로 Overdue 플래그 설정"

steps_hint:
  - "MeetingSummary, ActionItem Entity 및 Repository 생성"
  - "MeetingSummaryService 구현 (CRUD 및 버전 관리)"
  - "ActionItemService 구현 (상태 관리, 리마인더)"
  - "SummaryWebhookController 구현 (Python Service로부터 결과 수신)"
  - "SummaryDTO 및 ActionItemDTO 정의"
  - "GET /api/v1/meetings/{id}/summary API 구현"
  - "PATCH /api/v1/meetings/{id}/summary API 구현"
  - "PUT /api/v1/action-items/{id} API 구현"
  - "ActionItemReminderScheduler 구현 (매일 00시 실행)"
  - "이메일/Slack 알림 발송 로직 구현"
  - "검색 및 필터링 쿼리 최적화 (Elasticsearch 고려)"
  - "단위 테스트 및 통합 테스트 작성"

preconditions:
  - "EPIC2-AI-001 (LLM Pipeline) 완료"
  - "EPIC1-BE-001 (Calendar Sync) 완료"

postconditions:
  - "LLM이 생성한 요약이 DB에 저장된다."
  - "액션 아이템이 별도로 추출되어 관리된다."
  - "사용자는 액션 아이템의 상태를 변경할 수 있다."
  - "기한 전날 담당자에게 리마인더가 발송된다."
  - "요약 수정 시 버전이 증가하고 히스토리가 유지된다."

dependencies: ["EPIC2-AI-001"]

parallelizable: false
estimated_effort: "L"
priority: "Must"
agent_profile: ["backend"]

risk_notes:
  - "담당자 이메일 매칭 실패 시 액션 아이템이 누락될 수 있음 (수동 매핑 UI 필요)"
  - "액션 아이템이 많을 경우 알림 폭주 가능 (배치 알림 고려)"
  - "버전 관리로 인한 스토리지 증가 (오래된 버전 정리 정책 필요)"
```

