package com.taskflow.service;

import com.taskflow.entity.TaskHistory;
import com.taskflow.repository.TaskHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditService {

    private final TaskHistoryRepository historyRepository;

    public void logAction(String action, String details) {
        log.info("AUDIT - Action: {}, Details: {}", action, details);
    }

    @Transactional
    public TaskHistory saveHistory(TaskHistory history) {
        return historyRepository.save(history);
    }
}