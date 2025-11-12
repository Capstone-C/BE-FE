package com.capstone.web.posts.repository;

import com.capstone.web.posts.domain.PostIngredient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostIngredientRepository extends JpaRepository<PostIngredient, Long> {
}