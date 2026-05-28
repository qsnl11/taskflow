package com.taskflow.controller;

import com.taskflow.dto.request.CommentRequest;
import com.taskflow.dto.response.CommentResponse;
import com.taskflow.service.CommentService;
import com.taskflow.entity.User;
import com.taskflow.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@Tag(name = "Comment Controller", description = "Operations for task comments")
@SecurityRequirement(name = "bearerAuth")
public class CommentController {

    private final CommentService commentService;
    private final UserRepository userRepository;

    @GetMapping("/task/{taskId}")
    @Operation(summary = "Get comments by task")
    public ResponseEntity<List<CommentResponse>> getCommentsByTask(@PathVariable Long taskId) {
        return ResponseEntity.ok(commentService.getCommentsByTask(taskId));
    }

    @PostMapping("/task/{taskId}")
    @Operation(summary = "Add comment to task")
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable Long taskId,
            @Valid @RequestBody CommentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        User currentUser = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
        return ResponseEntity.ok(commentService.addComment(taskId, request, currentUser.getId()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete comment")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }
}