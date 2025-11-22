package com.capstone.web.posts.dto;

import com.capstone.web.posts.domain.PostScrap;
import com.capstone.web.posts.domain.Posts;
import lombok.Getter;

import java.time.LocalDateTime;

public class PostScrapDto {

    @Getter
    public static class Response {
        private final Long scrapId;
        private final Long postId;
        private final String title;
        private final String authorName;
        private final String thumbnailUrl;
        private final int likeCount;
        private final int viewCount;
        private final LocalDateTime scrappedAt;
        private final LocalDateTime postCreatedAt;

        // 레시피 정보
        private final boolean isRecipe;
        private final Posts.DietType dietType;
        private final Posts.Difficulty difficulty;
        private final Integer cookTimeInMinutes;

        public Response(PostScrap scrap) {
            this.scrapId = scrap.getId();
            this.scrappedAt = scrap.getScrappedAt();

            Posts post = scrap.getPost();
            this.postId = post.getId();
            this.title = post.getTitle();
            this.authorName = post.getAuthorId().getNickname();
            this.likeCount = post.getLikeCount();
            this.viewCount = post.getViewCount();
            this.postCreatedAt = post.getCreatedAt();
            this.isRecipe = post.isRecipe();

            // 썸네일 URL (Posts 엔티티의 헬퍼 메서드 사용 가정)
            this.thumbnailUrl = post.getThumbnailUrl().orElse(null);

            this.dietType = post.getDietType();
            this.difficulty = post.getDifficulty();
            this.cookTimeInMinutes = post.getCookTimeInMinutes();
        }
    }
}