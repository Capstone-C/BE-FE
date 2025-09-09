package com.capstone.be.repository;

import com.capstone.be.entity.Task;
import com.capstone.be.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    List<Task> findByAssignee(User assignee);
    
    List<Task> findByAssigneeId(Long assigneeId);
    
    List<Task> findByStatus(Task.TaskStatus status);
    
    List<Task> findByPriority(Task.TaskPriority priority);
    
    List<Task> findByAssigneeIdAndStatus(Long assigneeId, Task.TaskStatus status);
}