package vibe.bizplan.bizplan_be_inclass.team.entity;

import jakarta.persistence.*;
import lombok.*;
import vibe.bizplan.bizplan_be_inclass.common.entity.BaseEntity;

/**
 * Team entity representing an organization or group.
 */
@Entity
@Table(name = "teams")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Team extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "business_hours", columnDefinition = "JSON")
    private String businessHours;

    @Column(columnDefinition = "JSON")
    private String holidays;

    // Business Methods
    public void updateName(String name) {
        this.name = name;
    }

    public void updateBusinessHours(String businessHours) {
        this.businessHours = businessHours;
    }

    public void updateHolidays(String holidays) {
        this.holidays = holidays;
    }
}

