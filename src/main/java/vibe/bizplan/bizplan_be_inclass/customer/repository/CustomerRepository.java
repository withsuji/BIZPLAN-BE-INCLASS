package vibe.bizplan.bizplan_be_inclass.customer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vibe.bizplan.bizplan_be_inclass.customer.entity.Customer;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    List<Customer> findByTeamId(Long teamId);

    List<Customer> findByTeamIdOrderByNameAsc(Long teamId);

    Optional<Customer> findByTeamIdAndEmail(Long teamId, String email);

    boolean existsByTeamIdAndEmail(Long teamId, String email);

    long countByTeamId(Long teamId);
}

