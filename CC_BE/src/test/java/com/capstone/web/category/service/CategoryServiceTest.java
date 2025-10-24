package com.capstone.web.category.service;

import static org.assertj.core.api.Assertions.*;

import com.capstone.web.category.domain.Category;
import com.capstone.web.category.dto.CategoryRequest;
import com.capstone.web.category.dto.CategoryResponse;
import com.capstone.web.category.exception.CategoryNotFoundException;
import com.capstone.web.category.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CategoryServiceTest {

    @Autowired private CategoryService categoryService;
    @Autowired private CategoryRepository categoryRepository;

    @BeforeEach
    void setup() {
        categoryRepository.deleteAll();
    }

    @DisplayName("새로운 카테고리를 성공적으로 생성한다")
    @Test
    void createCategory_Success() {
        // given
        CategoryRequest request = new CategoryRequest("새 카테고리", Category.CategoryType.FREE, null);

        // when
        Long categoryId = categoryService.createCategory(request);

        // then
        assertThat(categoryId).isNotNull();
        assertThat(categoryRepository.existsById(categoryId)).isTrue();
    }

    @DisplayName("존재하지 않는 부모 카테고리 ID로 생성 시 예외가 발생한다")
    @Test
    void createCategory_Fail_ParentNotFound() {
        // given
        Long nonExistentParentId = 999L;
        CategoryRequest request = new CategoryRequest("자식 카테고리", Category.CategoryType.FREE, nonExistentParentId);

        // when & then
        assertThatThrownBy(() -> categoryService.createCategory(request))
                .isInstanceOf(CategoryNotFoundException.class)
                .hasMessageContaining("부모 카테고리를 찾을 수 없습니다.");
    }

    @DisplayName("ID로 카테고리를 성공적으로 조회한다")
    @Test
    void getCategoryById_Success() {
        // given
        Category saved = categoryRepository.save(Category.builder().name("조회용").type(Category.CategoryType.QA).build());

        // when
        CategoryResponse response = categoryService.getCategoryById(saved.getId());

        // then
        assertThat(response.getName()).isEqualTo("조회용");
        assertThat(response.getType()).isEqualTo(Category.CategoryType.QA);
    }

    @DisplayName("존재하지 않는 ID로 카테고리 조회 시 예외가 발생한다")
    @Test
    void getCategoryById_Fail_NotFound() {
        // given
        Long nonExistentId = 999L;

        // when & then
        assertThatThrownBy(() -> categoryService.getCategoryById(nonExistentId))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    @DisplayName("카테고리를 성공적으로 삭제한다")
    @Test
    void deleteCategory_Success() {
        // given
        Category saved = categoryRepository.save(Category.builder().name("삭제용").type(Category.CategoryType.FREE).build());

        // when
        categoryService.deleteCategory(saved.getId());

        // then
        assertThat(categoryRepository.existsById(saved.getId())).isFalse();
    }
}