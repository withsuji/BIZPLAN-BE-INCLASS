# EPIC3-BE-002: 인보이스 생성 및 상태 관리 (Draft -> Sent -> Paid)

## 1. 개요 및 목적
시간 기록(Time Entry)을 기반으로 인보이스를 자동 생성하고, 발송부터 결제 완료까지의 생명주기를 관리한다.

## 2. 상세 요구사항

### 2.1. 인보이스 자동 생성
- **Trigger**: 
  - 주간/월간 배치 작업 (매주 일요일, 매월 말일)
  - 수동 생성 (특정 기간 및 프로젝트 선택)
- **생성 로직**: 
  1. 청구 가능(billable)하고 아직 청구되지 않은 TimeEntry 조회
  2. 클라이언트/프로젝트별로 그룹핑
  3. 각 그룹에 대해 Invoice 생성
  4. Line Item 생성 (각 TimeEntry가 하나의 항목)
  5. 총액 계산 (합계, 세금, 할인 적용)
- **인보이스 번호**: 
  - 형식: `INV-{YEAR}{MONTH}-{SEQUENCE}` (예: INV-202511-001)
  - 자동 증가 시퀀스

### 2.2. 인보이스 상태 관리
- **상태 전환**:
  ```
  DRAFT → SENT → PAID
        ↓        ↓
     CANCELLED  OVERDUE
  ```
- **DRAFT**: 초안 상태, 수정 가능
- **SENT**: 클라이언트에게 발송됨, 수정 불가 (Credit Note로만 조정)
- **PAID**: 결제 완료
- **OVERDUE**: 기한 경과
- **CANCELLED**: 취소됨

### 2.3. 인보이스 내용
- **Header**: 
  - 인보이스 번호, 발행일, 기한 (due_date)
  - 발행자 정보 (이름, 주소, 사업자번호)
  - 수신자 정보 (클라이언트)
- **Line Items**: 
  - 날짜, 프로젝트, 작업 설명, 시간, 요율, 금액
- **Summary**: 
  - 소계 (subtotal)
  - 세금 (tax, 부가세 10%)
  - 할인 (discount, Optional)
  - 총액 (total)
- **Payment Info**: 
  - 계좌 정보 또는 결제 링크
  - 결제 조건 (Net 30 등)

### 2.4. 인보이스 발송
- **이메일 발송**: 
  - PDF 첨부 (PDF 생성 라이브러리 사용)
  - 이메일 템플릿 (HTML)
  - 결제 링크 포함
- **발송 기록**: 
  - 발송 시각, 수신자, 상태 추적
- **재발송**: 
  - 사용자가 수동으로 재발송 가능
  - Dunning 알림 시 자동 재발송

### 2.5. 데이터 모델
```yaml
Invoice:
  - id: UUID
  - invoice_number: string (unique)
  - user_id: FK (발행자)
  - client_id: FK
  - project_id: FK nullable
  - issue_date: date
  - due_date: date
  - status: DRAFT | SENT | PAID | OVERDUE | CANCELLED
  - subtotal: decimal
  - tax_rate: decimal (0.1 = 10%)
  - tax_amount: decimal
  - discount_amount: decimal
  - total_amount: decimal
  - currency: string (KRW, USD)
  - notes: text
  - payment_method: BANK_TRANSFER | CREDIT_CARD | STRIPE
  - paid_at: timestamp nullable
  - sent_at: timestamp nullable
  - created_at: timestamp
  - updated_at: timestamp

InvoiceLineItem:
  - id: UUID
  - invoice_id: FK
  - time_entry_id: FK
  - date: date
  - description: text
  - hours: decimal
  - rate: decimal
  - amount: decimal
  - created_at: timestamp

InvoiceSendLog:
  - id: UUID
  - invoice_id: FK
  - recipient_email: string
  - sent_at: timestamp
  - status: SUCCESS | FAILED
  - error_message: text nullable
```

## 3. Task Definition (YAML)

```yaml
task_id: "EPIC3-BE-002"
title: "인보이스 자동 생성 및 상태 관리 시스템 구현"
summary: >
  시간 기록 기반 인보이스를 자동 생성하고,
  Draft → Sent → Paid 상태 전환을 관리하는 시스템을 구축한다.
type: "functional"

epic: "EPIC_3_BILLING"
req_ids: ["REQ-FUNC-021"]
component: ["backend.billing", "backend.invoice"]

context:
  srs_section: "3.3 Billing Service"
  tech_stack: ["Spring Boot", "MySQL", "PDF Library (iText)", "Email (SMTP)"]

requirements:
  description: "정확한 인보이스 생성 및 생명주기 관리"
  kpis:
    - "인보이스 생성 정확도 100%"
    - "이메일 발송 성공률 95% 이상"
    - "PDF 생성 시간 < 3초"

design_constraints:
  - "SENT 상태 이후 인보이스는 수정 불가 (Credit Note로만 조정)"
  - "세금 계산은 국가별 세율 고려 (한국: 10% 부가세)"
  - "인보이스 번호는 연도별로 리셋되지 않음 (연속 증가)"

steps_hint:
  - "Invoice, InvoiceLineItem, InvoiceSendLog Entity 및 Repository 생성"
  - "InvoiceService 구현 (생성, 상태 전환, 발송)"
  - "InvoiceNumberGenerator 구현 (시퀀스 관리)"
  - "InvoicePdfGenerator 구현 (iText 활용)"
  - "InvoiceEmailService 구현 (SMTP 발송)"
  - "InvoiceScheduler 구현 (주간/월간 자동 생성)"
  - "POST /api/v1/invoices API 구현 (수동 생성)"
  - "GET /api/v1/invoices API 구현 (조회 및 필터링)"
  - "PATCH /api/v1/invoices/{id}/status API 구현 (상태 변경)"
  - "POST /api/v1/invoices/{id}/send API 구현 (발송)"
  - "GET /api/v1/invoices/{id}/pdf API 구현 (PDF 다운로드)"
  - "OverdueDetectionScheduler 구현 (매일 00시 실행)"
  - "단위 테스트 및 통합 테스트 작성"

preconditions:
  - "EPIC3-BE-001 (Time Tracking) 완료"

postconditions:
  - "주간/월간 자동으로 인보이스가 생성된다."
  - "사용자는 인보이스를 조회/수정/발송할 수 있다."
  - "인보이스를 이메일로 발송할 수 있다."
  - "PDF 형식으로 다운로드할 수 있다."
  - "기한 경과 시 자동으로 OVERDUE 상태로 변경된다."

dependencies: ["EPIC3-BE-001"]

parallelizable: false
estimated_effort: "XL"
priority: "Must"
agent_profile: ["backend"]

risk_notes:
  - "PDF 생성 시 한글 폰트 문제 발생 가능 (폰트 임베딩 필요)"
  - "대량 이메일 발송 시 SMTP 제한에 걸릴 수 있음 (SES 등 고려)"
  - "세금 계산 로직이 복잡해질 수 있음 (국가별, 품목별 세율)"
```

