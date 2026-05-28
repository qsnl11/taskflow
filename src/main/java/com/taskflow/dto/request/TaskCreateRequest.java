package com.taskflow.dto.request;

import com.taskflow.entity.enums.Priority;
import com.taskflow.entity.enums.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class TaskCreateRequest {
    @NotBlank
    private String title;

    private String description;

    private TaskStatus status = TaskStatus.TODO;
    private Priority priority = Priority.MEDIUM;
    private Integer storyPoints;
    private LocalDate dueDate;

    @NotNull
    private Long projectId;

    private Long assigneeId;
    private Long sprintId;
}