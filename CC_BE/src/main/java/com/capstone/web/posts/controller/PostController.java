package com.capstone.web.posts.controller;

import com.capstone.web.auth.jwt.JwtAuthenticationFilter.MemberPrincipal;
import com.capstone.web.posts.dto.PostDto;
import com.capstone.web.posts.dto.PostListRequest;
import com.capstone.web.posts.dto.PostComparisonDto;
import com.capstone.web.posts.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // ==========================================
    //  1. 게시글 생성 (Create)
    // ==========================================

    // [옵션 A] JSON만 보낼 때 (기존 방식 유지 - 이미지 없음)
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> createPostJson(
            @AuthenticationPrincipal MemberPrincipal userPrincipal,
            @Valid @RequestBody PostDto.CreateRequest request
    ) {
        // 이미지 파일 자리에 null 전달
        Long postId = postService.createPost(userPrincipal.id(), request, null, null);
        return ResponseEntity.created(URI.create("/api/posts/" + postId)).build();
    }

    // [옵션 B] 파일과 함께 보낼 때 (신규 방식 - multipart/form-data)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> createPostMultipart(
            @AuthenticationPrincipal MemberPrincipal userPrincipal,
            @Valid @RequestPart("request") PostDto.CreateRequest request,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnailFile,
            @RequestPart(value = "files", required = false) List<MultipartFile> files // (추가) 다중 파일
    ) {
        // 실제 이미지 파일 전달
        Long postId = postService.createPost(userPrincipal.id(), request, thumbnailFile, files);
        return ResponseEntity.created(URI.create("/api/posts/" + postId)).build();
    }

    // ==========================================
    //  2. 게시글 조회 (Read)
    // ==========================================

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

    // ==========================================
    //  3. 게시글 수정 (Update)
    // ==========================================

    // [옵션 A] JSON만 보낼 때 (기존 방식)
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updatePostJson(
            @PathVariable Long id,
            @AuthenticationPrincipal MemberPrincipal userPrincipal,
            @Valid @RequestBody PostDto.UpdateRequest request
    ) {
        postService.updatePost(id, userPrincipal.id(), request, null, null);
        return ResponseEntity.ok().build();
    }

    // [옵션 B] 파일과 함께 보낼 때 (신규 방식)
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updatePostMultipart(
            @PathVariable Long id,
            @AuthenticationPrincipal MemberPrincipal userPrincipal,
            @Valid @RequestPart("request") PostDto.UpdateRequest request,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnailFile,
            @RequestPart(value = "files", required = false) List<MultipartFile> files // (추가) 다중 파일
    ) {
        postService.updatePost(id, userPrincipal.id(), request, thumbnailFile, files);
        return ResponseEntity.ok().build();
    }

    // ==========================================
    //  4. 기타 기능 (Delete, Like, Compare)
    // ==========================================

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long id,
            @AuthenticationPrincipal MemberPrincipal userPrincipal
    ) {
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

    @GetMapping("/{postId}/compare-refrigerator")
    public ResponseEntity<PostComparisonDto.Response> compareWithRefrigerator(
            @PathVariable Long postId,
            @AuthenticationPrincipal MemberPrincipal userPrincipal
    ) {
        PostComparisonDto.Response response = postService.compareWithRefrigerator(postId, userPrincipal.id());
        return ResponseEntity.ok(response);
    }
}