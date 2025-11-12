package com.capstone.web.posts.repository;

import com.capstone.web.posts.domain.Posts;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional; // (추가)

@Repository
public interface PostsRepository extends JpaRepository<Posts, Long>, JpaSpecificationExecutor<Posts> {

    // --- N+1 문제 해결용 findAll (list) ---
    @Override
    @EntityGraph(value = "Posts.withIngredients")
    Page<Posts> findAll(@Nullable Specification<Posts> spec, Pageable pageable);

    // --- (추가) N+1 문제 해결용 findById (getPostById, compare...) ---
    @Override
    @EntityGraph(value = "Posts.withIngredients")
    Optional<Posts> findById(Long id);

    // --- (기존 메서드들) ---
    @Query("SELECT p FROM Posts p JOIN FETCH p.authorId")
    List<Posts> findAllWithAuthor();

    long countByCategory_Id(Long categoryId);

    long countByCategory_IdAndCreatedAtBetween(Long categoryId, LocalDateTime start, LocalDateTime end);

    Posts findTop1ByCategory_IdOrderByCreatedAtDesc(Long categoryId);
}