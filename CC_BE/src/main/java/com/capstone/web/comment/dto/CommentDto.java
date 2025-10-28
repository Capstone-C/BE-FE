package com.capstone.web.comment.dto;

import com.capstone.web.comment.domain.Comment;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CommentDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        private Long parentId; // 대댓글일 경우 부모 댓글 ID

        @NotBlank(message = "댓글 내용은 비워둘 수 없습니다.")
        private String content;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        @NotBlank(message = "댓글 내용은 비워둘 수 없습니다.")
        private String content;
    }

    @Getter
    public static class Response {
        private final Long id;
        private final Long authorId;
        private final String authorNickname;
        private final String content;
        private final int likeCount;
        private final LocalDateTime createdAt;
        private final int depth;
        private List<Response> children = new ArrayList<>();

        public Response(Comment comment) {
            this.id = comment.getId();
            this.authorId = comment.getAuthor().getId();
            this.authorNickname = comment.getAuthor().getNickname();
            this.content = comment.getContent();
            this.likeCount = comment.getLikeCount();
            this.createdAt = comment.getCreatedAt();
            this.depth = comment.getDepth();
        }
    }
}