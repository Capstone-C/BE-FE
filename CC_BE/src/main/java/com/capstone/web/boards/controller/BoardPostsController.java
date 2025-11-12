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

        // (수정) 1. DTO를 새로 만들지 않고, 받아온 request 객체에 boardId만 설정
        request.setCategoryId(boardId);

        // (수정) 2. 수정된 request 객체를 그대로 서비스로 전달
        Long id = postService.createPost(userPrincipal.id(), request);

        return ResponseEntity.created(URI.create("/api/posts/" + id)).build();
    }
}