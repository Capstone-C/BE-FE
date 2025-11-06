package com.capstone.web.recipe.repository;

import com.capstone.web.recipe.domain.RecipeIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeIngredientRepository extends JpaRepository<RecipeIngredient, Long> {

    /**
     * 레시피 ID로 재료 조회
     */
    List<RecipeIngredient> findByRecipeId(Long recipeId);

    /**
     * 재료 이름으로 검색 (해당 재료가 포함된 레시피 찾기용)
     */
    @Query("SELECT ri FROM RecipeIngredient ri WHERE LOWER(ri.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<RecipeIngredient> findByNameContaining(@Param("name") String name);

    /**
     * 필수 재료만 조회
     */
    List<RecipeIngredient> findByRecipeIdAndIsRequiredTrue(Long recipeId);
}
