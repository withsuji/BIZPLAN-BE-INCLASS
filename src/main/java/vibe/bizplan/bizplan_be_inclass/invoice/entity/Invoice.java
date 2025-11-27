package vibe.bizplan.bizplan_be_inclass.invoice.entity;

import jakarta.persistence.*;
import lombok.*;
import vibe.bizplan.bizplan_be_inclass.common.entity.BaseEntity;
import vibe.bizplan.bizplan_be_inclass.customer.entity.Customer;
import vibe.bizplan.bizplan_be_inclass.team.entity.Team;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Invoice entity for billing.
 * REQ-FUNC-021: Automatic invoice generation at billing cycle deadline
 */
@Entity
@Table(
    name = "invoices",
    indexes = {
        @Index(name = "idx_invoices_customer_status", columnList = "customer_id, status, due_date"),
        @Index(name = "idx_invoices_status", columnList = "status")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Invoice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "invoice_number", nullable = false, unique = true, length = 50)
    private String invoiceNumber;

    @Column(precision = 12, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(nullable = false, length = 3)
    @Builder.Default
    private String currency = "USD";

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private InvoiceStatus status = InvoiceStatus.DRAFT;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(columnDefinition = "TEXT")
    private String notes;

    // Business Methods
    public void send() {
        if (this.status != InvoiceStatus.DRAFT) {
            throw new IllegalStateException("Only draft invoices can be sent");
        }
        this.status = InvoiceStatus.SENT;
        this.sentAt = LocalDateTime.now();
    }

    public void markAsPaid() {
        if (this.status == InvoiceStatus.PAID) {
            return; // Already paid
        }
        this.status = InvoiceStatus.PAID;
        this.paidAt = LocalDateTime.now();
    }

    public void markAsOverdue() {
        if (this.status == InvoiceStatus.PAID) {
            throw new IllegalStateException("Paid invoices cannot be marked as overdue");
        }
        this.status = InvoiceStatus.OVERDUE;
    }

    public boolean isOverdue() {
        if (this.status == InvoiceStatus.PAID) {
            return false;
        }
        return LocalDate.now().isAfter(this.dueDate);
    }

    public boolean isPaid() {
        return this.status == InvoiceStatus.PAID;
    }

    public boolean isDraft() {
        return this.status == InvoiceStatus.DRAFT;
    }

    public void updateAmount(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        this.amount = amount;
    }

    public void updateDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public void updateNotes(String notes) {
        this.notes = notes;
    }
}

