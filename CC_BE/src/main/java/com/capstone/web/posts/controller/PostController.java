package com.capstone.web.posts.controller;

import com.capstone.web.auth.jwt.JwtAuthenticationFilter.MemberPrincipal;
import com.capstone.web.posts.dto.PostDto;
import com.capstone.web.posts.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<Void> createPost(
            @AuthenticationPrincipal MemberPrincipal userPrincipal, // (수정) 인증 정보 받기
            @Valid @RequestBody PostDto.CreateRequest request
    ) {
        // (수정) 토큰에서 추출한 사용자 ID(userPrincipal.id())를 서비스로 전달
        Long postId = postService.createPost(userPrincipal.id(), request);
        return ResponseEntity.created(URI.create("/api/posts/" + postId)).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDto.Response> getPost(@PathVariable Long id) {
        PostDto.Response post = postService.getPostById(id);
        return ResponseEntity.ok(post);
    }

    @GetMapping
    public ResponseEntity<List<PostDto.Response>> getAllPosts() {
        List<PostDto.Response> posts = postService.getAllPosts();
        return ResponseEntity.ok(posts);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updatePost(
            @PathVariable Long id,
            @AuthenticationPrincipal MemberPrincipal userPrincipal, // (수정) 인증 정보 받기
            @Valid @RequestBody PostDto.UpdateRequest request
    ) {
        // (수정) 토큰에서 추출한 사용자 ID(userPrincipal.id())를 서비스로 전달
        postService.updatePost(id, userPrincipal.id(), request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long id,
            @AuthenticationPrincipal MemberPrincipal userPrincipal // (수정) 인증 정보 받기
    ) {
        // (수정) 토큰에서 추출한 사용자 ID(userPrincipal.id())를 서비스로 전달
        postService.deletePost(id, userPrincipal.id());
        return ResponseEntity.noContent().build();
    }
}