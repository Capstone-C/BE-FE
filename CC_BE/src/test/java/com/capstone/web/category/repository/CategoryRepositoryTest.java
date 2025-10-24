package com.capstone.web.category.repository;

import static org.assertj.core.api.Assertions.*;

import com.capstone.web.category.domain.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @DisplayName("카테고리를 성공적으로 저장한다")
    @Test
    void save_Success() {
        // given
        Category category = Category.builder()
                .name("레시피")
                .type(Category.CategoryType.RECIPE)
                .build();

        // when
        Category savedCategory = categoryRepository.save(category);

        // then
        assertThat(savedCategory.getId()).isNotNull();
        assertThat(savedCategory.getName()).isEqualTo("레시피");
    }

    @DisplayName("부모-자식 관계를 가진 카테고리를 저장한다")
    @Test
    void save_WithParentChildRelationship() {
        // given
        Category parent = categoryRepository.save(Category.builder()
                .name("요리")
                .type(Category.CategoryType.RECIPE)
                .build());

        Category child = Category.builder()
                .name("한식")
                .type(Category.CategoryType.RECIPE)
                .parent(parent) // 부모 카테고리 설정
                .build();

        // when
        Category savedChild = categoryRepository.save(child);

        // then
        assertThat(savedChild.getParent()).isNotNull();
        assertThat(savedChild.getParent().getId()).isEqualTo(parent.getId());
    }
    @DisplayName("부모 카테고리 삭제 시 자식 카테고리도 함께 삭제된다 (Cascade)")
    @Test
    void delete_CascadeToChildren() {
        // given
        Category parent = categoryRepository.save(Category.builder()
                .name("요리")
                .type(Category.CategoryType.RECIPE)
                .build());

        Category child = Category.builder()
                .name("한식")
                .type(Category.CategoryType.RECIPE)
                .parent(parent)
                .build();

        // 👇 이 코드를 추가하여 부모 객체에도 자식이 있음을 알려줍니다.
        parent.getChildren().add(child);

        // 부모-자식 관계가 설정된 후 자식을 저장합니다.
        categoryRepository.save(child);

        long parentId = parent.getId();
        long childId = child.getId();

        // when: 부모 카테고리만 삭제
        categoryRepository.deleteById(parentId);

        // then: 자식 카테고리도 함께 삭제되었는지 확인
        assertThat(categoryRepository.existsById(parentId)).isFalse();
        assertThat(categoryRepository.existsById(childId)).isFalse();
    }

}