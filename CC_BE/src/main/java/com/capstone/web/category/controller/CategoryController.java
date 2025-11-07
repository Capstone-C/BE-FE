package com.capstone.web.category.controller;

import com.capstone.web.category.dto.CategoryRequest;
import com.capstone.web.category.dto.CategoryResponse;
import com.capstone.web.category.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
// 'ServletUriComponentsBuilder' import가 필요합니다.
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<Void> createCategory(@Valid @RequestBody CategoryRequest request) {
        Long categoryId = categoryService.createCategory(request);

        // [수정된 부분]
        // 1. ServletUriComponentsBuilder로 URI 컴포넌트를 빌드합니다.
        String path = ServletUriComponentsBuilder
                .fromCurrentRequest() // (http://localhost/api/v1/categories)
                .path("/{id}")        // (/3)
                .buildAndExpand(categoryId)
                .getPath(); // 2. .getPath()를 호출하여 '/api/v1/categories/3' 부분만 추출합니다.

        // 3. 추출한 경로 문자열로 URI를 생성합니다.
        URI location = URI.create(path);

        return ResponseEntity.created(location).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategory(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}