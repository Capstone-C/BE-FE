package com.capstone.web.category.dto;

import com.capstone.web.category.domain.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor

public class CategoryRequest {

    @NotBlank(message = "카테고리 이름은 필수입니다.")
    private String name;

    @NotNull(message = "카테고리 타입은 필수입니다.")
    private Category.CategoryType type;

    private Long parentId; // 요청에서는 ID로 받음

    public Category toEntity(Category parent) {
        return Category.builder()
                .name(name)
                .type(type)
                .parent(parent)
                .build();
    }
}
