package com.taskflow.service;

import com.taskflow.dto.request.ProjectCreateRequest;
import com.taskflow.dto.response.ProjectResponse;
import com.taskflow.entity.Project;
import com.taskflow.entity.User;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.repository.ProjectRepository;
import com.taskflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public List<ProjectResponse> getAllProjects() {
        return projectRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ProjectResponse getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
        return toResponse(project);
    }

    @Transactional
    public ProjectResponse createProject(ProjectCreateRequest request, Long ownerId) {
        log.info("Creating new project: {}", request.getName());

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", ownerId));

        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setOwner(owner);

        project.getMembers().add(owner);

        Project savedProject = projectRepository.save(project);
        return toResponse(savedProject);
    }

    @Transactional
    public void addMemberToProject(Long projectId, Long userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (!project.getMembers().contains(user)) {
            project.getMembers().add(user);
            projectRepository.save(project);
            log.info("User {} added to project {}", user.getUsername(), project.getName());
        }
    }

    @Transactional
    public void deleteProject(Long id) {
        log.info("Deleting project with id: {}", id);
        projectRepository.deleteById(id);
    }

    private ProjectResponse toResponse(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .status(project.getStatus())
                .ownerName(project.getOwner() != null ? project.getOwner().getUsername() : null)
                .memberNames(project.getMembers().stream().map(User::getUsername).collect(Collectors.toList()))
                .taskCount(project.getTasks() != null ? project.getTasks().size() : 0)
                .build();
    }
}