package vibe.bizplan.bizplan_be_inclass.invoice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vibe.bizplan.bizplan_be_inclass.invoice.entity.Invoice;
import vibe.bizplan.bizplan_be_inclass.invoice.entity.InvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    boolean existsByInvoiceNumber(String invoiceNumber);

    List<Invoice> findByTeamId(Long teamId);

    List<Invoice> findByTeamIdAndStatus(Long teamId, InvoiceStatus status);

    List<Invoice> findByCustomerId(Long customerId);

    List<Invoice> findByCustomerIdAndStatus(Long customerId, InvoiceStatus status);

    @Query("""
        SELECT i FROM Invoice i
        WHERE i.status = 'SENT'
          AND i.dueDate < :today
    """)
    List<Invoice> findOverdueInvoices(@Param("today") LocalDate today);

    @Query("""
        SELECT SUM(i.amount) FROM Invoice i
        WHERE i.team.id = :teamId
          AND i.status = :status
    """)
    BigDecimal sumAmountByTeamAndStatus(
            @Param("teamId") Long teamId,
            @Param("status") InvoiceStatus status
    );

    @Query("""
        SELECT i FROM Invoice i
        WHERE i.team.id = :teamId
        ORDER BY i.createdAt DESC
    """)
    List<Invoice> findByTeamIdOrderByCreatedAtDesc(@Param("teamId") Long teamId);

    long countByTeamIdAndStatus(Long teamId, InvoiceStatus status);
}

