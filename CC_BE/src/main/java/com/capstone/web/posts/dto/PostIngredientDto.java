package com.capstone.web.posts.dto;

import com.capstone.web.posts.domain.PostIngredient;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class PostIngredientDto { // (이름 변경)

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime expirationDate;

        @NotBlank(message = "재료명은 필수입니다.")
        private String name;

        private Long quantity;
        private String unit;
        private String memo;
    }

    @Getter
    @AllArgsConstructor
    public static class Response {
        private Long id;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime expirationDate;
        private String name;
        private Long quantity;
        private String unit;
        private String memo;

        // 엔티티 -> DTO 변환 생성자
        public Response(PostIngredient entity) { // (수정)
            this.id = entity.getId();
            this.expirationDate = entity.getExpirationDate();
            this.name = entity.getName();
            this.quantity = entity.getQuantity();
            this.unit = entity.getUnit();
            this.memo = entity.getMemo();
        }
    }
}