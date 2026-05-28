package com.taskflow.repository;

import com.taskflow.entity.Comment;
import com.taskflow.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByTask(Task task);
    List<Comment> findByTaskOrderByCreatedAtDesc(Task task);
}