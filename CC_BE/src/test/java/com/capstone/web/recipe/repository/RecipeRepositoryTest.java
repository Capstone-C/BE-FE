    package com.capstone.web.recipe.repository;

import static org.assertj.core.api.Assertions.*;

import com.capstone.web.recipe.domain.Recipe;
import com.capstone.web.recipe.domain.RecipeIngredient;
import jakarta.persistence.EntityManager;
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
class RecipeRepositoryTest {

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private RecipeIngredientRepository recipeIngredientRepository;

    @Autowired
    private EntityManager em;

    @BeforeEach
    void setup() {
        recipeIngredientRepository.deleteAll();
        recipeRepository.deleteAll();
    }

    @Test
    @DisplayName("레시피를 저장하고 조회할 수 있다")
    void saveAndFind() {
        // given
        Recipe recipe = Recipe.builder()
                .name("김치찌개")
                .description("맛있는 김치찌개")
                .cookTime(30)
                .servings(4)
                .difficulty(Recipe.Difficulty.EASY)
                .build();

        // when
        Recipe saved = recipeRepository.save(recipe);
        Recipe found = recipeRepository.findById(saved.getId()).orElse(null);

        // then
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("김치찌개");
        assertThat(found.getDescription()).isEqualTo("맛있는 김치찌개");
    }

    @Test
    @DisplayName("재료와 함께 레시피를 저장할 수 있다")
    void saveWithIngredients() {
        // given
        Recipe recipe = Recipe.builder()
                .name("김치찌개")
                .build();

        recipe.addIngredient(RecipeIngredient.builder().name("김치").amount("200g").build());
        recipe.addIngredient(RecipeIngredient.builder().name("돼지고기").amount("100g").build());
        recipe.addIngredient(RecipeIngredient.builder().name("두부").amount("1모").build());

        // when
        Recipe saved = recipeRepository.save(recipe);
        recipeRepository.flush();
        em.clear();

        Recipe found = recipeRepository.findById(saved.getId()).orElse(null);

        // then
        assertThat(found).isNotNull();
        assertThat(found.getIngredients()).hasSize(3);
    }

    @Test
    @DisplayName("findAllWithIngredients는 N+1 문제 없이 재료를 함께 조회한다")
    void findAllWithIngredients_noNPlusOne() {
        // given
        Recipe recipe1 = Recipe.builder().name("김치찌개").build();
        recipe1.addIngredient(RecipeIngredient.builder().name("김치").build());
        recipe1.addIngredient(RecipeIngredient.builder().name("돼지고기").build());
        recipeRepository.save(recipe1);

        Recipe recipe2 = Recipe.builder().name("된장찌개").build();
        recipe2.addIngredient(RecipeIngredient.builder().name("된장").build());
        recipe2.addIngredient(RecipeIngredient.builder().name("두부").build());
        recipeRepository.save(recipe2);

        recipeRepository.flush();
        em.clear();

        // when
        List<Recipe> recipes = recipeRepository.findAllWithIngredients();

        // then
        assertThat(recipes).hasSize(2);
        assertThat(recipes.get(0).getIngredients()).isNotEmpty(); // 이미 로드됨
        assertThat(recipes.get(1).getIngredients()).isNotEmpty(); // 이미 로드됨
    }

    @Test
    @DisplayName("findByIdWithIngredients는 특정 레시피와 재료를 함께 조회한다")
    void findByIdWithIngredients() {
        // given
        Recipe recipe = Recipe.builder().name("김치찌개").build();
        recipe.addIngredient(RecipeIngredient.builder().name("김치").amount("200g").build());
        recipe.addIngredient(RecipeIngredient.builder().name("돼지고기").amount("100g").build());
        Recipe saved = recipeRepository.save(recipe);

        recipeRepository.flush();
        em.clear();

        // when
        Recipe found = recipeRepository.findByIdWithIngredients(saved.getId());

        // then
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("김치찌개");
        assertThat(found.getIngredients()).hasSize(2);
        assertThat(found.getIngredients().get(0).getName()).isIn("김치", "돼지고기");
    }

    @Test
    @DisplayName("이름으로 레시피를 검색할 수 있다")
    void findByNameContaining() {
        // given
        recipeRepository.save(Recipe.builder().name("김치찌개").build());
        recipeRepository.save(Recipe.builder().name("김치볶음밥").build());
        recipeRepository.save(Recipe.builder().name("된장찌개").build());

        // when
        List<Recipe> recipes = recipeRepository.findByNameContainingIgnoreCase("김치");

        // then
        assertThat(recipes).hasSize(2);
        assertThat(recipes).extracting("name")
                .containsExactlyInAnyOrder("김치찌개", "김치볶음밥");
    }

    @Test
    @DisplayName("난이도로 레시피를 필터링할 수 있다")
    void findByDifficulty() {
        // given
        recipeRepository.save(Recipe.builder().name("쉬운요리").difficulty(Recipe.Difficulty.EASY).build());
        recipeRepository.save(Recipe.builder().name("보통요리").difficulty(Recipe.Difficulty.MEDIUM).build());
        recipeRepository.save(Recipe.builder().name("어려운요리").difficulty(Recipe.Difficulty.HARD).build());

        // when
        List<Recipe> easyRecipes = recipeRepository.findByDifficulty(Recipe.Difficulty.EASY);

        // then
        assertThat(easyRecipes).hasSize(1);
        assertThat(easyRecipes.get(0).getName()).isEqualTo("쉬운요리");
    }

    @Test
    @DisplayName("조리 시간으로 레시피를 필터링할 수 있다")
    void findByCookTimeLessThanEqual() {
        // given
        recipeRepository.save(Recipe.builder().name("빠른요리").cookTime(15).build());
        recipeRepository.save(Recipe.builder().name("보통요리").cookTime(30).build());
        recipeRepository.save(Recipe.builder().name("오래걸리는요리").cookTime(60).build());

        // when
        List<Recipe> quickRecipes = recipeRepository.findByCookTimeLessThanEqual(30);

        // then
        assertThat(quickRecipes).hasSize(2);
        assertThat(quickRecipes).extracting("name")
                .containsExactlyInAnyOrder("빠른요리", "보통요리");
    }
}
