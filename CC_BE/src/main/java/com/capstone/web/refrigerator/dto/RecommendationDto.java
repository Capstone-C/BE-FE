package com.capstone.web.refrigerator.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

/**
 * REF-07: 레시피 추천 관련 DTO
 */
public class RecommendationDto {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "레시피 추천 응답")
    public static class RecommendationResponse {
        
        @Schema(description = "추천 레시피 목록")
        private List<RecommendedRecipe> recommendations;
        
        @Schema(description = "전체 추천 개수")
        private int totalCount;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "추천된 레시피")
    public static class RecommendedRecipe {
        
        @Schema(description = "레시피 ID")
        private Long recipeId;
        
        @Schema(description = "레시피 이름")
        private String recipeName;
        
        @Schema(description = "레시피 설명")
        private String description;
        
        @Schema(description = "조리 시간 (분)")
        private Integer cookTime;
        
        @Schema(description = "인분")
        private Integer servings;
        
        @Schema(description = "난이도")
        private String difficulty;
        
        @Schema(description = "이미지 URL")
        private String imageUrl;
        
        @Schema(description = "재료 매칭률 (0-100)", example = "85.5")
        private Double matchRate;
        
        @Schema(description = "보유 중인 재료 목록")
        private List<String> matchedIngredients;
        
        @Schema(description = "부족한 재료 목록")
        private List<MissingIngredient> missingIngredients;
        
        @Schema(description = "전체 필요 재료 개수")
        private int totalIngredientsCount;
        
        @Schema(description = "보유 재료 개수")
        private int matchedIngredientsCount;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "부족한 재료")
    public static class MissingIngredient {
        
        @Schema(description = "재료 이름")
        private String name;
        
        @Schema(description = "필요량", example = "200g")
        private String amount;
        
        @Schema(description = "필수 재료 여부")
        private Boolean isRequired;
    }
}
