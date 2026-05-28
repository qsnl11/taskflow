package com.taskflow.repository;

import com.taskflow.entity.Attachment;
import com.taskflow.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    List<Attachment> findByTask(Task task);
}