package com.capstone.web.posts.dto;

import com.capstone.web.posts.domain.Posts;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class PostDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @NotNull(message = "카테고리 ID는 필수입니다.")
        private Long categoryId;
        @NotBlank(message = "제목은 비워둘 수 없습니다.")
        @Size(min = 5, max = 100, message = "제목은 5자 이상 100자 이하로 입력해주세요.")
        private String title;
        @NotBlank(message = "내용은 비워둘 수 없습니다.")
        @Size(min = 10, max = 10000, message = "내용은 10자 이상 10000자 이하로 입력해주세요.")
        private String content;
        private Posts.PostStatus status;
        @NotNull(message = "레시피 여부는 필수입니다.")
        private Boolean isRecipe;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        @NotBlank(message = "제목은 비워둘 수 없습니다.")
        @Size(min = 5, max = 100, message = "제목은 5자 이상 100자 이하로 입력해주세요.")
        private String title;
        @NotBlank(message = "내용은 비워둘 수 없습니다.")
        @Size(min = 10, max = 10000, message = "내용은 10자 이상 10000자 이하로 입력해주세요.")
        private String content;
        @NotNull(message = "카테고리 ID는 필수입니다.")
        private Long categoryId;
        @NotNull(message = "상태값은 필수입니다.")
        private Posts.PostStatus status;
        @NotNull(message = "레시피 여부는 필수입니다.")
        private Boolean isRecipe;
    }

    @Getter
    public static class Response {
        private final Long id;
        private final Long authorId;
        private final Long categoryId;
        private final String categoryName;
        private final String title;
        private final String content;
        private final Posts.PostStatus status;
        private final int viewCount;
        private final int likeCount;
        private final int commentCount;
        private final LocalDateTime createdAt;
        private final LocalDateTime updatedAt;
        private final Posts.TruthValue selected;
        private final Posts.TruthValue file;
        private final boolean isRecipe;
        private final Boolean likedByMe; // nullable when unauthenticated

        public Response(Posts post) {
            this.id = post.getId();
            this.authorId = post.getAuthorId().getId();
            this.categoryId = post.getCategory().getId();
            this.categoryName = post.getCategory().getName();
            this.title = post.getTitle();
            this.content = post.getContent();
            this.status = post.getStatus();
            this.viewCount = post.getViewCount();
            this.likeCount = post.getLikeCount();
            this.commentCount = post.getCommentCount();
            this.createdAt = post.getCreatedAt();
            this.updatedAt = post.getUpdatedAt();
            this.selected = post.getSelected();
            this.file = post.getFile();
            this.isRecipe = post.isRecipe();
            this.likedByMe = null; // default; service can decorate when member known
        }

        public Response(Posts post, Boolean likedByMe) {
            this.id = post.getId();
            this.authorId = post.getAuthorId().getId();
            this.categoryId = post.getCategory().getId();
            this.categoryName = post.getCategory().getName();
            this.title = post.getTitle();
            this.content = post.getContent();
            this.status = post.getStatus();
            this.viewCount = post.getViewCount();
            this.likeCount = post.getLikeCount();
            this.commentCount = post.getCommentCount();
            this.createdAt = post.getCreatedAt();
            this.updatedAt = post.getUpdatedAt();
            this.selected = post.getSelected();
            this.file = post.getFile();
            this.isRecipe = post.isRecipe();
            this.likedByMe = likedByMe;
        }
    }

    @Getter
    @AllArgsConstructor
    public static class IdResponse {
        private Long id;
    }
}
