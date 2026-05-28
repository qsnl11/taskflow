package com.taskflow.repository;

import com.taskflow.entity.Task;
import com.taskflow.entity.User;
import com.taskflow.entity.Project;
import com.taskflow.entity.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByAssignee(User assignee);
    List<Task> findByReporter(User reporter);
    List<Task> findByProject(Project project);
    List<Task> findByStatus(TaskStatus status);
    List<Task> findByDueDateBefore(LocalDate date);

    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId AND t.status = :status")
    List<Task> findByProjectIdAndStatus(@Param("projectId") Long projectId, @Param("status") TaskStatus status);
}