package com.taskflow.controller;

import com.taskflow.entity.Label;
import com.taskflow.entity.Task;
import com.taskflow.exception.ResourceNotFoundException;
import com.taskflow.repository.LabelRepository;
import com.taskflow.repository.TaskRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/labels")
@RequiredArgsConstructor
@Tag(name = "Label Controller", description = "Operations for labels/tags")
@SecurityRequirement(name = "bearerAuth")
public class LabelController {

    private final LabelRepository labelRepository;
    private final TaskRepository taskRepository;

    @GetMapping
    @Operation(summary = "Get all labels")
    public ResponseEntity<List<Label>> getAllLabels() {
        return ResponseEntity.ok(labelRepository.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get label by ID")
    public ResponseEntity<Label> getLabelById(@PathVariable Long id) {
        Label label = labelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Label", "id", id));
        return ResponseEntity.ok(label);
    }

    @GetMapping("/task/{taskId}")
    @Operation(summary = "Get labels by task")
    public ResponseEntity<List<Label>> getLabelsByTask(@PathVariable Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
        return ResponseEntity.ok(task.getLabels());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(summary = "Create new label")
    public ResponseEntity<Label> createLabel(@RequestBody Label label) {
        label.setCreatedAt(LocalDateTime.now());
        label.setUpdatedAt(LocalDateTime.now());
        Label saved = labelRepository.save(label);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(summary = "Update label")
    public ResponseEntity<Label> updateLabel(@PathVariable Long id, @RequestBody Label labelDetails) {
        Label label = labelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Label", "id", id));

        if (labelDetails.getName() != null) label.setName(labelDetails.getName());
        if (labelDetails.getColor() != null) label.setColor(labelDetails.getColor());
        if (labelDetails.getDescription() != null) label.setDescription(labelDetails.getDescription());

        label.setUpdatedAt(LocalDateTime.now());
        return ResponseEntity.ok(labelRepository.save(label));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete label")
    public ResponseEntity<Void> deleteLabel(@PathVariable Long id) {
        labelRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}