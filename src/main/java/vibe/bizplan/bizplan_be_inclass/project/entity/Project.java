package vibe.bizplan.bizplan_be_inclass.project.entity;

import jakarta.persistence.*;
import lombok.*;
import vibe.bizplan.bizplan_be_inclass.common.entity.BaseEntity;
import vibe.bizplan.bizplan_be_inclass.team.entity.Team;

import java.math.BigDecimal;

/**
 * Project entity for organizing work and billing.
 */
@Entity
@Table(
    name = "projects",
    indexes = {
        @Index(name = "idx_projects_team_id", columnList = "team_id"),
        @Index(name = "idx_projects_status", columnList = "status")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Project extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "default_rate", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal defaultRate = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ProjectStatus status = ProjectStatus.ACTIVE;

    // Business Methods
    public void updateName(String name) {
        this.name = name;
    }

    public void updateDescription(String description) {
        this.description = description;
    }

    public void updateDefaultRate(BigDecimal defaultRate) {
        this.defaultRate = defaultRate;
    }

    public void archive() {
        this.status = ProjectStatus.ARCHIVED;
    }

    public void activate() {
        this.status = ProjectStatus.ACTIVE;
    }

    public boolean isActive() {
        return this.status == ProjectStatus.ACTIVE;
    }
}

