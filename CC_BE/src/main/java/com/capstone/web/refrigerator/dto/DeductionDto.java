package com.capstone.web.refrigerator.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

/**
 * REF-08: 레시피 재료 자동 차감 관련 DTO
 */
public class DeductionDto {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "재료 차감 미리보기 응답")
    public static class DeductPreviewResponse {
        
        @Schema(description = "레시피 ID")
        private Long recipeId;
        
        @Schema(description = "레시피 이름")
        private String recipeName;
        
        @Schema(description = "재료별 차감 가능 여부")
        private List<IngredientDeductionStatus> ingredients;
        
        @Schema(description = "차감 가능 여부 (모든 필수 재료 충분)")
        private boolean canProceed;
        
        @Schema(description = "경고 메시지 목록")
        private List<String> warnings;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "재료별 차감 상태")
    public static class IngredientDeductionStatus {
        
        @Schema(description = "재료 이름")
        private String name;
        
        @Schema(description = "필요량", example = "200g")
        private String requiredAmount;
        
        @Schema(description = "현재 보유량", example = "500g")
        private String currentAmount;
        
        @Schema(description = "현재 수량 (숫자)")
        private Integer currentQuantity;
        
        @Schema(description = "상태", allowableValues = {"OK", "INSUFFICIENT", "NOT_FOUND"})
        private DeductionStatus status;
        
        @Schema(description = "필수 재료 여부")
        private Boolean isRequired;
        
        @Schema(description = "경고 메시지")
        private String message;
    }

    public enum DeductionStatus {
        OK,          // 충분함
        INSUFFICIENT, // 부족함
        NOT_FOUND    // 없음
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "재료 차감 요청")
    public static class DeductRequest {
        
        @NotNull(message = "레시피 ID는 필수입니다")
        @Schema(description = "레시피 ID", required = true)
        private Long recipeId;
        
        @Schema(description = "경고 무시 여부 (부족한 재료가 있어도 차감)", example = "false")
        @Builder.Default
        private Boolean ignoreWarnings = false;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "재료 차감 응답")
    public static class DeductResponse {
        
        @Schema(description = "레시피 ID")
        private Long recipeId;
        
        @Schema(description = "레시피 이름")
        private String recipeName;
        
        @Schema(description = "차감 성공 개수")
        private int successCount;
        
        @Schema(description = "차감 실패 개수")
        private int failedCount;
        
        @Schema(description = "차감된 재료 목록")
        private List<DeductedIngredient> deductedIngredients;
        
        @Schema(description = "실패한 재료 목록")
        private List<String> failedIngredients;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "차감된 재료")
    public static class DeductedIngredient {
        
        @Schema(description = "재료 이름")
        private String name;
        
        @Schema(description = "차감 전 수량")
        private Integer previousQuantity;
        
        @Schema(description = "차감 후 수량")
        private Integer newQuantity;
    }
}
