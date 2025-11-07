package com.capstone.web.posts.controller;

import com.capstone.web.auth.jwt.JwtAuthenticationFilter.MemberPrincipal;
import com.capstone.web.posts.dto.PostDto;
import com.capstone.web.posts.dto.PostListRequest;
import com.capstone.web.posts.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

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
    public ResponseEntity<PostDto.Response> getPost(
            @PathVariable Long id,
            @AuthenticationPrincipal MemberPrincipal userPrincipal
    ) {
        Long viewer = userPrincipal != null ? userPrincipal.id() : null;
        PostDto.Response post = postService.getPostById(id, viewer);
        return ResponseEntity.ok(post);
    }

    @GetMapping
    public ResponseEntity<Page<PostDto.Response>> list(
            @RequestParam(required = false) Long boardId,
            @RequestParam(required = false) Long authorId,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size,
            @RequestParam(required = false) String searchType,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "createdAt") String sortBy
    ) {
        PostListRequest req = new PostListRequest();
        req.setBoardId(boardId);
        req.setAuthorId(authorId);
        req.setPage(page);
        req.setSize(size);
        req.setSearchType(searchType);
        req.setKeyword(keyword);
        req.setSortBy(sortBy);
        return ResponseEntity.ok(postService.list(req));
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

    @PostMapping("/{id}/like")
    public ResponseEntity<Object> toggleLike(
            @PathVariable Long id,
            @AuthenticationPrincipal MemberPrincipal userPrincipal
    ) {
        var result = postService.toggleLike(id, userPrincipal.id());
        return ResponseEntity.ok(result);
    }
}