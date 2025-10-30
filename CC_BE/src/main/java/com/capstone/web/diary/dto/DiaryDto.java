package com.capstone.web.diary.dto;

import com.capstone.web.diary.domain.Diary;
import com.capstone.web.diary.domain.Diary.MealType;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DiaryDto {

    /**
     * 식단 기록 생성 요청 DTO
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @NotNull(message = "날짜는 필수입니다")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate date;

        @NotNull(message = "식사 타입은 필수입니다")
        private MealType mealType;

        @NotBlank(message = "메뉴/내용은 필수입니다")
        @Size(max = 500, message = "메뉴/내용은 500자 이하이어야 합니다")
        private String content;

        private String imageUrl;

        private Long recipeId;
    }

    /**
     * 식단 기록 수정 요청 DTO
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        @NotNull(message = "식사 타입은 필수입니다")
        private MealType mealType;

        @NotBlank(message = "메뉴/내용은 필수입니다")
        @Size(max = 500, message = "메뉴/내용은 500자 이하이어야 합니다")
        private String content;

        private String imageUrl;

        private Long recipeId;
    }

    /**
     * 식단 기록 응답 DTO
     */
    @Getter
    public static class Response {
        private final Long id;
        private final Long memberId;
        @JsonFormat(pattern = "yyyy-MM-dd")
        private final LocalDate date;
        private final MealType mealType;
        private final String content;
        private final String imageUrl;
        private final Long recipeId;
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private final LocalDateTime createdAt;
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private final LocalDateTime updatedAt;

        @Builder
        public Response(Diary diary) {
            this.id = diary.getId();
            this.memberId = diary.getMember().getId();
            this.date = diary.getDate();
            this.mealType = diary.getMealType();
            this.content = diary.getContent();
            this.imageUrl = diary.getImageUrl();
            this.recipeId = diary.getRecipeId();
            this.createdAt = diary.getCreatedAt();
            this.updatedAt = diary.getUpdatedAt();
        }
    }

    /**
     * 월별 캘린더 응답 DTO
     */
    @Getter
    public static class MonthlyCalendarResponse {
        private final int year;
        private final int month;
        private final List<DailyEntry> dailyEntries;

        @Builder
        public MonthlyCalendarResponse(int year, int month, List<Diary> diaryList) {
            this.year = year;
            this.month = month;
            
            // 날짜별로 그룹핑
            Map<LocalDate, List<Diary>> groupedByDate = diaryList.stream()
                .collect(Collectors.groupingBy(Diary::getDate));
            
            this.dailyEntries = groupedByDate.entrySet().stream()
                .map(entry -> new DailyEntry(entry.getKey(), entry.getValue()))
                .sorted((a, b) -> a.getDate().compareTo(b.getDate()))
                .collect(Collectors.toList());
        }
    }

    /**
     * 일일 식단 요약 DTO (캘린더용)
     */
    @Getter
    public static class DailyEntry {
        @JsonFormat(pattern = "yyyy-MM-dd")
        private final LocalDate date;
        private final boolean hasBreakfast;
        private final boolean hasLunch;
        private final boolean hasDinner;
        private final boolean hasSnack;
        private final String thumbnailUrl; // 대표 이미지 (첫 번째 식단의 이미지)

        public DailyEntry(LocalDate date, List<Diary> diaries) {
            this.date = date;
            this.hasBreakfast = diaries.stream().anyMatch(d -> d.getMealType() == MealType.BREAKFAST);
            this.hasLunch = diaries.stream().anyMatch(d -> d.getMealType() == MealType.LUNCH);
            this.hasDinner = diaries.stream().anyMatch(d -> d.getMealType() == MealType.DINNER);
            this.hasSnack = diaries.stream().anyMatch(d -> d.getMealType() == MealType.SNACK);
            this.thumbnailUrl = diaries.stream()
                .filter(d -> d.getImageUrl() != null)
                .findFirst()
                .map(Diary::getImageUrl)
                .orElse(null);
        }
    }
}
