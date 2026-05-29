package com.taskflow.controller;

import com.taskflow.dto.request.TaskCreateRequest;
import com.taskflow.dto.request.TaskUpdateRequest;
import com.taskflow.dto.response.TaskResponse;
import com.taskflow.entity.*;
import com.taskflow.entity.enums.TaskStatus;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.repository.*;
import com.taskflow.service.NotificationService;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Task Controller", description = "CRUD operations for tasks")
@SecurityRequirement(name = "bearerAuth")
public class TaskController {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final SprintRepository sprintRepository;
    private final TaskHistoryRepository historyRepository;
    private final LabelRepository labelRepository;
    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Get all tasks")
    public ResponseEntity<List<TaskResponse>> getAllTasks() {
        return ResponseEntity.ok(taskRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get task by ID")
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));
        return ResponseEntity.ok(toResponse(task));
    }

    @GetMapping("/project/{projectId}")
    @Operation(summary = "Get tasks by project")
    public ResponseEntity<List<TaskResponse>> getTasksByProject(@PathVariable Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
        return ResponseEntity.ok(taskRepository.findByProject(project).stream()
                .map(this::toResponse)
                .collect(Collectors.toList()));
    }

    @GetMapping("/assignee/{assigneeId}")
    @Operation(summary = "Get tasks by assignee")
    public ResponseEntity<List<TaskResponse>> getTasksByAssignee(@PathVariable Long assigneeId) {
        User assignee = userRepository.findById(assigneeId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", assigneeId));
        return ResponseEntity.ok(taskRepository.findByAssignee(assignee).stream()
                .map(this::toResponse)
                .collect(Collectors.toList()));
    }

    @GetMapping("/reporter/{reporterId}")
    @Operation(summary = "Get tasks by reporter")
    public ResponseEntity<List<TaskResponse>> getTasksByReporter(@PathVariable Long reporterId) {
        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", reporterId));
        return ResponseEntity.ok(taskRepository.findByReporter(reporter).stream()
                .map(this::toResponse)
                .collect(Collectors.toList()));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get tasks by status")
    public ResponseEntity<List<TaskResponse>> getTasksByStatus(@PathVariable TaskStatus status) {
        return ResponseEntity.ok(taskRepository.findByStatus(status).stream()
                .map(this::toResponse)
                .collect(Collectors.toList()));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(summary = "Create new task")
    public ResponseEntity<TaskResponse> createTask(
            @Valid @RequestBody TaskCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", userDetails.getUsername()));

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", request.getProjectId()));

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus() != null ? request.getStatus() : TaskStatus.TODO);
        task.setPriority(request.getPriority());
        task.setStoryPoints(request.getStoryPoints());
        task.setDueDate(request.getDueDate());
        task.setProject(project);
        task.setReporter(currentUser);

        if (request.getAssigneeId() != null) {
            User assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getAssigneeId()));
            task.setAssignee(assignee);
            notificationService.sendTaskAssignedNotification(assignee, task);
        }

        if (request.getSprintId() != null) {
            Sprint sprint = sprintRepository.findById(request.getSprintId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sprint", "id", request.getSprintId()));
            task.setSprint(sprint);
        }

        Task savedTask = taskRepository.save(task);

        // Сохраняем историю
        TaskHistory history = new TaskHistory();
        history.setTask(savedTask);
        history.setFieldName("CREATED");
        history.setNewValue("Task created");
        history.setChangedAt(LocalDateTime.now());
        history.setChangedBy(currentUser);
        historyRepository.save(history);

        return ResponseEntity.ok(toResponse(savedTask));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update task")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", userDetails.getUsername()));

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));

        if (request.getTitle() != null) {
            saveHistory(task, "title", task.getTitle(), request.getTitle(), currentUser);
            task.setTitle(request.getTitle());
        }

        if (request.getDescription() != null) {
            saveHistory(task, "description", task.getDescription(), request.getDescription(), currentUser);
            task.setDescription(request.getDescription());
        }

        if (request.getStatus() != null && request.getStatus() != task.getStatus()) {
            saveHistory(task, "status", task.getStatus().toString(), request.getStatus().toString(), currentUser);
            task.setStatus(request.getStatus());
            if (request.getStatus() == TaskStatus.DONE) {
                task.setCompletedAt(LocalDateTime.now());
                if (task.getAssignee() != null) {
                    notificationService.sendTaskCompletedNotification(task.getAssignee(), task);
                }
            }
        }

        if (request.getPriority() != null && request.getPriority() != task.getPriority()) {
            saveHistory(task, "priority", task.getPriority().toString(), request.getPriority().toString(), currentUser);
            task.setPriority(request.getPriority());
        }

        if (request.getStoryPoints() != null) {
            saveHistory(task, "storyPoints",
                    task.getStoryPoints() != null ? task.getStoryPoints().toString() : null,
                    request.getStoryPoints().toString(), currentUser);
            task.setStoryPoints(request.getStoryPoints());
        }

        if (request.getDueDate() != null) {
            saveHistory(task, "dueDate",
                    task.getDueDate() != null ? task.getDueDate().toString() : null,
                    request.getDueDate().toString(), currentUser);
            task.setDueDate(request.getDueDate());
        }

        if (request.getAssigneeId() != null && (task.getAssignee() == null || !task.getAssignee().getId().equals(request.getAssigneeId()))) {
            User newAssignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getAssigneeId()));
            String oldAssignee = task.getAssignee() != null ? task.getAssignee().getUsername() : null;
            saveHistory(task, "assignee", oldAssignee, newAssignee.getUsername(), currentUser);
            task.setAssignee(newAssignee);
            notificationService.sendTaskAssignedNotification(newAssignee, task);
        }

        if (request.getSprintId() != null && (task.getSprint() == null || !task.getSprint().getId().equals(request.getSprintId()))) {
            Sprint sprint = sprintRepository.findById(request.getSprintId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sprint", "id", request.getSprintId()));
            String oldSprint = task.getSprint() != null ? task.getSprint().getName() : null;
            saveHistory(task, "sprint", oldSprint, sprint.getName(), currentUser);
            task.setSprint(sprint);
        }

        Task updatedTask = taskRepository.save(task);
        return ResponseEntity.ok(toResponse(updatedTask));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete task")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/labels/{labelId}")
    @Operation(summary = "Add label to task")
    public ResponseEntity<TaskResponse> addLabelToTask(@PathVariable Long id, @PathVariable Long labelId) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));
        Label label = labelRepository.findById(labelId)
                .orElseThrow(() -> new ResourceNotFoundException("Label", "id", labelId));

        if (!task.getLabels().contains(label)) {
            task.getLabels().add(label);
        }

        Task saved = taskRepository.save(task);
        return ResponseEntity.ok(toResponse(saved));
    }

    @DeleteMapping("/{id}/labels/{labelId}")
    @Operation(summary = "Remove label from task")
    public ResponseEntity<TaskResponse> removeLabelFromTask(@PathVariable Long id, @PathVariable Long labelId) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));
        Label label = labelRepository.findById(labelId)
                .orElseThrow(() -> new ResourceNotFoundException("Label", "id", labelId));

        task.getLabels().remove(label);

        Task saved = taskRepository.save(task);
        return ResponseEntity.ok(toResponse(saved));
    }

    @GetMapping("/{id}/history")
    @Operation(summary = "Get task history")
    public ResponseEntity<List<TaskHistory>> getTaskHistory(@PathVariable Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));
        return ResponseEntity.ok(historyRepository.findByTaskOrderByChangedAtDesc(task));
    }

    private void saveHistory(Task task, String fieldName, String oldValue, String newValue, User changedBy) {
        TaskHistory history = new TaskHistory();
        history.setTask(task);
        history.setFieldName(fieldName);
        history.setOldValue(oldValue);
        history.setNewValue(newValue);
        history.setChangedAt(LocalDateTime.now());
        history.setChangedBy(changedBy);
        historyRepository.save(history);
    }

    private TaskResponse toResponse(Task task) {
        TaskResponse.TaskResponseBuilder builder = TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .storyPoints(task.getStoryPoints())
                .dueDate(task.getDueDate())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .completedAt(task.getCompletedAt())
                .projectId(task.getProject() != null ? task.getProject().getId() : null)
                .projectName(task.getProject() != null ? task.getProject().getName() : null)
                .assigneeId(task.getAssignee() != null ? task.getAssignee().getId() : null)
                .assigneeName(task.getAssignee() != null ? task.getAssignee().getUsername() : null)
                .reporterId(task.getReporter() != null ? task.getReporter().getId() : null)
                .reporterName(task.getReporter() != null ? task.getReporter().getUsername() : null)
                .sprintId(task.getSprint() != null ? task.getSprint().getId() : null)
                .sprintName(task.getSprint() != null ? task.getSprint().getName() : null);

        if (task.getLabels() != null) {
            builder.labels(task.getLabels().stream().map(Label::getName).collect(Collectors.toList()));
        }

        return builder.build();
    }
}