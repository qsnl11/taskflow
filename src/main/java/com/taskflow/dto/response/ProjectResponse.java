package com.taskflow.dto.response;

import com.taskflow.entity.enums.ProjectStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ProjectResponse {
    private Long id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDate startDate;
    private LocalDate endDate;
    private ProjectStatus status;
    private String ownerName;
    private List<String> memberNames;
    private Integer taskCount;
}