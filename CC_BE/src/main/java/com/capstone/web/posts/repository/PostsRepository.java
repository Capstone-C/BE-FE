package com.capstone.web.posts.repository;

import com.capstone.web.posts.domain.Posts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostsRepository extends JpaRepository<Posts, Long> {
    @Query("SELECT p FROM Posts p JOIN FETCH p.authorId")
    List<Posts> findAllWithAuthor();
}

