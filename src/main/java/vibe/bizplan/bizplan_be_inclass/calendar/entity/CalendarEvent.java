package vibe.bizplan.bizplan_be_inclass.calendar.entity;

import jakarta.persistence.*;
import lombok.*;
import vibe.bizplan.bizplan_be_inclass.common.entity.BaseEntity;
import vibe.bizplan.bizplan_be_inclass.project.entity.Project;
import vibe.bizplan.bizplan_be_inclass.team.entity.Team;
import vibe.bizplan.bizplan_be_inclass.user.entity.User;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Calendar event entity for scheduling.
 * REQ-FUNC-001: Timezone-based available slot calculation
 */
@Entity
@Table(
    name = "calendar_events",
    indexes = {
        @Index(name = "idx_events_team_start", columnList = "team_id, start_at"),
        @Index(name = "idx_events_user_id", columnList = "user_id"),
        @Index(name = "idx_events_status", columnList = "status")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CalendarEvent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", unique = true, length = 255)
    private String externalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(length = 500)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Column(nullable = false, length = 64)
    @Builder.Default
    private String timezone = "UTC";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EventStatus status = EventStatus.TENTATIVE;

    // Business Methods
    public int calculateDurationMinutes() {
        return (int) Duration.between(startAt, endAt).toMinutes();
    }

    public void confirm() {
        this.status = EventStatus.CONFIRMED;
    }

    public void cancel() {
        this.status = EventStatus.CANCELED;
    }

    public boolean isCompleted() {
        return this.status == EventStatus.CONFIRMED 
            && this.endAt.isBefore(LocalDateTime.now());
    }

    public boolean isConfirmed() {
        return this.status == EventStatus.CONFIRMED;
    }

    public boolean isCanceled() {
        return this.status == EventStatus.CANCELED;
    }

    public void updateTitle(String title) {
        this.title = title;
    }

    public void updateDescription(String description) {
        this.description = description;
    }

    public void updateTime(LocalDateTime startAt, LocalDateTime endAt) {
        if (endAt.isBefore(startAt) || endAt.isEqual(startAt)) {
            throw new IllegalArgumentException("End time must be after start time");
        }
        this.startAt = startAt;
        this.endAt = endAt;
    }

    public void assignProject(Project project) {
        this.project = project;
    }
}

