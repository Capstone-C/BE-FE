package com.capstone.web.posts.repository;

import com.capstone.web.posts.domain.Posts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PostsRepository extends JpaRepository<Posts, Long>, JpaSpecificationExecutor<Posts> {
    @Query("SELECT p FROM Posts p JOIN FETCH p.authorId")
    List<Posts> findAllWithAuthor();

    long countByCategory_Id(Long categoryId);

    long countByCategory_IdAndCreatedAtBetween(Long categoryId, LocalDateTime start, LocalDateTime end);

    Posts findTop1ByCategory_IdOrderByCreatedAtDesc(Long categoryId);
}
