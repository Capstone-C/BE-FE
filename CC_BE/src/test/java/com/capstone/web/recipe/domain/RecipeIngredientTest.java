package com.capstone.web.recipe.domain;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RecipeIngredientTest {

    @Test
    @DisplayName("재료 생성 시 빌더로 정상 생성된다")
    void createRecipeIngredient() {
        // when
        RecipeIngredient ingredient = RecipeIngredient.builder()
                .name("양파")
                .amount("1개")
                .unit("개")
                .isRequired(true)
                .build();

        // then
        assertThat(ingredient.getName()).isEqualTo("양파");
        assertThat(ingredient.getAmount()).isEqualTo("1개");
        assertThat(ingredient.getUnit()).isEqualTo("개");
        assertThat(ingredient.getIsRequired()).isTrue();
    }

    @Test
    @DisplayName("필수 여부 기본값은 true이다")
    void defaultIsRequired() {
        // when
        RecipeIngredient ingredient = RecipeIngredient.builder()
                .name("양파")
                .build();

        // then
        assertThat(ingredient.getIsRequired()).isTrue();
    }

    @Test
    @DisplayName("선택 재료로 설정할 수 있다")
    void optionalIngredient() {
        // when
        RecipeIngredient ingredient = RecipeIngredient.builder()
                .name("고춧가루")
                .isRequired(false)
                .build();

        // then
        assertThat(ingredient.getIsRequired()).isFalse();
    }

    @Test
    @DisplayName("레시피와 재료명만으로 생성 가능하다")
    void createWithRecipeAndName() {
        // given
        Recipe recipe = Recipe.builder()
                .name("김치찌개")
                .build();

        // when
        RecipeIngredient ingredient = new RecipeIngredient(recipe, "김치");

        // then
        assertThat(ingredient.getRecipe()).isEqualTo(recipe);
        assertThat(ingredient.getName()).isEqualTo("김치");
        assertThat(ingredient.getIsRequired()).isTrue();
    }

    @Test
    @DisplayName("모든 필드를 포함한 생성자로 생성 가능하다")
    void createWithAllFields() {
        // given
        Recipe recipe = Recipe.builder()
                .name("김치찌개")
                .build();

        // when
        RecipeIngredient ingredient = new RecipeIngredient(
                recipe,
                "김치",
                "200g",
                "g",
                true
        );

        // then
        assertThat(ingredient.getRecipe()).isEqualTo(recipe);
        assertThat(ingredient.getName()).isEqualTo("김치");
        assertThat(ingredient.getAmount()).isEqualTo("200g");
        assertThat(ingredient.getUnit()).isEqualTo("g");
        assertThat(ingredient.getIsRequired()).isTrue();
    }
}
