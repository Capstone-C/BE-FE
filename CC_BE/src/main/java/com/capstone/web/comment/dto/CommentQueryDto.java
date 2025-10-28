package com.capstone.web.comment.dto;

import com.capstone.web.comment.domain.Comment;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CommentQueryDto {

    private final Long commentId;
    private final String content;
    private final LocalDateTime createdAt;

    // 댓글이 달린 게시글 정보
    private final Long postId;
    private final String postTitle;

    public CommentQueryDto(Comment comment) {
        this.commentId = comment.getId();
        this.content = comment.getContent();
        this.createdAt = comment.getCreatedAt();
        this.postId = comment.getPost().getId();
        this.postTitle = comment.getPost().getTitle();
    }
}