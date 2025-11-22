package com.capstone.web.boards.controller;

import com.capstone.web.auth.jwt.JwtAuthenticationFilter.MemberPrincipal;
import com.capstone.web.posts.dto.PostDto;
import com.capstone.web.posts.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/boards/{boardId}/posts")
@RequiredArgsConstructor
public class BoardPostsController {

    private final PostService postService;

    // [옵션 A] JSON만 보낼 때
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> createJson(
            @PathVariable Long boardId,
            @AuthenticationPrincipal MemberPrincipal userPrincipal,
            @Valid @RequestBody PostDto.CreateRequest request
    ) {
        request.setCategoryId(boardId);
        Long id = postService.createPost(userPrincipal.id(), request, null, null);
        return ResponseEntity.created(URI.create("/api/posts/" + id)).build();
    }

    // [옵션 B] 파일(썸네일 및 추가 이미지)과 함께 보낼 때
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> createMultipart(
            @PathVariable Long boardId,
            @AuthenticationPrincipal MemberPrincipal userPrincipal,
            @Valid @RequestPart("request") PostDto.CreateRequest request,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnailFile,
            @RequestPart(value = "files", required = false) List<MultipartFile> files // (추가)
    ) {
        request.setCategoryId(boardId);
        Long id = postService.createPost(userPrincipal.id(), request, thumbnailFile, files);
        return ResponseEntity.created(URI.create("/api/posts/" + id)).build();
    }
}