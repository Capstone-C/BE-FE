package com.capstone.web.recipe.repository;

import com.capstone.web.recipe.domain.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    /**
     * 레시피 이름으로 검색
     */
    List<Recipe> findByNameContainingIgnoreCase(String name);

    /**
     * 난이도로 필터링
     */
    List<Recipe> findByDifficulty(Recipe.Difficulty difficulty);

    /**
     * 조리 시간 이하로 필터링
     */
    List<Recipe> findByCookTimeLessThanEqual(Integer cookTime);

    /**
     * 레시피와 재료를 함께 조회 (N+1 문제 해결)
     */
    @Query("SELECT DISTINCT r FROM Recipe r LEFT JOIN FETCH r.ingredients")
    List<Recipe> findAllWithIngredients();

    /**
     * 특정 레시피와 재료를 함께 조회
     */
    @Query("SELECT r FROM Recipe r LEFT JOIN FETCH r.ingredients WHERE r.id = :id")
    Recipe findByIdWithIngredients(@Param("id") Long id);
}
