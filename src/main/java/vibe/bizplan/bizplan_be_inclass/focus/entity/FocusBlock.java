package vibe.bizplan.bizplan_be_inclass.focus.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import vibe.bizplan.bizplan_be_inclass.user.entity.User;

import java.time.LocalDateTime;

/**
 * Focus block entity for protecting focus time.
 * REQ-FUNC-002: Focus block preservation and conflict avoidance
 */
@Entity
@Table(
    name = "focus_blocks",
    indexes = {
        @Index(name = "idx_focus_user_time", columnList = "user_id, start_at, end_at")
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FocusBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "rule_id")
    private Long ruleId;

    @Column(length = 100)
    private String title;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Business Methods
    public boolean overlaps(LocalDateTime otherStart, LocalDateTime otherEnd) {
        return startAt.isBefore(otherEnd) && endAt.isAfter(otherStart);
    }

    public boolean isActive() {
        LocalDateTime now = LocalDateTime.now();
        return startAt.isBefore(now) && endAt.isAfter(now);
    }

    public void updateTime(LocalDateTime startAt, LocalDateTime endAt) {
        if (endAt.isBefore(startAt) || endAt.isEqual(startAt)) {
            throw new IllegalArgumentException("End time must be after start time");
        }
        this.startAt = startAt;
        this.endAt = endAt;
    }

    public void updateTitle(String title) {
        this.title = title;
    }
}

