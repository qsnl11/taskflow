package com.taskflow.service;

import com.taskflow.dto.request.TaskCreateRequest;
import com.taskflow.dto.request.TaskUpdateRequest;
import com.taskflow.dto.response.TaskResponse;
import com.taskflow.entity.*;
import com.taskflow.entity.enums.TaskStatus;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final SprintRepository sprintRepository;
    private final TaskHistoryRepository historyRepository;
    private final NotificationService notificationService;

    public List<TaskResponse> getAllTasks() {
        return taskRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public TaskResponse getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));
        return toResponse(task);
    }

    public List<TaskResponse> getTasksByProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
        return taskRepository.findByProject(project).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<TaskResponse> getTasksByAssignee(Long assigneeId) {
        User assignee = userRepository.findById(assigneeId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", assigneeId));
        return taskRepository.findByAssignee(assignee).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public TaskResponse createTask(TaskCreateRequest request, Long reporterId) {
        log.info("Creating new task: {}", request.getTitle());

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", request.getProjectId()));

        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", reporterId));

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus());
        task.setPriority(request.getPriority());
        task.setStoryPoints(request.getStoryPoints());
        task.setDueDate(request.getDueDate());
        task.setProject(project);
        task.setReporter(reporter);

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

        saveHistory(savedTask, "CREATED", null, "Task created");

        return toResponse(savedTask);
    }

    @Transactional
    public TaskResponse updateTask(Long id, TaskUpdateRequest request) {
        log.info("Updating task with id: {}", id);

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));

        if (request.getTitle() != null) {
            saveHistory(task, "title", task.getTitle(), request.getTitle());
            task.setTitle(request.getTitle());
        }

        if (request.getDescription() != null) {
            saveHistory(task, "description", task.getDescription(), request.getDescription());
            task.setDescription(request.getDescription());
        }

        if (request.getStatus() != null && request.getStatus() != task.getStatus()) {
            saveHistory(task, "status", task.getStatus().toString(), request.getStatus().toString());
            task.setStatus(request.getStatus());

            if (request.getStatus() == TaskStatus.DONE) {
                task.setCompletedAt(LocalDateTime.now());

                if (task.getAssignee() != null) {
                    notificationService.sendTaskCompletedNotification(task.getAssignee(), task);
                }
            }
        }

        if (request.getPriority() != null && request.getPriority() != task.getPriority()) {
            saveHistory(task, "priority", task.getPriority().toString(), request.getPriority().toString());
            task.setPriority(request.getPriority());
        }

        if (request.getStoryPoints() != null) {
            saveHistory(task, "storyPoints",
                    task.getStoryPoints() != null ? task.getStoryPoints().toString() : null,
                    request.getStoryPoints().toString());
            task.setStoryPoints(request.getStoryPoints());
        }

        if (request.getDueDate() != null) {
            saveHistory(task, "dueDate",
                    task.getDueDate() != null ? task.getDueDate().toString() : null,
                    request.getDueDate().toString());
            task.setDueDate(request.getDueDate());
        }

        if (request.getAssigneeId() != null) {
            User newAssignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getAssigneeId()));
            String oldAssignee = task.getAssignee() != null ? task.getAssignee().getUsername() : null;
            saveHistory(task, "assignee", oldAssignee, newAssignee.getUsername());
            task.setAssignee(newAssignee);

            notificationService.sendTaskAssignedNotification(newAssignee, task);
        }

        if (request.getSprintId() != null) {
            Sprint sprint = sprintRepository.findById(request.getSprintId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sprint", "id", request.getSprintId()));
            saveHistory(task, "sprint",
                    task.getSprint() != null ? task.getSprint().getName() : null,
                    sprint.getName());
            task.setSprint(sprint);
        }

        Task updatedTask = taskRepository.save(task);
        return toResponse(updatedTask);
    }

    @Transactional
    public void deleteTask(Long id) {
        log.info("Deleting task with id: {}", id);
        taskRepository.deleteById(id);
    }

    private void saveHistory(Task task, String fieldName, String oldValue, String newValue) {
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        User changedBy = userRepository.findByUsername(currentUser).orElse(null);

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
        return TaskResponse.builder()
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
                .sprintName(task.getSprint() != null ? task.getSprint().getName() : null)
                .build();
    }
}