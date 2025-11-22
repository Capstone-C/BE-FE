package com.capstone.web.posts.repository;

import com.capstone.web.member.domain.Member;
import com.capstone.web.posts.domain.PostScrap;
import com.capstone.web.posts.domain.Posts;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostScrapRepository extends JpaRepository<PostScrap, Long> {

    // 중복 확인 및 삭제용
    Optional<PostScrap> findByMemberAndPost(Member member, Posts post);

    // REC-08: 내 스크랩 목록 조회 (N+1 방지 Fetch Join)
    @EntityGraph(attributePaths = {"post", "post.authorId", "post.media"})
    Page<PostScrap> findByMember(Member member, Pageable pageable);

    // 검색 기능 (제목+내용)
    @EntityGraph(attributePaths = {"post", "post.authorId", "post.media"})
    @Query("SELECT s FROM PostScrap s WHERE s.member = :member AND " +
            "(s.post.title LIKE %:keyword% OR s.post.content LIKE %:keyword%)")
    Page<PostScrap> findByMemberAndKeyword(@Param("member") Member member,
                                           @Param("keyword") String keyword,
                                           Pageable pageable);
}