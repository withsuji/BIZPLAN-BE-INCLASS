package vibe.bizplan.bizplan_be_inclass.timetracking.entity;

import jakarta.persistence.*;
import lombok.*;
import vibe.bizplan.bizplan_be_inclass.calendar.entity.CalendarEvent;
import vibe.bizplan.bizplan_be_inclass.common.entity.BaseEntity;
import vibe.bizplan.bizplan_be_inclass.project.entity.Project;
import vibe.bizplan.bizplan_be_inclass.user.entity.User;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Time entry entity for tracking billable hours.
 * REQ-FUNC-020: Automatic time entry generation from calendar events
 */
@Entity
@Table(
    name = "time_entries",
    indexes = {
        @Index(name = "idx_time_entries_user", columnList = "user_id"),
        @Index(name = "idx_time_entries_project", columnList = "project_id"),
        @Index(name = "idx_time_entries_created", columnList = "created_at")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TimeEntry extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", unique = true)
    private CalendarEvent event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(length = 500)
    private String description;

    @Column(name = "duration_min", nullable = false)
    private Integer durationMin;

    @Column(precision = 10, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal rate = BigDecimal.ZERO;

    @Column(nullable = false)
    @Builder.Default
    private Boolean billable = true;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    // Factory Method: Create from CalendarEvent
    public static TimeEntry fromEvent(CalendarEvent event, BigDecimal rate) {
        return TimeEntry.builder()
                .event(event)
                .user(event.getUser())
                .project(event.getProject())
                .description(event.getTitle())
                .durationMin(event.calculateDurationMinutes())
                .rate(rate != null ? rate : BigDecimal.ZERO)
                .billable(true)
                .build();
    }

    // Business Methods
    public BigDecimal calculateAmount() {
        if (!billable || rate == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal hours = BigDecimal.valueOf(durationMin)
                .divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP);
        return hours.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }

    public void approve() {
        this.approvedAt = LocalDateTime.now();
    }

    public void unapprove() {
        this.approvedAt = null;
    }

    public boolean isApproved() {
        return this.approvedAt != null;
    }

    public void updateDuration(Integer durationMin) {
        if (durationMin <= 0) {
            throw new IllegalArgumentException("Duration must be positive");
        }
        this.durationMin = durationMin;
    }

    public void updateRate(BigDecimal rate) {
        this.rate = rate != null ? rate : BigDecimal.ZERO;
    }

    public void updateBillable(Boolean billable) {
        this.billable = billable != null ? billable : true;
    }

    public void updateDescription(String description) {
        this.description = description;
    }
}

