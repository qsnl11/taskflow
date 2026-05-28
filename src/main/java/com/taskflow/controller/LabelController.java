package com.taskflow.controller;

import com.taskflow.entity.Label;
import com.taskflow.repository.LabelRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/labels")
@RequiredArgsConstructor
@Tag(name = "Label Controller", description = "Operations for labels/tags")
@SecurityRequirement(name = "bearerAuth")
public class LabelController {

    private final LabelRepository labelRepository;

    @GetMapping
    @Operation(summary = "Get all labels")
    public ResponseEntity<List<Label>> getAllLabels() {
        return ResponseEntity.ok(labelRepository.findAll());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Operation(summary = "Create new label")
    public ResponseEntity<Label> createLabel(@RequestBody Label label) {
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