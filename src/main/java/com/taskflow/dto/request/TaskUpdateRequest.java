package com.taskflow.dto.request;

import com.taskflow.entity.enums.Priority;
import com.taskflow.entity.enums.TaskStatus;
import lombok.Data;
import java.time.LocalDate;

@Data
public class TaskUpdateRequest {
    private String title;
    private String description;
    private TaskStatus status;
    private Priority priority;
    private Integer storyPoints;
    private LocalDate dueDate;
    private Long assigneeId;
    private Long sprintId;
}