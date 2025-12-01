---
issue_number: 018
epic: EPIC-3
task_id: EPIC3-BE-003
title: "[BE] Stripe 결제 게이트웨이 연동 및 Webhook 처리"
labels: ["backend", "billing", "payment", "stripe", "priority:must", "size:L"]
assignees: []
milestone: "MVP-Phase-3-Billing-Complete"
dependencies: ["#017"]
parallel_group: "group-5-payment"
estimated_effort: "L"
difficulty: "중"
estimated_duration: "5일"
---

# Issue #018: Stripe 결제 게이트웨이 연동 및 Webhook 처리

## 📋 개요
Stripe 결제 게이트웨이를 연동하여 인보이스 온라인 결제 기능을 제공하고, Webhook을 통해 결제 결과를 실시간으로 수신하여 처리한다.

## 🎯 목표
- Stripe Checkout Session 생성
- 결제 Webhook 처리
- 결제 상태 관리
- 환불 처리

---

## 📝 상세 요구사항

### 1. 결제 플로우

```
1. 사용자 결제 링크 클릭
2. POST /api/v1/invoices/{id}/checkout
3. Stripe Checkout Session 생성
4. Stripe 결제 페이지 리다이렉트
5. 결제 완료
6. Webhook: checkout.session.completed
7. 인보이스 상태 → PAID
8. Success URL로 리다이렉트
```

### 2. Webhook 이벤트

| 이벤트 | 처리 |
|--------|------|
| `checkout.session.completed` | 결제 완료, PAID 상태 변경 |
| `payment_intent.payment_failed` | 결제 실패 알림 |
| `charge.refunded` | 환불 처리 |

### 3. 보안 요구사항
- Webhook Signature 검증 필수
- 멱등성 보장 (중복 요청 처리)
- PCI-DSS 준수 (카드 정보 비저장)

---

## 🔧 구현 가이드

### 의존성 추가

```groovy
implementation 'com.stripe:stripe-java:24.0.0'
```

### 데이터 모델

```sql
CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_id BIGINT NOT NULL,
    stripe_payment_intent_id VARCHAR(255) UNIQUE,
    stripe_checkout_session_id VARCHAR(255),
    amount DECIMAL(12,2) NOT NULL,
    fee DECIMAL(10,2),
    net_amount DECIMAL(12,2),
    currency VARCHAR(3) NOT NULL,
    payment_method ENUM('CARD', 'BANK_TRANSFER'),
    card_last4 VARCHAR(4),
    card_brand VARCHAR(20),
    status ENUM('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED') DEFAULT 'PENDING',
    paid_at TIMESTAMP,
    refunded_at TIMESTAMP,
    refund_reason TEXT,
    metadata JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (invoice_id) REFERENCES invoices(id)
);

CREATE TABLE stripe_webhook_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id VARCHAR(255) UNIQUE NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload JSON,
    processed BOOLEAN DEFAULT FALSE,
    processed_at TIMESTAMP,
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Stripe Service

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class StripeService {
    
    @Value("${stripe.api-key}")
    private String apiKey;
    
    @Value("${stripe.webhook-secret}")
    private String webhookSecret;
    
    @Value("${app.base-url}")
    private String baseUrl;
    
    private final PaymentRepository paymentRepository;
    private final InvoiceService invoiceService;
    
    @PostConstruct
    public void init() {
        Stripe.apiKey = apiKey;
    }
    
    /**
     * Checkout Session 생성
     */
    public String createCheckoutSession(Invoice invoice) throws StripeException {
        SessionCreateParams params = SessionCreateParams.builder()
            .setMode(SessionCreateParams.Mode.PAYMENT)
            .setSuccessUrl(baseUrl + "/payment/success?session_id={CHECKOUT_SESSION_ID}")
            .setCancelUrl(baseUrl + "/payment/cancel")
            .addLineItem(
                SessionCreateParams.LineItem.builder()
                    .setPriceData(
                        SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency(invoice.getCurrency().toLowerCase())
                            .setUnitAmount(invoice.getTotalAmount()
                                .multiply(new BigDecimal("100"))
                                .longValue()) // cents
                            .setProductData(
                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                    .setName("Invoice " + invoice.getInvoiceNumber())
                                    .build()
                            )
                            .build()
                    )
                    .setQuantity(1L)
                    .build()
            )
            .putMetadata("invoice_id", invoice.getId().toString())
            .build();
        
        Session session = Session.create(params);
        
        // Payment 레코드 생성
        Payment payment = Payment.builder()
            .invoiceId(invoice.getId())
            .stripeCheckoutSessionId(session.getId())
            .amount(invoice.getTotalAmount())
            .currency(invoice.getCurrency())
            .status(PaymentStatus.PENDING)
            .build();
        
        paymentRepository.save(payment);
        
        return session.getUrl();
    }
    
    /**
     * Webhook 이벤트 처리
     */
    @Transactional
    public void handleWebhook(String payload, String sigHeader) {
        Event event;
        
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.error("Invalid Stripe signature");
            throw new InvalidSignatureException("Invalid webhook signature");
        }
        
        // 멱등성 체크
        if (isEventProcessed(event.getId())) {
            log.info("Event already processed: {}", event.getId());
            return;
        }
        
        // 이벤트 로깅
        logWebhookEvent(event);
        
        try {
            switch (event.getType()) {
                case "checkout.session.completed" -> handleCheckoutCompleted(event);
                case "payment_intent.payment_failed" -> handlePaymentFailed(event);
                case "charge.refunded" -> handleRefund(event);
                default -> log.info("Unhandled event type: {}", event.getType());
            }
            
            markEventProcessed(event.getId());
            
        } catch (Exception e) {
            log.error("Error processing webhook event: {}", event.getId(), e);
            markEventFailed(event.getId(), e.getMessage());
            throw e;
        }
    }
    
    private void handleCheckoutCompleted(Event event) {
        Session session = (Session) event.getDataObjectDeserializer()
            .getObject().orElseThrow();
        
        String invoiceId = session.getMetadata().get("invoice_id");
        PaymentIntent paymentIntent = PaymentIntent.retrieve(session.getPaymentIntent());
        
        // Payment 업데이트
        Payment payment = paymentRepository.findByStripeCheckoutSessionId(session.getId())
            .orElseThrow(() -> new NotFoundException("Payment not found"));
        
        payment.setStripePaymentIntentId(paymentIntent.getId());
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaidAt(LocalDateTime.now());
        
        // 카드 정보 저장 (마지막 4자리만)
        if (paymentIntent.getPaymentMethod() != null) {
            PaymentMethod pm = PaymentMethod.retrieve(paymentIntent.getPaymentMethod());
            if (pm.getCard() != null) {
                payment.setCardLast4(pm.getCard().getLast4());
                payment.setCardBrand(pm.getCard().getBrand());
            }
        }
        
        // 수수료 계산 (Stripe: 2.9% + $0.30)
        BigDecimal fee = payment.getAmount()
            .multiply(new BigDecimal("0.029"))
            .add(new BigDecimal("0.30"));
        payment.setFee(fee);
        payment.setNetAmount(payment.getAmount().subtract(fee));
        
        paymentRepository.save(payment);
        
        // 인보이스 상태 업데이트
        invoiceService.markAsPaid(Long.valueOf(invoiceId), LocalDateTime.now());
        
        log.info("Payment completed for invoice: {}", invoiceId);
    }
    
    private void handlePaymentFailed(Event event) {
        PaymentIntent intent = (PaymentIntent) event.getDataObjectDeserializer()
            .getObject().orElseThrow();
        
        Payment payment = paymentRepository.findByStripePaymentIntentId(intent.getId())
            .orElse(null);
        
        if (payment != null) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
        }
        
        log.warn("Payment failed: {}", intent.getId());
    }
    
    /**
     * 환불 처리
     */
    public void refund(Long paymentId, String reason) throws StripeException {
        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new NotFoundException("Payment not found"));
        
        RefundCreateParams params = RefundCreateParams.builder()
            .setPaymentIntent(payment.getStripePaymentIntentId())
            .build();
        
        Refund refund = Refund.create(params);
        
        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setRefundedAt(LocalDateTime.now());
        payment.setRefundReason(reason);
        paymentRepository.save(payment);
        
        log.info("Payment refunded: {}", paymentId);
    }
}
```

### Webhook Controller

```java
@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookController {
    
    private final StripeService stripeService;
    
    @PostMapping("/stripe")
    public ResponseEntity<Void> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        
        try {
            stripeService.handleWebhook(payload, sigHeader);
            return ResponseEntity.ok().build();
        } catch (InvalidSignatureException e) {
            log.error("Invalid Stripe signature");
            return ResponseEntity.status(400).build();
        } catch (Exception e) {
            log.error("Webhook processing error", e);
            return ResponseEntity.ok().build(); // Always 200 to prevent retries
        }
    }
}
```

### Checkout Controller

```java
@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
public class InvoicePaymentController {
    
    private final InvoiceService invoiceService;
    private final StripeService stripeService;
    
    @PostMapping("/{id}/checkout")
    public ResponseEntity<Map<String, String>> createCheckout(@PathVariable Long id) {
        Invoice invoice = invoiceService.getInvoice(id);
        
        if (invoice.getStatus() != InvoiceStatus.SENT) {
            throw new IllegalStateException("Only sent invoices can be paid");
        }
        
        try {
            String checkoutUrl = stripeService.createCheckoutSession(invoice);
            return ResponseEntity.ok(Map.of("checkout_url", checkoutUrl));
        } catch (StripeException e) {
            throw new PaymentException("Failed to create checkout session", e);
        }
    }
}
```

---

## ✅ 완료 조건

| # | 조건 | 검증 방법 |
|---|------|----------|
| AC-1 | Checkout 생성 | POST /checkout → Stripe URL 반환 |
| AC-2 | 결제 완료 처리 | Webhook → 인보이스 PAID 상태 |
| AC-3 | Signature 검증 | 잘못된 서명 시 400 반환 |
| AC-4 | 멱등성 | 중복 Webhook 처리 무시 |
| AC-5 | 환불 처리 | Refund API 동작 |
| AC-6 | 결제 내역 조회 | GET /payments 동작 |
| AC-7 | Webhook 로깅 | 모든 이벤트 DB 기록 |

---

## 🔗 의존성

**선행 작업**: #017 (Invoice Generation)  
**후행 작업**: 없음  
**병렬 가능**: 없음 (마지막 단계)

---

## 🏷️ 라벨
`backend`, `billing`, `payment`, `stripe`, `priority:must`, `size:L`

---

## ⚠️ 주의사항

| 리스크 | 영향 | 완화 방안 |
|--------|------|----------|
| Webhook 누락 | 결제 상태 불일치 | Polling Fallback |
| Webhook 엔드포인트 공개 | DDoS 가능 | Rate Limiting |
| 결제 수수료 | 비용 증가 | 사용자에게 전가 또는 최소 금액 설정 |

---

## 📚 참고 문서
- Task Spec: `tasks/functional/EPIC3-BE-003_Payment_Gateway.md`
- REQ-IDs: REQ-FUNC-022
- [Stripe API 문서](https://stripe.com/docs/api)
- [Stripe Checkout](https://stripe.com/docs/payments/checkout)
