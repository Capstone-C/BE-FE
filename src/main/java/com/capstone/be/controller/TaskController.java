package com.capstone.be.controller;

import com.capstone.be.entity.Task;
import com.capstone.be.entity.User;
import com.capstone.be.repository.TaskRepository;
import com.capstone.be.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*")
public class TaskController {
    
    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks() {
        List<Task> tasks = taskRepository.findAll();
        return ResponseEntity.ok(tasks);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long id) {
        Optional<Task> task = taskRepository.findById(id);
        return task.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Task>> getTasksByUserId(@PathVariable Long userId) {
        List<Task> tasks = taskRepository.findByAssigneeId(userId);
        return ResponseEntity.ok(tasks);
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Task>> getTasksByStatus(@PathVariable Task.TaskStatus status) {
        List<Task> tasks = taskRepository.findByStatus(status);
        return ResponseEntity.ok(tasks);
    }
    
    @GetMapping("/priority/{priority}")
    public ResponseEntity<List<Task>> getTasksByPriority(@PathVariable Task.TaskPriority priority) {
        List<Task> tasks = taskRepository.findByPriority(priority);
        return ResponseEntity.ok(tasks);
    }
    
    @PostMapping
    public ResponseEntity<Task> createTask(@Valid @RequestBody Task task) {
        Task savedTask = taskRepository.save(task);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedTask);
    }
    
    @PostMapping("/{taskId}/assign/{userId}")
    public ResponseEntity<?> assignTaskToUser(@PathVariable Long taskId, @PathVariable Long userId) {
        Optional<Task> optionalTask = taskRepository.findById(taskId);
        Optional<User> optionalUser = userRepository.findById(userId);
        
        if (!optionalTask.isPresent()) {
            return ResponseEntity.badRequest().body("Task not found!");
        }
        
        if (!optionalUser.isPresent()) {
            return ResponseEntity.badRequest().body("User not found!");
        }
        
        Task task = optionalTask.get();
        User user = optionalUser.get();
        
        task.setAssignee(user);
        Task updatedTask = taskRepository.save(task);
        
        return ResponseEntity.ok(updatedTask);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @Valid @RequestBody Task taskDetails) {
        Optional<Task> optionalTask = taskRepository.findById(id);
        
        if (!optionalTask.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        Task task = optionalTask.get();
        task.setTitle(taskDetails.getTitle());
        task.setDescription(taskDetails.getDescription());
        task.setStatus(taskDetails.getStatus());
        task.setPriority(taskDetails.getPriority());
        task.setDueDate(taskDetails.getDueDate());
        
        // Only update assignee if provided in request
        if (taskDetails.getAssignee() != null) {
            task.setAssignee(taskDetails.getAssignee());
        }
        
        Task updatedTask = taskRepository.save(task);
        return ResponseEntity.ok(updatedTask);
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<Task> updateTaskStatus(@PathVariable Long id, @RequestBody Task.TaskStatus status) {
        Optional<Task> optionalTask = taskRepository.findById(id);
        
        if (!optionalTask.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        Task task = optionalTask.get();
        task.setStatus(status);
        Task updatedTask = taskRepository.save(task);
        
        return ResponseEntity.ok(updatedTask);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable Long id) {
        Optional<Task> task = taskRepository.findById(id);
        
        if (!task.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        taskRepository.delete(task.get());
        return ResponseEntity.ok().body("Task deleted successfully!");
    }
}