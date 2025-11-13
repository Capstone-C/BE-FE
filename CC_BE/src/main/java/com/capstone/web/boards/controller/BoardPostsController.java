package com.capstone.web.boards.controller;

import com.capstone.web.auth.jwt.JwtAuthenticationFilter.MemberPrincipal;
import com.capstone.web.posts.dto.PostDto;
import com.capstone.web.posts.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/boards/{boardId}/posts")
@RequiredArgsConstructor
public class BoardPostsController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<Void> create(@PathVariable Long boardId,
                                       @AuthenticationPrincipal MemberPrincipal userPrincipal,
                                       @Valid @RequestBody PostDto.CreateRequest request) {
        PostDto.CreateRequest req = new PostDto.CreateRequest(boardId, request.getTitle(), request.getContent(), request.getStatus(), request.getIsRecipe());
        Long id = postService.createPost(userPrincipal.id(), req);
        return ResponseEntity.created(URI.create("/api/posts/" + id)).build();
    }
}

