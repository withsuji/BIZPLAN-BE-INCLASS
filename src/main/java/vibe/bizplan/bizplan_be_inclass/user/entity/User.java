package vibe.bizplan.bizplan_be_inclass.user.entity;

import jakarta.persistence.*;
import lombok.*;
import vibe.bizplan.bizplan_be_inclass.common.entity.BaseEntity;
import vibe.bizplan.bizplan_be_inclass.team.entity.Team;

/**
 * User entity representing a system user.
 */
@Entity
@Table(
    name = "users",
    indexes = {
        @Index(name = "idx_users_team_id", columnList = "team_id"),
        @Index(name = "idx_users_email", columnList = "email")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserRole role = UserRole.MEMBER;

    @Column(nullable = false, length = 64)
    @Builder.Default
    private String timezone = "UTC";

    @Column(columnDefinition = "JSON")
    private String preferences;

    // Business Methods
    public void updateRole(UserRole role) {
        this.role = role;
    }

    public void updateTimezone(String timezone) {
        this.timezone = timezone;
    }

    public void updatePreferences(String preferences) {
        this.preferences = preferences;
    }

    public boolean isOwner() {
        return this.role == UserRole.OWNER;
    }

    public boolean canEdit() {
        return this.role == UserRole.OWNER || this.role == UserRole.MEMBER;
    }
}

