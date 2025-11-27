package vibe.bizplan.bizplan_be_inclass.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vibe.bizplan.bizplan_be_inclass.user.entity.User;
import vibe.bizplan.bizplan_be_inclass.user.entity.UserRole;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByTeamId(Long teamId);

    List<User> findByTeamIdAndRole(Long teamId, UserRole role);

    @Query("SELECT u FROM User u WHERE u.team.id = :teamId ORDER BY u.createdAt DESC")
    List<User> findByTeamIdOrderByCreatedAtDesc(@Param("teamId") Long teamId);

    long countByTeamId(Long teamId);
}

