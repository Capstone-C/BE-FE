package com.capstone.web.category.service;

import com.capstone.web.category.domain.Category;
import com.capstone.web.category.dto.CategoryRequest;
import com.capstone.web.category.dto.CategoryResponse;
import com.capstone.web.category.exception.CategoryNotFoundException;
import com.capstone.web.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional
    public Long createCategory(CategoryRequest request) {
        Category parent = null;
        if (request.getParentId() != null) {
            parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new CategoryNotFoundException("부모 카테고리를 찾을 수 없습니다. ID: " + request.getParentId()));
        }

        Category category = categoryRepository.save(request.toEntity(parent));
        return category.getId();
    }

    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("카테고리를 찾을 수 없습니다. ID: " + id));
        return new CategoryResponse(category);
    }

    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(CategoryResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new CategoryNotFoundException("삭제할 카테고리를 찾을 수 없습니다. ID: " + id);
        }
        categoryRepository.deleteById(id);
    }
}
