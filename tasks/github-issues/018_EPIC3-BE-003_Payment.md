---
issue_number: 018
epic: EPIC-3
task_id: EPIC3-BE-003
title: "[BE] Stripe 결제 게이트웨이 연동 및 Webhook 처리"
labels: ["backend", "billing", "payment", "stripe", "priority:must"]
assignees: []
milestone: "MVP-Phase-2-Billing"
dependencies: ["#017"]
parallel_group: "group-6-payment"
estimated_effort: "L"
---

# Issue #018: Stripe 결제 게이트웨이 연동 및 Webhook 처리

## 📋 개요
Stripe를 통한 온라인 결제 기능을 구현하고, Webhook을 통해 결제 결과를 실시간으로 수신하여 인보이스를 업데이트한다.

## 🎯 목표
- Stripe 결제 연동
- Checkout Session 생성
- Webhook 처리
- 결제 기록 관리

## 📝 상세 작업 내역

### 1. Stripe 설정
- [ ] Stripe Java SDK 의존성 추가
- [ ] API Key 설정 (Secrets Manager)
- [ ] Test Mode / Live Mode 분리

### 2. Checkout Session
- [ ] StripeService 구현 (Checkout Session 생성)
- [ ] POST `/api/v1/invoices/{id}/checkout` API 구현
- [ ] Session 정보 저장 (Payment Entity)

### 3. Webhook 처리
- [ ] StripeWebhookController (`POST /api/v1/webhooks/stripe`)
- [ ] WebhookSignatureValidator (Stripe Signature 검증)
- [ ] Event Handler (Event Type별 분기):
  - `checkout.session.completed`: 결제 완료
  - `payment_intent.succeeded`: 결제 성공
  - `payment_intent.payment_failed`: 결제 실패
  - `charge.refunded`: 환불
- [ ] PaymentService (결제 기록 저장 및 인보이스 업데이트)

### 4. 데이터 모델
- [ ] Payment, StripeWebhookLog Entity 생성
- [ ] Repository 생성
- [ ] 멱등성 처리 (event_id 기준)

### 5. 환불 처리
- [ ] POST `/api/v1/payments/{id}/refund` API 구현
- [ ] 부분 환불 및 전체 환불 지원
- [ ] 환불 이유 기록

### 6. 조회 API
- [ ] GET `/api/v1/payments` (결제 내역 조회)
- [ ] 필터링 (상태, 날짜 범위)

### 7. 테스트
- [ ] Stripe Test Mode 활용
- [ ] Webhook 시뮬레이션 테스트
- [ ] 통합 테스트

## ✅ 완료 조건
- [ ] 사용자가 인보이스 결제 링크를 통해 온라인 결제 가능
- [ ] 결제 완료 시 인보이스 상태가 자동으로 PAID로 변경
- [ ] 결제 실패 시 사용자에게 알림 발송
- [ ] 환불 처리 가능
- [ ] 결제 내역 조회 가능

## 📚 참고 문서
- SRS: `docs/GPT-SRS-v02.md` (Section 3.1 External Systems - Stripe)
- Task Spec: `tasks/functional/EPIC3-BE-003_Payment_Gateway.md`
- REQ-IDs: REQ-FUNC-022

## 🔗 의존성
**선행 작업**: #017 (Invoice Generation)
**후행 작업**: #009 (Billing UI - API 연동)
**병렬 가능**: 없음

## ⚠️ 주의사항
- Webhook Signature 검증 필수 (보안)
- Webhook은 멱등성 보장 (중복 요청 처리)
- 결제 정보는 PCI-DSS 준수 (카드 정보 직접 저장 금지)
- Webhook 엔드포인트가 공개되므로 Rate Limiting 필수

## 🏷️ 라벨
`backend`, `billing`, `payment`, `stripe`, `webhook`, `priority:must`, `size:L`

