package com.taskflow.controller;

import com.taskflow.entity.Sprint;
import com.taskflow.entity.Project;
import com.taskflow.entity.enums.SprintStatus;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.repository.SprintRepository;
import com.taskflow.repository.ProjectRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/sprints")
@RequiredArgsConstructor
@Tag(name = "Sprint Controller", description = "Operations for sprints")
@SecurityRequirement(name = "bearerAuth")
public class SprintController {

    private final SprintRepository sprintRepository;
    private final ProjectRepository projectRepository;

    @GetMapping
    @Operation(summary = "Get all sprints")
    public ResponseEntity<List<Sprint>> getAllSprints() {
        return ResponseEntity.ok(sprintRepository.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get sprint by ID")
    public ResponseEntity<Sprint> getSprintById(@PathVariable Long id) {
        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint", "id", id));
        return ResponseEntity.ok(sprint);
    }

    @GetMapping("/project/{projectId}")
    @Operation(summary = "Get sprints by project")
    public ResponseEntity<List<Sprint>> getSprintsByProject(@PathVariable Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
        return ResponseEntity.ok(sprintRepository.findByProject(project));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(summary = "Create new sprint")
    public ResponseEntity<Sprint> createSprint(
            @RequestParam String name,
            @RequestParam(required = false) String goal,
            @RequestParam Long projectId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        Sprint sprint = new Sprint();
        sprint.setName(name);
        sprint.setGoal(goal);
        sprint.setProject(project);
        sprint.setStartDate(startDate);
        sprint.setEndDate(endDate);
        sprint.setStatus(SprintStatus.PLANNED);
        sprint.setCreatedAt(LocalDateTime.now());

        Sprint saved = sprintRepository.save(sprint);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/{id}/start")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(summary = "Start sprint")
    public ResponseEntity<Sprint> startSprint(@PathVariable Long id) {
        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint", "id", id));
        sprint.setStatus(SprintStatus.ACTIVE);
        sprint.setUpdatedAt(LocalDateTime.now());
        return ResponseEntity.ok(sprintRepository.save(sprint));
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(summary = "Complete sprint")
    public ResponseEntity<Sprint> completeSprint(@PathVariable Long id) {
        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint", "id", id));
        sprint.setStatus(SprintStatus.COMPLETED);
        sprint.setCompletedAt(LocalDateTime.now());
        sprint.setUpdatedAt(LocalDateTime.now());
        return ResponseEntity.ok(sprintRepository.save(sprint));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(summary = "Update sprint")
    public ResponseEntity<Sprint> updateSprint(@PathVariable Long id, @RequestBody Sprint sprintDetails) {
        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint", "id", id));

        if (sprintDetails.getName() != null) sprint.setName(sprintDetails.getName());
        if (sprintDetails.getGoal() != null) sprint.setGoal(sprintDetails.getGoal());
        if (sprintDetails.getStartDate() != null) sprint.setStartDate(sprintDetails.getStartDate());
        if (sprintDetails.getEndDate() != null) sprint.setEndDate(sprintDetails.getEndDate());
        if (sprintDetails.getVelocity() != null) sprint.setVelocity(sprintDetails.getVelocity());

        sprint.setUpdatedAt(LocalDateTime.now());
        return ResponseEntity.ok(sprintRepository.save(sprint));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete sprint")
    public ResponseEntity<Void> deleteSprint(@PathVariable Long id) {
        sprintRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}