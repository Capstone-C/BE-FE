package com.capstone.web.category.config;

import com.capstone.web.category.domain.Category;
import com.capstone.web.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CategoryDataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    @Override
    public void run(String... args) {
        if (categoryRepository.count() > 0) {
            return; // already seeded
        }
        // Seed minimal default categories used by community/boards
        Category vegan = Category.builder()
                .name("비건")
                .type(Category.CategoryType.VEGAN)
                .parent(null)
                .build();
        Category recipe = Category.builder()
                .name("레시피")
                .type(Category.CategoryType.RECIPE)
                .parent(null)
                .build();
        Category free = Category.builder()
                .name("자유")
                .type(Category.CategoryType.FREE)
                .parent(null)
                .build();
        Category qa = Category.builder()
                .name("Q&A")
                .type(Category.CategoryType.QA)
                .parent(null)
                .build();

        categoryRepository.saveAll(List.of(vegan, recipe, free, qa));
    }
}

