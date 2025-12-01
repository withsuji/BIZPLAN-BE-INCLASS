---
issue_number: 017
epic: EPIC-3
task_id: EPIC3-BE-002
title: "[BE] 인보이스 자동 생성 및 상태 관리"
labels: ["backend", "billing", "invoice", "pdf", "priority:must", "size:XL"]
assignees: []
milestone: "MVP-Phase-3-Billing-Complete"
dependencies: ["#014"]
parallel_group: "group-4-invoice"
estimated_effort: "XL"
difficulty: "상"
estimated_duration: "8일"
---

# Issue #017: 인보이스 자동 생성 및 상태 관리

## 📋 개요
시간 기록(Time Entry)을 기반으로 인보이스를 자동 생성하고, 발송부터 결제 완료까지의 생명주기를 관리한다.

## 🎯 목표
- 주간/월간 자동 인보이스 생성
- 상태 관리 (Draft → Sent → Paid)
- PDF 생성 및 이메일 발송
- 미수금 Overdue 자동 처리

---

## 📝 상세 요구사항

### 1. 인보이스 상태 흐름

```
DRAFT ──────► SENT ──────► PAID
   │            │
   ▼            ▼
CANCELLED   OVERDUE
```

### 2. 인보이스 번호 형식
- `INV-{YYYYMM}-{SEQUENCE}`
- 예: `INV-202512-001`

### 3. 생성 주기
| 주기 | 실행 시점 | 대상 |
|------|----------|------|
| 주간 | 매주 일요일 | 지난 주 시간 기록 |
| 월간 | 매월 1일 | 지난 달 시간 기록 |

---

## 🔧 구현 가이드

### 데이터 모델

```sql
CREATE TABLE invoices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_number VARCHAR(50) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    client_id BIGINT NOT NULL,
    project_id BIGINT,
    issue_date DATE NOT NULL,
    due_date DATE NOT NULL,
    status ENUM('DRAFT', 'SENT', 'PAID', 'OVERDUE', 'CANCELLED') DEFAULT 'DRAFT',
    subtotal DECIMAL(12,2) NOT NULL,
    tax_rate DECIMAL(5,4) DEFAULT 0.10,
    tax_amount DECIMAL(12,2),
    discount_amount DECIMAL(12,2) DEFAULT 0,
    total_amount DECIMAL(12,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD',
    notes TEXT,
    payment_method ENUM('BANK_TRANSFER', 'CREDIT_CARD', 'STRIPE'),
    sent_at TIMESTAMP,
    paid_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (client_id) REFERENCES clients(id),
    INDEX idx_status_due (status, due_date)
);

CREATE TABLE invoice_line_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_id BIGINT NOT NULL,
    time_entry_id BIGINT NOT NULL,
    date DATE NOT NULL,
    description TEXT,
    hours DECIMAL(5,2) NOT NULL,
    rate DECIMAL(10,2) NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (invoice_id) REFERENCES invoices(id),
    FOREIGN KEY (time_entry_id) REFERENCES time_entries(id)
);

CREATE TABLE invoice_send_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_id BIGINT NOT NULL,
    recipient_email VARCHAR(255) NOT NULL,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ENUM('SUCCESS', 'FAILED') NOT NULL,
    error_message TEXT,
    FOREIGN KEY (invoice_id) REFERENCES invoices(id)
);
```

### 핵심 서비스

```java
@Service
@RequiredArgsConstructor
@Transactional
public class InvoiceService {
    
    private final InvoiceRepository invoiceRepository;
    private final TimeEntryRepository timeEntryRepository;
    private final InvoiceLineItemRepository lineItemRepository;
    private final InvoiceNumberGenerator numberGenerator;
    private final InvoicePdfGenerator pdfGenerator;
    private final EmailService emailService;
    
    /**
     * 인보이스 자동 생성
     */
    public Invoice generateInvoice(Long userId, Long clientId, LocalDate startDate, LocalDate endDate) {
        // 청구 가능한 시간 기록 조회
        List<TimeEntry> entries = timeEntryRepository.findBillableEntriesForInvoice(
            userId, clientId, startDate, endDate
        );
        
        if (entries.isEmpty()) {
            throw new IllegalStateException("No billable entries found");
        }
        
        // 금액 계산
        BigDecimal subtotal = entries.stream()
            .map(TimeEntry::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal taxRate = new BigDecimal("0.10"); // 10%
        BigDecimal taxAmount = subtotal.multiply(taxRate);
        BigDecimal total = subtotal.add(taxAmount);
        
        // 인보이스 생성
        Invoice invoice = Invoice.builder()
            .invoiceNumber(numberGenerator.generate())
            .userId(userId)
            .clientId(clientId)
            .issueDate(LocalDate.now())
            .dueDate(LocalDate.now().plusDays(30)) // NET 30
            .subtotal(subtotal)
            .taxRate(taxRate)
            .taxAmount(taxAmount)
            .totalAmount(total)
            .currency("USD")
            .status(InvoiceStatus.DRAFT)
            .build();
        
        invoice = invoiceRepository.save(invoice);
        
        // Line Items 생성
        for (TimeEntry entry : entries) {
            InvoiceLineItem lineItem = InvoiceLineItem.builder()
                .invoiceId(invoice.getId())
                .timeEntryId(entry.getId())
                .date(entry.getDate())
                .description(entry.getDescription())
                .hours(entry.getDurationHours())
                .rate(entry.getRate())
                .amount(entry.getAmount())
                .build();
            
            lineItemRepository.save(lineItem);
            
            // TimeEntry에 invoice_id 연결
            entry.setInvoiceId(invoice.getId());
            timeEntryRepository.save(entry);
        }
        
        return invoice;
    }
    
    /**
     * 인보이스 발송
     */
    public void sendInvoice(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
            .orElseThrow(() -> new NotFoundException("Invoice not found"));
        
        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new IllegalStateException("Only draft invoices can be sent");
        }
        
        // PDF 생성
        byte[] pdfBytes = pdfGenerator.generate(invoice);
        
        // 이메일 발송
        Client client = invoice.getClient();
        try {
            emailService.sendInvoice(
                client.getContactEmail(),
                invoice.getInvoiceNumber(),
                pdfBytes
            );
            
            invoice.setStatus(InvoiceStatus.SENT);
            invoice.setSentAt(LocalDateTime.now());
            invoiceRepository.save(invoice);
            
            logSendAttempt(invoice, client.getContactEmail(), true, null);
            
        } catch (Exception e) {
            logSendAttempt(invoice, client.getContactEmail(), false, e.getMessage());
            throw new InvoiceSendException("Failed to send invoice", e);
        }
    }
    
    /**
     * 결제 완료 처리
     */
    public void markAsPaid(Long invoiceId, LocalDateTime paidAt) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
            .orElseThrow(() -> new NotFoundException("Invoice not found"));
        
        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaidAt(paidAt);
        invoiceRepository.save(invoice);
    }
}
```

### PDF 생성기

```java
@Component
public class InvoicePdfGenerator {
    
    public byte[] generate(Invoice invoice) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            document.open();
            
            // 헤더
            addHeader(document, invoice);
            
            // 발행자/수신자 정보
            addPartyInfo(document, invoice);
            
            // Line Items 테이블
            addLineItemsTable(document, invoice.getLineItems());
            
            // 합계
            addTotals(document, invoice);
            
            // 결제 정보
            addPaymentInfo(document, invoice);
            
            document.close();
            return baos.toByteArray();
            
        } catch (Exception e) {
            throw new PdfGenerationException("Failed to generate PDF", e);
        }
    }
    
    private void addLineItemsTable(Document document, List<InvoiceLineItem> items) {
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        
        // 헤더
        table.addCell("Date");
        table.addCell("Description");
        table.addCell("Hours");
        table.addCell("Rate");
        table.addCell("Amount");
        
        // 데이터
        for (InvoiceLineItem item : items) {
            table.addCell(item.getDate().toString());
            table.addCell(item.getDescription());
            table.addCell(item.getHours().toString());
            table.addCell(formatCurrency(item.getRate()));
            table.addCell(formatCurrency(item.getAmount()));
        }
        
        document.add(table);
    }
}
```

### 스케줄러

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class InvoiceScheduler {
    
    private final InvoiceService invoiceService;
    private final InvoiceRepository invoiceRepository;
    
    @Scheduled(cron = "0 0 0 1 * *") // 매월 1일 자정
    public void generateMonthlyInvoices() {
        LocalDate lastMonth = LocalDate.now().minusMonths(1);
        LocalDate start = lastMonth.withDayOfMonth(1);
        LocalDate end = lastMonth.withDayOfMonth(lastMonth.lengthOfMonth());
        
        // 자동 생성 대상 사용자/클라이언트 조회 및 생성
        log.info("Starting monthly invoice generation for {}", lastMonth);
        // ...
    }
    
    @Scheduled(cron = "0 0 9 * * *") // 매일 오전 9시
    public void markOverdueInvoices() {
        LocalDate today = LocalDate.now();
        int updated = invoiceRepository.markOverdue(today);
        log.info("Marked {} invoices as overdue", updated);
    }
}
```

---

## ✅ 완료 조건

| # | 조건 | 검증 방법 |
|---|------|----------|
| AC-1 | 인보이스 생성 | 시간 기록 기반 인보이스 생성 |
| AC-2 | 인보이스 번호 생성 | 형식 준수 및 유니크 |
| AC-3 | PDF 생성 | 다운로드 가능한 PDF |
| AC-4 | 이메일 발송 | PDF 첨부 이메일 발송 |
| AC-5 | 상태 전이 | Draft → Sent → Paid |
| AC-6 | Overdue 처리 | 기한 경과 시 자동 변경 |
| AC-7 | 월간 자동 생성 | 스케줄러 동작 확인 |

---

## 🔗 의존성

**선행 작업**: #014 (Time Tracking)  
**후행 작업**: #018 (Payment)  
**병렬 가능**: #016 (Slot Algorithm)

---

## 🏷️ 라벨
`backend`, `billing`, `invoice`, `pdf`, `priority:must`, `size:XL`

---

## 📚 참고 문서
- Task Spec: `tasks/functional/EPIC3-BE-002_Invoice_Generation.md`
- REQ-IDs: REQ-FUNC-021
