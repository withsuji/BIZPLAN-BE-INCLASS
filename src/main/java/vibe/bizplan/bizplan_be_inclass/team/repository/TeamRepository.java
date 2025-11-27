package vibe.bizplan.bizplan_be_inclass.team.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vibe.bizplan.bizplan_be_inclass.team.entity.Team;

import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {
    
    Optional<Team> findByName(String name);
    
    boolean existsByName(String name);
}

