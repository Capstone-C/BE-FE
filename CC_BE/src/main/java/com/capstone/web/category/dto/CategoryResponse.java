package com.capstone.web.category.dto;

import com.capstone.web.category.domain.Category;
import lombok.Getter;

@Getter
public class CategoryResponse {
    private final Long id;
    private final String name;
    private final Category.CategoryType type;
    private final Long parentId;

    public CategoryResponse(Category category) {
        this.id = category.getId();
        this.name = category.getName();
        this.type = category.getType();
        this.parentId = category.getParent() != null ? category.getParent().getId() : null;
    }
}
