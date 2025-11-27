package vibe.bizplan.bizplan_be_inclass.focus.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vibe.bizplan.bizplan_be_inclass.focus.entity.FocusBlock;

import java.time.LocalDateTime;
import java.util.List;

public interface FocusBlockRepository extends JpaRepository<FocusBlock, Long> {

    List<FocusBlock> findByUserId(Long userId);

    @Query("""
        SELECT f FROM FocusBlock f
        WHERE f.user.id = :userId
          AND f.startAt <= :end
          AND f.endAt >= :start
    """)
    List<FocusBlock> findOverlappingBlocks(
            @Param("userId") Long userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
        SELECT f FROM FocusBlock f
        WHERE f.user.id = :userId
          AND f.startAt BETWEEN :start AND :end
        ORDER BY f.startAt ASC
    """)
    List<FocusBlock> findByUserAndDateRange(
            @Param("userId") Long userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
        SELECT COUNT(f) > 0 FROM FocusBlock f
        WHERE f.user.id = :userId
          AND f.startAt <= :time
          AND f.endAt >= :time
    """)
    boolean existsActiveBlockAt(
            @Param("userId") Long userId,
            @Param("time") LocalDateTime time
    );
}

