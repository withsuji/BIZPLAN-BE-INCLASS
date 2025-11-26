# EPIC0-FE-005: FE PoC - 시간 기록 및 인보이스 관리 UI

## 1. 개요 및 목적
캘린더 일정과 연동된 시간 기록(Time Entries) 목록을 조회하고, 이를 바탕으로 생성된 인보이스(Invoice)의 상태를 관리하는 화면을 구현한다. 사용자가 "자동 생성된 기록"을 검토하고 "청구서 발송"을 누르는 흐름을 검증한다.

## 2. 상세 요구사항

### 2.1. 화면 구성
- **URL**: `/invoices`
- **Tab UI**: "Time Entries" / "Invoices"
- **Time Entries Tab**:
    - 데이터 테이블: 날짜, 프로젝트/클라이언트, 작업 내용(일정 제목), 소요 시간, 청구 가능 여부(Billable).
    - **Bulk Actions**: 선택 항목 "Create Invoice" 버튼.
- **Invoices Tab**:
    - 인보이스 리스트: ID, 클라이언트, 금액, 상태(Draft, Sent, Paid, Overdue), 기한.
    - 상태별 배지 스타일링 (Paid=Green, Overdue=Red).
    - "Send Now" 버튼 (Draft 상태일 때).

### 2.2. 상호작용 (Mock)
- Time Entry 행을 클릭하면 수정 모달(시간/메모 수정)이 뜬다.
- "Create Invoice" 클릭 시 Invoices 탭으로 이동하며 새 행이 추가되는 Mock 동작.
- "Send Now" 클릭 시 상태가 Sent로 변경되고 알림 토스트가 뜬다.

## 3. Task Definition (YAML)

```yaml
task_id: "EPIC0-FE-005"
title: "FE PoC - 시간 기록 테이블 및 인보이스 발송 관리 UI"
summary: >
  시간 기록 목록과 인보이스 상태를 관리하는 테이블 기반 UI를 구현한다.
  데이터 그리드와 상태 변경 액션을 Mock으로 처리한다.
type: "functional"

epic: "EPIC_0_FE_PROTOTYPE"
req_ids: ["REQ-FUNC-020", "REQ-FUNC-021"]
component: ["frontend.billing"]

context:
  srs_section: "3.3 Billing/Dunning Service"

inputs:
  description: "Mock Time Entries & Invoices (JSON)"
  mock_data_example: |
    {
      "entries": [
        { "id": 1, "date": "2025-11-25", "task": "Consulting", "hours": 2.0, "rate": 100, "billable": true },
        { "id": 2, "date": "2025-11-26", "task": "Internal Meeting", "hours": 1.0, "rate": 0, "billable": false }
      ],
      "invoices": [
        { "id": "INV-001", "client": "Acme Corp", "amount": 500, "status": "draft", "due": "2025-12-10" }
      ]
    }

outputs:
  description: "시간 기록 및 인보이스 관리 페이지"
  artifacts:
    - "src/pages/InvoicePage.tsx"
    - "src/components/TimeEntryTable.tsx"
    - "src/components/InvoiceTable.tsx"

steps_hint:
  - "Tab 컴포넌트 구현 (Time Entries / Invoices)"
  - "TimeEntryTable 구현 (TanStack Table 등 활용 또는 단순 Table)"
  - "청구 가능 여부 토글 스위치 UI 구현"
  - "InvoiceTable 구현 및 상태(Status)에 따른 뱃지 스타일링"
  - "발송(Send) 버튼 클릭 시 상태 변경 로직(Mock State) 연결"

preconditions:
  - "EPIC0-FE-001 (Layout) 완료"

postconditions:
  - "사용자는 시간 기록을 수정할 수 있어야 한다(Mock)."
  - "인보이스 발송 시 상태 변화가 UI에 즉시 반영되어야 한다."

dependencies: ["EPIC0-FE-001"]

parallelizable: true
estimated_effort: "M"
priority: "Must"
agent_profile: ["frontend"]
```









