package com.taskflow.repository;

import com.taskflow.entity.Sprint;
import com.taskflow.entity.Project;
import com.taskflow.entity.enums.SprintStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SprintRepository extends JpaRepository<Sprint, Long> {
    List<Sprint> findByProject(Project project);
    List<Sprint> findByProjectAndStatus(Project project, SprintStatus status);
}