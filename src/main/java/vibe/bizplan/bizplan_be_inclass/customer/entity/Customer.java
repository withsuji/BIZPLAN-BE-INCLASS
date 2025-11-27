package vibe.bizplan.bizplan_be_inclass.customer.entity;

import jakarta.persistence.*;
import lombok.*;
import vibe.bizplan.bizplan_be_inclass.common.entity.BaseEntity;
import vibe.bizplan.bizplan_be_inclass.team.entity.Team;

/**
 * Customer entity for billing purposes.
 */
@Entity
@Table(
    name = "customers",
    indexes = {
        @Index(name = "idx_customers_team", columnList = "team_id"),
        @Index(name = "idx_customers_email", columnList = "email")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Customer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(name = "billing_address", length = 500)
    private String billingAddress;

    // Business Methods
    public void updateName(String name) {
        this.name = name;
    }

    public void updateEmail(String email) {
        this.email = email;
    }

    public void updateBillingAddress(String billingAddress) {
        this.billingAddress = billingAddress;
    }
}

