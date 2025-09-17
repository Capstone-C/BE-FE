package com.capstone.be.controller;

import com.capstone.be.entity.User;
import com.capstone.be.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {
    
    @Autowired
    private UserRepository userRepository;
    
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/username/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
        Optional<User> user = userRepository.findByUsername(username);
        return user.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody User user) {
        // Check if username already exists
        if (userRepository.existsByUsername(user.getUsername())) {
            return ResponseEntity.badRequest()
                    .body("Error: Username is already taken!");
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(user.getEmail())) {
            return ResponseEntity.badRequest()
                    .body("Error: Email is already in use!");
        }
        
        User savedUser = userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody User userDetails) {
        Optional<User> optionalUser = userRepository.findById(id);
        
        if (!optionalUser.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        User user = optionalUser.get();
        
        // Check if username is being changed and if new username already exists
        if (!user.getUsername().equals(userDetails.getUsername()) && 
            userRepository.existsByUsername(userDetails.getUsername())) {
            return ResponseEntity.badRequest()
                    .body("Error: Username is already taken!");
        }
        
        // Check if email is being changed and if new email already exists
        if (!user.getEmail().equals(userDetails.getEmail()) && 
            userRepository.existsByEmail(userDetails.getEmail())) {
            return ResponseEntity.badRequest()
                    .body("Error: Email is already in use!");
        }
        
        user.setUsername(userDetails.getUsername());
        user.setEmail(userDetails.getEmail());
        user.setFirstName(userDetails.getFirstName());
        user.setLastName(userDetails.getLastName());
        
        User updatedUser = userRepository.save(user);
        return ResponseEntity.ok(updatedUser);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        Optional<User> user = userRepository.findById(id);
        
        if (!user.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        userRepository.delete(user.get());
        return ResponseEntity.ok().body("User deleted successfully!");
    }
}