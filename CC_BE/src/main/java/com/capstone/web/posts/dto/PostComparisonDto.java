package com.capstone.web.posts.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

public class PostComparisonDto {

    @Getter
    @Builder
    public static class Response {
        private Long postId;
        private String postTitle;
        private List<ComparedIngredient> ingredients;
        private int totalNeeded; // 총 필요 재료 수
        private int ownedCount;  // 보유 재료 수
        private int missingCount; // 부족 재료 수
    }

    @Getter
    @Builder
    public static class ComparedIngredient {
        private String name; // 필요 재료명
        private String amount; // 필요량 (예: "200g")
        private ComparisonStatus status; // 상태 (OWNED, MISSING)
    }

    public enum ComparisonStatus {
        OWNED,   // 보유 중
        MISSING  // 부족함
    }
}