package com.capstone.web.recipe.domain;

import jakarta.persistence.*;
import lombok.*;

/**
 * 레시피 재료 엔티티
 */
@Entity
@Table(name = "recipe_ingredient")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RecipeIngredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 50)
    private String amount; // 필요량 (예: "200g", "2개", "1컵")

    @Column(length = 10)
    private String unit; // 단위

    @Column(name = "is_required")
    @Builder.Default
    private Boolean isRequired = true; // 필수 재료 여부

    public RecipeIngredient(Recipe recipe, String name) {
        this.recipe = recipe;
        this.name = name;
        this.isRequired = true;
    }

    public RecipeIngredient(Recipe recipe, String name, String amount, String unit, Boolean isRequired) {
        this.recipe = recipe;
        this.name = name;
        this.amount = amount;
        this.unit = unit;
        this.isRequired = isRequired;
    }

    public RecipeIngredient(String name, String amount, String unit) {
        this.name = name;
        this.amount = amount;
        this.unit = unit;
        this.isRequired = true;
    }

    public RecipeIngredient(String name, String amount, String unit, Boolean isRequired) {
        this.name = name;
        this.amount = amount;
        this.unit = unit;
        this.isRequired = isRequired;
    }
}
