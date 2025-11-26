# EPIC3-BE-003: 결제 게이트웨이(Stripe) 연동 및 Webhook 처리

## 1. 개요 및 목적
Stripe 결제 게이트웨이를 연동하여 인보이스 온라인 결제 기능을 제공하고, Webhook을 통해 결제 결과를 실시간으로 수신하여 처리한다.

## 2. 상세 요구사항

### 2.1. Stripe 연동
- **Stripe API**: 
  - Stripe Java SDK 사용
  - Test Mode / Live Mode 분리
  - API Key는 Secrets Manager에 저장
- **지원 결제 수단**: 
  - 신용카드/체크카드 (Visa, MasterCard, Amex)
  - 계좌이체 (한국: 토스페이, 카카오페이 등 추가 고려)
- **결제 플로우**: 
  1. 사용자가 인보이스 결제 링크 클릭
  2. Stripe Checkout Session 생성
  3. Stripe 결제 페이지로 리다이렉트
  4. 결제 완료 후 Success URL로 리다이렉트
  5. Webhook을 통해 결제 결과 수신 및 인보이스 상태 업데이트

### 2.2. Stripe Checkout Session
- **생성 API**: 
  - POST `/api/v1/invoices/{id}/checkout`
  - 인보이스 정보를 기반으로 Stripe Checkout Session 생성
  - 금액, 통화, 고객 정보, Success/Cancel URL 전달
- **응답**: 
  - Stripe Checkout URL 반환
  - 사용자를 해당 URL로 리다이렉트

### 2.3. Webhook 처리
- **Endpoint**: 
  - POST `/api/v1/webhooks/stripe`
  - Stripe Signature 검증 필수
- **처리 이벤트**: 
  - `checkout.session.completed`: 결제 완료
  - `payment_intent.succeeded`: 결제 성공
  - `payment_intent.payment_failed`: 결제 실패
  - `charge.refunded`: 환불 처리
- **처리 로직**: 
  1. Webhook Signature 검증
  2. Event Type에 따라 분기
  3. 인보이스 상태 업데이트 (PAID)
  4. 결제 기록 저장 (Payment)
  5. 사용자에게 이메일 알림 발송
  6. 200 OK 응답 (Stripe가 재시도하지 않도록)

### 2.4. 결제 기록 관리
- **Payment**: 
  - 결제 ID (Stripe Payment Intent ID)
  - 결제 금액, 수수료, 실제 수령 금액
  - 결제 시각, 결제 수단
  - 상태 (SUCCESS, FAILED, REFUNDED)
- **환불 처리**: 
  - 부분 환불 및 전체 환불 지원
  - 환불 이유 기록
  - 인보이스 상태는 PAID 유지 (Credit Note 발행)

### 2.5. 데이터 모델
```yaml
Payment:
  - id: UUID
  - invoice_id: FK
  - stripe_payment_intent_id: string (unique)
  - stripe_checkout_session_id: string
  - amount: decimal
  - fee: decimal (Stripe 수수료)
  - net_amount: decimal (amount - fee)
  - currency: string
  - payment_method: CARD | BANK_TRANSFER
  - card_last4: string nullable
  - card_brand: string nullable (Visa, MasterCard 등)
  - status: PENDING | SUCCESS | FAILED | REFUNDED
  - paid_at: timestamp nullable
  - refunded_at: timestamp nullable
  - refund_reason: text nullable
  - metadata: JSON
  - created_at: timestamp
  - updated_at: timestamp

StripeWebhookLog:
  - id: UUID
  - event_id: string (Stripe Event ID)
  - event_type: string
  - payload: JSON
  - processed: boolean
  - processed_at: timestamp nullable
  - error_message: text nullable
  - created_at: timestamp
```

## 3. Task Definition (YAML)

```yaml
task_id: "EPIC3-BE-003"
title: "Stripe 결제 게이트웨이 연동 및 Webhook 처리 구현"
summary: >
  Stripe를 통한 온라인 결제 기능을 구현하고,
  Webhook을 통해 결제 결과를 실시간으로 수신하여 인보이스를 업데이트한다.
type: "functional"

epic: "EPIC_3_BILLING"
req_ids: ["REQ-FUNC-022"]
component: ["backend.billing", "backend.payment"]

context:
  srs_section: "3.1 External Systems (Stripe)"
  tech_stack: ["Spring Boot", "Stripe Java SDK", "MySQL"]

requirements:
  description: "안전하고 신뢰할 수 있는 결제 처리 및 Webhook 관리"
  kpis:
    - "결제 성공률 98% 이상 (Stripe 의존)"
    - "Webhook 처리 성공률 100%"
    - "Webhook 처리 시간 < 500ms"

design_constraints:
  - "Webhook Signature 검증 필수 (보안)"
  - "Webhook은 멱등성(Idempotency) 보장 (중복 요청 처리)"
  - "결제 정보는 PCI-DSS 준수 (카드 정보 직접 저장 금지)"

steps_hint:
  - "Payment, StripeWebhookLog Entity 및 Repository 생성"
  - "Stripe Java SDK 의존성 추가"
  - "StripeService 구현 (Checkout Session 생성)"
  - "POST /api/v1/invoices/{id}/checkout API 구현"
  - "StripeWebhookController 구현 (POST /api/v1/webhooks/stripe)"
  - "WebhookSignatureValidator 구현 (Stripe Signature 검증)"
  - "PaymentService 구현 (결제 기록 저장 및 인보이스 업데이트)"
  - "Event Handler 구현 (Event Type별 분기 처리)"
  - "WebhookRetryHandler 구현 (실패 시 재처리)"
  - "환불 API 구현 (POST /api/v1/payments/{id}/refund)"
  - "GET /api/v1/payments API 구현 (결제 내역 조회)"
  - "단위 테스트 및 통합 테스트 (Stripe Test Mode 활용)"

preconditions:
  - "EPIC3-BE-002 (Invoice Generation) 완료"
  - "Stripe 계정 생성 및 API Key 발급 완료"

postconditions:
  - "사용자는 인보이스 결제 링크를 통해 온라인 결제할 수 있다."
  - "결제 완료 시 인보이스 상태가 자동으로 PAID로 변경된다."
  - "결제 실패 시 사용자에게 알림이 발송된다."
  - "환불 처리를 할 수 있다."
  - "결제 내역을 조회할 수 있다."

dependencies: ["EPIC3-BE-002"]

parallelizable: false
estimated_effort: "L"
priority: "Must"
agent_profile: ["backend"]

risk_notes:
  - "Stripe Webhook이 늦게 도착하거나 누락될 수 있음 (Polling Fallback 고려)"
  - "결제 수수료가 높을 수 있음 (2.9% + $0.30)"
  - "한국 시장에서는 Stripe보다 토스페이, 카카오페이 선호도가 높음 (추가 연동 고려)"
  - "Webhook 엔드포인트가 공개되므로 DDoS 공격 가능 (Rate Limiting 필수)"
```

