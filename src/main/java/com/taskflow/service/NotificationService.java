package com.taskflow.service;

import com.taskflow.entity.Notification;
import com.taskflow.entity.Task;
import com.taskflow.entity.User;
import com.taskflow.entity.enums.NotificationType;
import com.taskflow.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public void sendTaskAssignedNotification(User user, Task task) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage("You have been assigned to task: " + task.getTitle());
        notification.setType(NotificationType.TASK_ASSIGNED);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setLink("/tasks/" + task.getId());
        notification.setRead(false);

        notificationRepository.save(notification);
        log.info("Notification sent to user {}: assigned to task {}", user.getUsername(), task.getId());
    }

    @Transactional
    public void sendTaskCompletedNotification(User user, Task task) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage("Task completed: " + task.getTitle());
        notification.setType(NotificationType.TASK_COMPLETED);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setLink("/tasks/" + task.getId());
        notification.setRead(false);

        notificationRepository.save(notification);
        log.info("Notification sent to user {}: task {} completed", user.getUsername(), task.getId());
    }

    public List<Notification> getUserUnreadNotifications(Long userId) {
        User user = new User();
        user.setId(userId);
        return notificationRepository.findByUserAndIsReadFalse(user);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId).orElse(null);
        if (notification != null) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
        }
    }
}