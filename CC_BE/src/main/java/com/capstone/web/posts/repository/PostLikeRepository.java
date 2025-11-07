package com.capstone.web.posts.repository;

import com.capstone.web.posts.domain.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    @Query("SELECT pl FROM PostLike pl WHERE pl.post.id = :postId AND pl.member.id = :memberId")
    Optional<PostLike> findByPostIdAndMemberId(@Param("postId") Long postId, @Param("memberId") Long memberId);

    long countByPost_Id(Long postId);

    void deleteByPost_Id(Long postId);
}

