package com.taskflow.repository;

import com.taskflow.entity.Project;
import com.taskflow.entity.User;
import com.taskflow.entity.enums.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByOwner(User owner);
    List<Project> findByMembersContaining(User user);
    List<Project> findByStatus(ProjectStatus status);
}