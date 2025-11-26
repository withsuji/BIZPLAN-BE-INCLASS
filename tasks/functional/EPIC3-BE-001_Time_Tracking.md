# EPIC3-BE-001: 캘린더 이벤트 → 시간 기록(TimeEntry) 자동 매핑 로직

## 1. 개요 및 목적
캘린더 이벤트를 기반으로 시간 기록(Time Entry)을 자동으로 생성하고, 프로젝트/클라이언트별로 시간을 추적하여 청구 가능한 데이터를 만든다.

## 2. 상세 요구사항

### 2.1. 자동 시간 기록 생성
- **Trigger**: 
  - 캘린더 이벤트 종료 시 자동 생성
  - 특정 카테고리/태그가 있는 이벤트만 대상 (예: "billable", "client-meeting")
- **매핑 규칙**: 
  - 이벤트 제목에서 프로젝트/클라이언트 추출 (패턴 매칭)
  - 이벤트 설명에 프로젝트 코드가 있으면 자동 연결
  - 참석자 정보로 클라이언트 자동 식별
- **수동 조정**: 
  - 사용자가 시간 기록을 수정/삭제 가능
  - 청구 가능 여부(billable) 토글
  - 시간 반올림 규칙 적용 (15분 단위)

### 2.2. 프로젝트 및 클라이언트 관리
- **Project**: 
  - 프로젝트 코드, 이름, 설명
  - 기본 시간당 요율 설정
  - 예산 및 누적 시간 추적
- **Client**: 
  - 클라이언트 정보 (회사명, 담당자, 이메일)
  - 기본 요율, 결제 조건 (Net 30, Net 60 등)
  - 청구 주기 (주간, 월간)

### 2.3. 시간 카테고리
- **Billable**: 클라이언트에게 청구 가능한 시간
- **Non-Billable**: 내부 회의, 관리 업무 등
- **Overtime**: 야간/주말 근무 (할증 요율 적용)

### 2.4. 시간 승인 워크플로 (Optional MVP)
- **Self-Approval**: 개인 사용자는 자동 승인
- **Team Approval**: 팀 환경에서는 매니저 승인 필요
- **Approval Status**: PENDING, APPROVED, REJECTED

### 2.5. 데이터 모델
```yaml
Project:
  - id: UUID
  - user_id: FK
  - client_id: FK
  - code: string (unique)
  - name: string
  - description: text
  - default_rate: decimal (시간당 요율)
  - currency: string (KRW, USD)
  - budget_hours: decimal nullable
  - status: ACTIVE | ARCHIVED
  - created_at: timestamp
  - updated_at: timestamp

Client:
  - id: UUID
  - user_id: FK
  - company_name: string
  - contact_name: string
  - contact_email: string
  - default_rate: decimal
  - currency: string
  - payment_terms: NET_30 | NET_60 | NET_90
  - billing_cycle: WEEKLY | MONTHLY
  - created_at: timestamp
  - updated_at: timestamp

TimeEntry:
  - id: UUID
  - user_id: FK
  - calendar_event_id: FK nullable
  - project_id: FK
  - client_id: FK
  - date: date
  - start_time: timestamp
  - end_time: timestamp
  - duration_hours: decimal (계산 필드)
  - description: text
  - billable: boolean
  - rate: decimal (시간당 요율)
  - amount: decimal (duration * rate)
  - category: BILLABLE | NON_BILLABLE | OVERTIME
  - approval_status: PENDING | APPROVED | REJECTED
  - invoice_id: FK nullable (청구서에 포함되면 설정)
  - created_at: timestamp
  - updated_at: timestamp
```

## 3. Task Definition (YAML)

```yaml
task_id: "EPIC3-BE-001"
title: "캘린더 이벤트 기반 자동 시간 기록 및 프로젝트 매핑 구현"
summary: >
  캘린더 이벤트를 시간 기록으로 자동 변환하고,
  프로젝트/클라이언트별로 시간을 추적하는 시스템을 구축한다.
type: "functional"

epic: "EPIC_3_BILLING"
req_ids: ["REQ-FUNC-020"]
component: ["backend.time-tracking", "backend.billing"]

context:
  srs_section: "3.3 Time Tracking Service"
  tech_stack: ["Spring Boot", "MySQL", "Redis"]

requirements:
  description: "정확한 시간 기록 자동화 및 청구 가능 데이터 생성"
  kpis:
    - "자동 생성 정확도 90% 이상"
    - "시간 기록 생성 지연 < 1분"
    - "사용자 수정률 < 20%"

design_constraints:
  - "시간은 15분 단위로 반올림 (예: 23분 → 30분)"
  - "청구서에 포함된 시간 기록은 수정/삭제 불가 (immutable)"
  - "삭제된 캘린더 이벤트의 시간 기록은 자동 삭제되지 않음 (수동 처리)"

steps_hint:
  - "Project, Client, TimeEntry Entity 및 Repository 생성"
  - "TimeEntryService 구현 (CRUD 및 자동 생성)"
  - "CalendarEventListener 구현 (이벤트 종료 시 TimeEntry 생성)"
  - "ProjectMatcher 구현 (이벤트 → 프로젝트 자동 매핑)"
  - "RoundingUtil 구현 (15분 단위 반올림)"
  - "POST /api/v1/time-entries API 구현 (수동 생성)"
  - "GET /api/v1/time-entries API 구현 (조회 및 필터링)"
  - "PUT /api/v1/time-entries/{id} API 구현 (수정)"
  - "DELETE /api/v1/time-entries/{id} API 구현 (삭제)"
  - "GET /api/v1/projects API 구현 (프로젝트 목록)"
  - "POST /api/v1/projects API 구현 (프로젝트 생성)"
  - "통계 API 구현 (월별/프로젝트별 시간 합계)"
  - "단위 테스트 및 통합 테스트 작성"

preconditions:
  - "EPIC1-BE-001 (Calendar Sync) 완료"

postconditions:
  - "캘린더 이벤트 종료 시 자동으로 시간 기록이 생성된다."
  - "사용자는 시간 기록을 조회/수정/삭제할 수 있다."
  - "프로젝트 및 클라이언트를 관리할 수 있다."
  - "청구 가능 여부를 토글할 수 있다."

dependencies: ["EPIC1-BE-001"]

parallelizable: true
estimated_effort: "XL"
priority: "Must"
agent_profile: ["backend"]

risk_notes:
  - "프로젝트 자동 매핑 정확도가 낮을 수 있음 (ML 모델 고려)"
  - "여러 프로젝트가 동시 진행될 경우 매핑 혼란 가능"
  - "시간 기록이 많아질수록 쿼리 성능 저하 (인덱싱 및 파티셔닝 필요)"
```

