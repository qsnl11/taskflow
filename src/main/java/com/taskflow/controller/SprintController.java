package com.taskflow.controller;

import com.taskflow.entity.Sprint;
import com.taskflow.service.SprintService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/sprints")
@RequiredArgsConstructor
@Tag(name = "Sprint Controller", description = "Operations for sprints")
@SecurityRequirement(name = "bearerAuth")
public class SprintController {

    private final SprintService sprintService;

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(summary = "Create new sprint")
    public ResponseEntity<Sprint> createSprint(
            @RequestParam String name,
            @RequestParam String goal,
            @RequestParam Long projectId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        return ResponseEntity.ok(sprintService.createSprint(name, goal, projectId, startDate, endDate));
    }

    @PostMapping("/{id}/start")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(summary = "Start sprint")
    public ResponseEntity<Sprint> startSprint(@PathVariable Long id) {
        return ResponseEntity.ok(sprintService.startSprint(id));
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(summary = "Complete sprint")
    public ResponseEntity<Sprint> completeSprint(@PathVariable Long id) {
        return ResponseEntity.ok(sprintService.completeSprint(id));
    }

    @GetMapping("/project/{projectId}")
    @Operation(summary = "Get sprints by project")
    public ResponseEntity<List<Sprint>> getSprintsByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(sprintService.getSprintsByProject(projectId));
    }
}