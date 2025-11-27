package vibe.bizplan.bizplan_be_inclass.summary.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vibe.bizplan.bizplan_be_inclass.summary.entity.SummaryNote;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SummaryNoteRepository extends JpaRepository<SummaryNote, Long> {

    Optional<SummaryNote> findByEventId(Long eventId);

    boolean existsByEventId(Long eventId);

    @Query("""
        SELECT s FROM SummaryNote s
        JOIN s.event e
        WHERE e.user.id = :userId
          AND e.startAt BETWEEN :start AND :end
        ORDER BY e.startAt DESC
    """)
    List<SummaryNote> findByUserAndDateRange(
            @Param("userId") Long userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
        SELECT s FROM SummaryNote s
        JOIN s.event e
        WHERE e.team.id = :teamId
        ORDER BY e.startAt DESC
    """)
    List<SummaryNote> findByTeamIdOrderByEventStartAtDesc(@Param("teamId") Long teamId);
}

