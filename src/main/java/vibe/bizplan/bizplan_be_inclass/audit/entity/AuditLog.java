package vibe.bizplan.bizplan_be_inclass.audit.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import vibe.bizplan.bizplan_be_inclass.user.entity.User;

import java.time.LocalDateTime;

/**
 * Audit log entity for tracking changes (append-only).
 * REQ-NF-009: Audit log retention >= 1 year, append-only
 */
@Entity
@Table(
    name = "audit_logs",
    indexes = {
        @Index(name = "idx_audit_actor", columnList = "actor_id"),
        @Index(name = "idx_audit_target", columnList = "target_type, target_id"),
        @Index(name = "idx_audit_created", columnList = "created_at"),
        @Index(name = "idx_audit_action", columnList = "action")
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private User actor;

    @Column(nullable = false, length = 255)
    private String action;

    @Column(name = "target_type", nullable = false, length = 64)
    private String targetType;

    @Column(name = "target_id", nullable = false, length = 64)
    private String targetId;

    @Column(name = "old_value", columnDefinition = "JSON")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "JSON")
    private String newValue;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(nullable = false, length = 255)
    private String hash;

    @Column(name = "prev_hash", length = 255)
    private String prevHash;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Factory Method
    public static AuditLog create(
            User actor,
            String action,
            String targetType,
            String targetId,
            String oldValue,
            String newValue,
            String ipAddress,
            String userAgent,
            String prevHash
    ) {
        AuditLog log = AuditLog.builder()
                .actor(actor)
                .action(action)
                .targetType(targetType)
                .targetId(targetId)
                .oldValue(oldValue)
                .newValue(newValue)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .prevHash(prevHash)
                .build();
        
        // Generate hash for integrity verification
        log.generateHash();
        return log;
    }

    private void generateHash() {
        String content = String.format("%s:%s:%s:%s:%s:%s",
                action, targetType, targetId, oldValue, newValue, prevHash);
        this.hash = String.valueOf(content.hashCode()); // Simplified; use SHA-256 in production
    }

    // Verify chain integrity
    public boolean verifyIntegrity(String expectedPrevHash) {
        return this.prevHash == null 
            ? expectedPrevHash == null 
            : this.prevHash.equals(expectedPrevHash);
    }
}

