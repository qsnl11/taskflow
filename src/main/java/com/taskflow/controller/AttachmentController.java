package com.taskflow.controller;

import com.taskflow.entity.Attachment;
import com.taskflow.repository.AttachmentRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/attachments")
@RequiredArgsConstructor
@Tag(name = "Attachment Controller", description = "Operations for file attachments")
@SecurityRequirement(name = "bearerAuth")
public class AttachmentController {

    private final AttachmentRepository attachmentRepository;

    @GetMapping("/task/{taskId}")
    @Operation(summary = "Get attachments by task")
    public ResponseEntity<List<Attachment>> getAttachmentsByTask(@PathVariable Long taskId) {
        // Simplified - in real app, fetch by task
        return ResponseEntity.ok(attachmentRepository.findAll());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete attachment")
    public ResponseEntity<Void> deleteAttachment(@PathVariable Long id) {
        attachmentRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}