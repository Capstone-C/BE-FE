package com.capstone.web.comment.repository;

import com.capstone.web.comment.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // [수정] author와 parent 정보도 함께 JOIN FETCH로 가져옵니다.
    @Query("SELECT c FROM Comment c " +
            "JOIN FETCH c.author " +
            "LEFT JOIN FETCH c.parent " + // 부모가 없는 댓글도 있어야 하므로 LEFT JOIN
            "WHERE c.post.id = :postId " +
            "ORDER BY c.parent.id ASC NULLS FIRST, c.createdAt ASC")
    List<Comment> findAllByPostId(@Param("postId") Long postId);

    // [수정] post 정보와 함께 author 정보도 JOIN FETCH로 가져옵니다.
    @Query("SELECT c FROM Comment c " +
            "JOIN FETCH c.post " +
            "JOIN FETCH c.author " +
            "WHERE c.author.id = :authorId " +
            "ORDER BY c.createdAt DESC")
    List<Comment> findAllByAuthorId(@Param("authorId") Long authorId);
}