package vibe.bizplan.bizplan_be_inclass.audit.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vibe.bizplan.bizplan_be_inclass.audit.entity.AuditLog;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByActorId(Long actorId);

    Page<AuditLog> findByActorId(Long actorId, Pageable pageable);

    List<AuditLog> findByTargetTypeAndTargetId(String targetType, String targetId);

    @Query("""
        SELECT a FROM AuditLog a
        WHERE a.createdAt BETWEEN :start AND :end
        ORDER BY a.createdAt DESC
    """)
    List<AuditLog> findByDateRange(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
        SELECT a FROM AuditLog a
        WHERE a.action = :action
        ORDER BY a.createdAt DESC
    """)
    List<AuditLog> findByAction(@Param("action") String action);

    @Query("SELECT a FROM AuditLog a ORDER BY a.id DESC LIMIT 1")
    Optional<AuditLog> findLatest();

    Page<AuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
}

