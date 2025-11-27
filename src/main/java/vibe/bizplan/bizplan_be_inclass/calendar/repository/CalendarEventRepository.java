package vibe.bizplan.bizplan_be_inclass.calendar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vibe.bizplan.bizplan_be_inclass.calendar.entity.CalendarEvent;
import vibe.bizplan.bizplan_be_inclass.calendar.entity.EventStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CalendarEventRepository extends JpaRepository<CalendarEvent, Long> {

    Optional<CalendarEvent> findByExternalId(String externalId);

    @Query("""
        SELECT e FROM CalendarEvent e
        WHERE e.team.id = :teamId
          AND e.startAt >= :start
          AND e.endAt <= :end
          AND e.status = :status
        ORDER BY e.startAt ASC
    """)
    List<CalendarEvent> findByTeamAndDateRangeAndStatus(
            @Param("teamId") Long teamId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("status") EventStatus status
    );

    @Query("""
        SELECT e FROM CalendarEvent e
        WHERE e.user.id = :userId
          AND e.startAt BETWEEN :start AND :end
        ORDER BY e.startAt ASC
    """)
    List<CalendarEvent> findByUserAndDateRange(
            @Param("userId") Long userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
        SELECT e FROM CalendarEvent e
        WHERE e.team.id = :teamId
          AND e.startAt BETWEEN :start AND :end
        ORDER BY e.startAt ASC
    """)
    List<CalendarEvent> findByTeamAndDateRange(
            @Param("teamId") Long teamId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
        SELECT e FROM CalendarEvent e
        LEFT JOIN TimeEntry t ON t.event = e
        WHERE e.status = 'CONFIRMED'
          AND e.endAt < :cutoffTime
          AND t.id IS NULL
    """)
    List<CalendarEvent> findCompletedEventsWithoutTimeEntry(
            @Param("cutoffTime") LocalDateTime cutoffTime
    );

    List<CalendarEvent> findByUserIdAndStatus(Long userId, EventStatus status);

    long countByTeamIdAndStatus(Long teamId, EventStatus status);
}

