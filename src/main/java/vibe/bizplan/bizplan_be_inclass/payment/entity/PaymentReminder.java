package vibe.bizplan.bizplan_be_inclass.payment.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import vibe.bizplan.bizplan_be_inclass.invoice.entity.Invoice;

import java.time.LocalDateTime;

/**
 * Payment reminder entity for dunning process.
 * REQ-FUNC-022: Overdue payment reminder flow
 */
@Entity
@Table(
    name = "payment_reminders",
    indexes = {
        @Index(name = "idx_reminders_invoice", columnList = "invoice_id"),
        @Index(name = "idx_reminders_status", columnList = "status"),
        @Index(name = "idx_reminders_scheduled", columnList = "scheduled_at")
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PaymentReminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReminderChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ReminderStatus status = ReminderStatus.SCHEDULED;

    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Business Methods
    public void markAsSent() {
        this.status = ReminderStatus.SENT;
        this.sentAt = LocalDateTime.now();
    }

    public void markAsFailed(String errorMessage) {
        this.status = ReminderStatus.FAILED;
        this.errorMessage = errorMessage;
    }

    public boolean isScheduled() {
        return this.status == ReminderStatus.SCHEDULED;
    }

    public boolean isSent() {
        return this.status == ReminderStatus.SENT;
    }

    public boolean isFailed() {
        return this.status == ReminderStatus.FAILED;
    }

    public boolean isDue() {
        return this.status == ReminderStatus.SCHEDULED 
            && LocalDateTime.now().isAfter(this.scheduledAt);
    }
}

