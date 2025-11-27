package vibe.bizplan.bizplan_be_inclass.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vibe.bizplan.bizplan_be_inclass.payment.entity.PaymentReminder;
import vibe.bizplan.bizplan_be_inclass.payment.entity.ReminderStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentReminderRepository extends JpaRepository<PaymentReminder, Long> {

    List<PaymentReminder> findByInvoiceId(Long invoiceId);

    List<PaymentReminder> findByInvoiceIdAndStatus(Long invoiceId, ReminderStatus status);

    @Query("""
        SELECT r FROM PaymentReminder r
        WHERE r.status = 'SCHEDULED'
          AND r.scheduledAt <= :now
        ORDER BY r.scheduledAt ASC
    """)
    List<PaymentReminder> findDueReminders(@Param("now") LocalDateTime now);

    @Query("""
        SELECT COUNT(r) FROM PaymentReminder r
        WHERE r.invoice.id = :invoiceId
          AND r.status = 'SENT'
    """)
    long countSentRemindersByInvoiceId(@Param("invoiceId") Long invoiceId);

    boolean existsByInvoiceIdAndStatus(Long invoiceId, ReminderStatus status);
}

