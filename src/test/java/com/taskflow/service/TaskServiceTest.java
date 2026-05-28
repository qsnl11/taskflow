package com.taskflow.service;

import com.taskflow.dto.request.TaskCreateRequest;
import com.taskflow.dto.response.TaskResponse;
import com.taskflow.entity.*;
import com.taskflow.entity.enums.Priority;
import com.taskflow.entity.enums.TaskStatus;
import com.taskflow.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SprintRepository sprintRepository;
    @Mock
    private TaskHistoryRepository historyRepository;
    @Mock
    private NotificationService notificationService;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private TaskService taskService;

    private User testUser;
    private Project testProject;
    private Task testTask;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        testProject = new Project();
        testProject.setId(1L);
        testProject.setName("Test Project");

        testTask = new Task();
        testTask.setId(1L);
        testTask.setTitle("Test Task");
        testTask.setStatus(TaskStatus.TODO);
        testTask.setProject(testProject);
        testTask.setReporter(testUser);

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("testuser");
    }

    @Test
    void createTask_ShouldCreateTask_WhenValidRequest() {
        TaskCreateRequest request = new TaskCreateRequest();
        request.setTitle("New Task");
        request.setProjectId(1L);
        request.setPriority(Priority.HIGH);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        TaskResponse response = taskService.createTask(request, 1L);

        assertThat(response).isNotNull();
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void updateTask_ShouldUpdateStatus_WhenValidRequest() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        com.taskflow.dto.request.TaskUpdateRequest request = new com.taskflow.dto.request.TaskUpdateRequest();
        request.setStatus(TaskStatus.IN_PROGRESS);

        TaskResponse response = taskService.updateTask(1L, request);

        assertThat(response).isNotNull();
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void getTaskById_ShouldReturnTask_WhenExists() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

        TaskResponse response = taskService.getTaskById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
    }
}