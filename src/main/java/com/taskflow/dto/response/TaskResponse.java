package com.taskflow.dto.response;

import com.taskflow.entity.enums.Priority;
import com.taskflow.entity.enums.TaskStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class TaskResponse {
    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private Priority priority;
    private Integer storyPoints;
    private LocalDate dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;

    private Long projectId;
    private String projectName;

    private Long assigneeId;
    private String assigneeName;

    private Long reporterId;
    private String reporterName;

    private Long sprintId;
    private String sprintName;

    private List<CommentResponse> comments;
    private List<AttachmentResponse> attachments;
    private List<String> labels;
}