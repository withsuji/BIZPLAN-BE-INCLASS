package vibe.bizplan.bizplan_be_inclass.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vibe.bizplan.bizplan_be_inclass.project.entity.Project;
import vibe.bizplan.bizplan_be_inclass.project.entity.ProjectStatus;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByTeamId(Long teamId);

    List<Project> findByTeamIdAndStatus(Long teamId, ProjectStatus status);

    List<Project> findByTeamIdOrderByCreatedAtDesc(Long teamId);

    long countByTeamIdAndStatus(Long teamId, ProjectStatus status);
}

