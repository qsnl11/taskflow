package com.taskflow.controller;

import com.taskflow.dto.request.ProjectCreateRequest;
import com.taskflow.dto.response.ProjectResponse;
import com.taskflow.service.ProjectService;
import com.taskflow.entity.User;
import com.taskflow.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "Project Controller", description = "CRUD operations for projects")
@SecurityRequirement(name = "bearerAuth")
public class ProjectController {

    private final ProjectService projectService;
    private final UserRepository userRepository;

    @GetMapping
    @Operation(summary = "Get all projects")
    public ResponseEntity<List<ProjectResponse>> getAllProjects() {
        return ResponseEntity.ok(projectService.getAllProjects());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get project by ID")
    public ResponseEntity<ProjectResponse> getProjectById(@PathVariable Long id) {
        return ResponseEntity.ok(projectService.getProjectById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(summary = "Create new project")
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody ProjectCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        User currentUser = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
        return ResponseEntity.ok(projectService.createProject(request, currentUser.getId()));
    }

    @PostMapping("/{projectId}/members/{userId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(summary = "Add member to project")
    public ResponseEntity<Void> addMemberToProject(
            @PathVariable Long projectId,
            @PathVariable Long userId) {
        projectService.addMemberToProject(projectId, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete project")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }
}