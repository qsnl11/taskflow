package com.taskflow.service;

import com.taskflow.dto.request.CommentRequest;
import com.taskflow.dto.response.CommentResponse;
import com.taskflow.entity.Comment;
import com.taskflow.entity.Task;
import com.taskflow.entity.User;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.repository.CommentRepository;
import com.taskflow.repository.TaskRepository;
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
public class CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public List<CommentResponse> getCommentsByTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
        return commentRepository.findByTaskOrderByCreatedAtDesc(task).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CommentResponse addComment(Long taskId, CommentRequest request, Long userId) {
        log.info("Adding comment to task {} by user {}", taskId, userId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setTask(task);
        comment.setUser(user);

        Comment savedComment = commentRepository.save(comment);
        return toResponse(savedComment);
    }

    @Transactional
    public void deleteComment(Long id) {
        log.info("Deleting comment with id: {}", id);
        commentRepository.deleteById(id);
    }

    private CommentResponse toResponse(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .userId(comment.getUser() != null ? comment.getUser().getId() : null)
                .username(comment.getUser() != null ? comment.getUser().getUsername() : null)
                .build();
    }
}