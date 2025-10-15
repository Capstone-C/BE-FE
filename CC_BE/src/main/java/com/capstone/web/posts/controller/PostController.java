package com.capstone.web.posts.controller;

import com.capstone.web.posts.dto.PostDto;
import com.capstone.web.posts.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    //POST /api/posts/
    @PostMapping
    public ResponseEntity<Void> createPost(@Valid @RequestBody PostDto.CreateRequest request) {
        Long postId = postService.createPost(request);
        return ResponseEntity.created(URI.create("/api/posts/" + postId)).build();
    }

    //GET /api/posts/{id}
    @GetMapping("/{id}")
    public ResponseEntity<PostDto.Response> getPost(@PathVariable Long id) {
        PostDto.Response post = postService.getPostById(id);
        return ResponseEntity.ok(post);
    }

    //GET /api/posts/
    @GetMapping
    public ResponseEntity<List<PostDto.Response>> getAllPosts() {
        List<PostDto.Response> posts = postService.getAllPosts();
        return ResponseEntity.ok(posts);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updatePost(@PathVariable Long id, @Valid @RequestBody PostDto.UpdateRequest request) {
        postService.updatePost(id, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }
}