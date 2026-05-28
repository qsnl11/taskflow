package com.taskflow.service;

import com.taskflow.entity.Sprint;
import com.taskflow.entity.Project;
import com.taskflow.entity.enums.SprintStatus;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.repository.SprintRepository;
import com.taskflow.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SprintService {

    private final SprintRepository sprintRepository;
    private final ProjectRepository projectRepository;

    @Transactional
    public Sprint createSprint(String name, String goal, Long projectId,
                               java.time.LocalDate startDate, java.time.LocalDate endDate) {
        log.info("Creating sprint: {} for project {}", name, projectId);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        Sprint sprint = new Sprint();
        sprint.setName(name);
        sprint.setGoal(goal);
        sprint.setProject(project);
        sprint.setStartDate(startDate);
        sprint.setEndDate(endDate);
        sprint.setStatus(SprintStatus.PLANNED);

        return sprintRepository.save(sprint);
    }

    @Transactional
    public Sprint startSprint(Long sprintId) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint", "id", sprintId));

        sprint.setStatus(SprintStatus.ACTIVE);
        log.info("Sprint {} started", sprint.getName());
        return sprintRepository.save(sprint);
    }

    @Transactional
    public Sprint completeSprint(Long sprintId) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint", "id", sprintId));

        sprint.setStatus(SprintStatus.COMPLETED);
        sprint.setCompletedAt(LocalDateTime.now());
        log.info("Sprint {} completed", sprint.getName());
        return sprintRepository.save(sprint);
    }

    public List<Sprint> getSprintsByProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
        return sprintRepository.findByProject(project);
    }
}