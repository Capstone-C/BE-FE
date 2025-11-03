package com.capstone.web.comment.controller;

import com.capstone.web.auth.jwt.JwtAuthenticationFilter.MemberPrincipal;
import com.capstone.web.comment.dto.CommentDto;
import com.capstone.web.comment.dto.CommentQueryDto; // DTO 임포트 추가
import com.capstone.web.comment.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
// 👇 클래스 레벨의 @RequestMapping 제거
public class CommentController {

    private final CommentService commentService;

    // --- 기존의 게시글별 댓글 API (전체 경로 명시) ---
    @PostMapping("/api/posts/{postId}/comments")
    public ResponseEntity<Void> createComment(
            @PathVariable Long postId,
            @AuthenticationPrincipal MemberPrincipal userPrincipal,
            @Valid @RequestBody CommentDto.CreateRequest request
    ) {
        Long commentId = commentService.createComment(postId, userPrincipal.id(), request);
        URI location = URI.create(String.format("/api/posts/%d/comments/%d", postId, commentId));
        return ResponseEntity.created(location).build();
    }

    @GetMapping("/api/posts/{postId}/comments")
    public ResponseEntity<List<CommentDto.Response>> getCommentsByPost(@PathVariable Long postId) {
        List<CommentDto.Response> comments = commentService.getCommentsByPost(postId);
        return ResponseEntity.ok(comments);
    }

    @PutMapping("/api/posts/{postId}/comments/{commentId}")
    public ResponseEntity<Void> updateComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal MemberPrincipal userPrincipal,
            @Valid @RequestBody CommentDto.UpdateRequest request
    ) {
        commentService.updateComment(commentId, userPrincipal.id(), request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/api/posts/{postId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal MemberPrincipal userPrincipal
    ) {
        commentService.deleteComment(commentId, userPrincipal.id());
        return ResponseEntity.noContent().build();
    }

    // --- 👇 [추가] 작성자별 댓글 조회 API ---
    @GetMapping("/api/comments")
    public ResponseEntity<List<CommentQueryDto>> getCommentsByAuthor(
            @RequestParam("authorId") Long authorId
    ) {
        List<CommentQueryDto> comments = commentService.getCommentsByAuthor(authorId);
        return ResponseEntity.ok(comments);
    }
}