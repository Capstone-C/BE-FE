package com.capstone.web.posts.dto;

import com.capstone.web.media.domain.Media; // (추가)
import com.capstone.web.posts.domain.Posts;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Comparator; // (추가)
import java.util.List;
import java.util.stream.Collectors;

public class PostDto {

    @Getter
    @Setter
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

        // --- 레시피 기본 정보 ---
        private Posts.DietType dietType;
        private Integer cookTimeInMinutes;
        private Integer servings;
        private Posts.Difficulty difficulty;
        // ----------------------------

        @Valid
        private List<PostIngredientDto.Request> ingredients;
    }

    @Getter
    @Setter
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

        // --- 레시피 기본 정보 ---
        private Posts.DietType dietType;
        private Integer cookTimeInMinutes;
        private Integer servings;
        private Posts.Difficulty difficulty;
        // ----------------------------

        @Valid
        private List<PostIngredientDto.Request> ingredients;
    }

    @Getter
    public static class Response {
        private final Long id;
        private final Long authorId;
        private final String authorName;
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
        private final Boolean isRecipe;
        private final Boolean likedByMe;

        // --- 레시피 기본 정보 ---
        private final Posts.DietType dietType;
        private final Integer cookTimeInMinutes;
        private final Integer servings;
        private final Posts.Difficulty difficulty;
        // ----------------------------

        // 1. 썸네일 이미지 (대표)
        private final String thumbnailUrl;

        // 2. (추가) 추가 이미지 목록
        private final List<String> imageUrls;

        private final List<PostIngredientDto.Response> ingredients;

        // 생성자 1
        public Response(Posts post) {
            this.id = post.getId();
            this.authorId = post.getAuthorId().getId();
            this.authorName = post.getAuthorId().getNickname();
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
            this.likedByMe = null;

            this.dietType = post.getDietType();
            this.cookTimeInMinutes = post.getCookTimeInMinutes();
            this.servings = post.getServings();
            this.difficulty = post.getDifficulty();

            // 썸네일 (순서 0번) 추출
            this.thumbnailUrl = post.getThumbnailUrl().orElse(null);

            // (추가) 나머지 이미지들 (순서 1번 이상) 추출
            this.imageUrls = post.getMedia().stream()
                    .filter(m -> m.getOwnerType() == Media.OwnerType.post && m.getOrderNum() > 0) // 0번(썸네일) 제외
                    .sorted(Comparator.comparingInt(Media::getOrderNum)) // 순서대로 정렬
                    .map(Media::getUrl)
                    .collect(Collectors.toList());

            this.ingredients = post.getIngredients().stream()
                    .map(PostIngredientDto.Response::new)
                    .collect(Collectors.toList());
        }

        // 생성자 2
        public Response(Posts post, Boolean likedByMe) {
            this.id = post.getId();
            this.authorId = post.getAuthorId().getId();
            this.authorName = post.getAuthorId().getNickname();
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

            this.dietType = post.getDietType();
            this.cookTimeInMinutes = post.getCookTimeInMinutes();
            this.servings = post.getServings();
            this.difficulty = post.getDifficulty();

            // 썸네일 추출
            this.thumbnailUrl = post.getThumbnailUrl().orElse(null);

            // (추가) 나머지 이미지들 추출
            this.imageUrls = post.getMedia().stream()
                    .filter(m -> m.getOwnerType() == Media.OwnerType.post && m.getOrderNum() > 0)
                    .sorted(Comparator.comparingInt(Media::getOrderNum))
                    .map(Media::getUrl)
                    .collect(Collectors.toList());

            this.ingredients = post.getIngredients().stream()
                    .map(PostIngredientDto.Response::new)
                    .collect(Collectors.toList());
        }
    }

    @Getter
    @AllArgsConstructor
    public static class IdResponse {
        private Long id;
    }
}