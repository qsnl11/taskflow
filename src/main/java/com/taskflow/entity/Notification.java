package com.taskflow.entity;

import com.taskflow.entity.enums.NotificationType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String message;
    private String link;
    private String metadata;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    private boolean isRead = false;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}