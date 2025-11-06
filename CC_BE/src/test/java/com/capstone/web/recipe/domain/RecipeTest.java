package com.capstone.web.recipe.domain;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RecipeTest {

    @Test
    @DisplayName("레시피 생성 시 빌더로 정상 생성된다")
    void createRecipe() {
        // when
        Recipe recipe = Recipe.builder()
                .name("김치찌개")
                .description("맛있는 김치찌개")
                .cookTime(30)
                .servings(4)
                .difficulty(Recipe.Difficulty.EASY)
                .imageUrl("http://example.com/image.jpg")
                .instructions("1. 김치를 볶는다\n2. 물을 넣는다\n3. 끓인다")
                .build();

        // then
        assertThat(recipe.getName()).isEqualTo("김치찌개");
        assertThat(recipe.getDescription()).isEqualTo("맛있는 김치찌개");
        assertThat(recipe.getCookTime()).isEqualTo(30);
        assertThat(recipe.getServings()).isEqualTo(4);
        assertThat(recipe.getDifficulty()).isEqualTo(Recipe.Difficulty.EASY);
        assertThat(recipe.getImageUrl()).isEqualTo("http://example.com/image.jpg");
        assertThat(recipe.getInstructions()).isEqualTo("1. 김치를 볶는다\n2. 물을 넣는다\n3. 끓인다");
        assertThat(recipe.getIngredients()).isEmpty();
    }

    @Test
    @DisplayName("재료 추가 시 양방향 관계가 설정된다")
    void addIngredient() {
        // given
        Recipe recipe = Recipe.builder()
                .name("김치찌개")
                .build();

        RecipeIngredient ingredient = RecipeIngredient.builder()
                .name("김치")
                .amount("200g")
                .unit("g")
                .isRequired(true)
                .build();

        // when
        recipe.addIngredient(ingredient);

        // then
        assertThat(recipe.getIngredients()).hasSize(1);
        assertThat(recipe.getIngredients().get(0)).isEqualTo(ingredient);
        assertThat(ingredient.getRecipe()).isEqualTo(recipe);
    }

    @Test
    @DisplayName("재료 제거 시 양방향 관계가 해제된다")
    void removeIngredient() {
        // given
        Recipe recipe = Recipe.builder()
                .name("김치찌개")
                .build();

        RecipeIngredient ingredient = RecipeIngredient.builder()
                .name("김치")
                .build();

        recipe.addIngredient(ingredient);

        // when
        recipe.removeIngredient(ingredient);

        // then
        assertThat(recipe.getIngredients()).isEmpty();
        assertThat(ingredient.getRecipe()).isNull();
    }

    @Test
    @DisplayName("전체 재료 개수를 정확히 반환한다")
    void getTotalIngredientCount() {
        // given
        Recipe recipe = Recipe.builder()
                .name("김치찌개")
                .build();

        recipe.addIngredient(RecipeIngredient.builder().name("김치").build());
        recipe.addIngredient(RecipeIngredient.builder().name("돼지고기").build());
        recipe.addIngredient(RecipeIngredient.builder().name("두부").build());

        // when
        int count = recipe.getTotalIngredientCount();

        // then
        assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("재료가 없을 때 개수는 0이다")
    void getTotalIngredientCount_empty() {
        // given
        Recipe recipe = Recipe.builder()
                .name("김치찌개")
                .build();

        // when
        int count = recipe.getTotalIngredientCount();

        // then
        assertThat(count).isZero();
    }

    @Test
    @DisplayName("난이도 EASY, MEDIUM, HARD 모두 설정 가능하다")
    void setDifficulty() {
        // given & when & then
        Recipe easy = Recipe.builder().name("쉬운요리").difficulty(Recipe.Difficulty.EASY).build();
        Recipe medium = Recipe.builder().name("보통요리").difficulty(Recipe.Difficulty.MEDIUM).build();
        Recipe hard = Recipe.builder().name("어려운요리").difficulty(Recipe.Difficulty.HARD).build();

        assertThat(easy.getDifficulty()).isEqualTo(Recipe.Difficulty.EASY);
        assertThat(medium.getDifficulty()).isEqualTo(Recipe.Difficulty.MEDIUM);
        assertThat(hard.getDifficulty()).isEqualTo(Recipe.Difficulty.HARD);
    }
}
