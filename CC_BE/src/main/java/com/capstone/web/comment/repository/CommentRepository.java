package com.capstone.web.comment.repository;

import com.capstone.web.comment.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 특정 게시글의 모든 댓글을 부모 댓글 우선, 그리고 생성 시간 순으로 정렬하여 조회
    @Query("SELECT c FROM Comment c WHERE c.post.id = :postId ORDER BY c.parent.id ASC NULLS FIRST, c.createdAt ASC")
    List<Comment> findAllByPostId(@Param("postId") Long postId);

    @Query("SELECT c FROM Comment c JOIN FETCH c.post WHERE c.author.id = :authorId ORDER BY c.createdAt DESC")
    List<Comment> findAllByAuthorId(@Param("authorId") Long authorId);
}