package com.capstone.web.refrigerator.repository;

import com.capstone.web.member.domain.Member;
import com.capstone.web.refrigerator.domain.RefrigeratorItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

/**
 * 냉장고 식재료 레포지토리
 * REF-01: 내 냉장고 식재료 목록 조회
 */
@Repository
public interface RefrigeratorItemRepository extends JpaRepository<RefrigeratorItem, Long> {

    /**
     * 특정 회원의 모든 식재료 조회 (소비기한 임박순 정렬)
     * 소비기한이 없는 항목은 하단에 위치
     */
    @Query("SELECT r FROM RefrigeratorItem r WHERE r.member = :member " +
            "ORDER BY CASE WHEN r.expirationDate IS NULL THEN 1 ELSE 0 END, " +
            "r.expirationDate ASC, r.createdAt DESC")
    List<RefrigeratorItem> findByMemberOrderByExpirationDateAsc(@Param("member") Member member);

    /**
     * 특정 회원의 특정 이름 식재료 존재 여부 확인 (중복 체크)
     */
    boolean existsByMemberAndName(Member member, String name);

    /**
     * 특정 회원의 특정 이름 식재료 조회
     */
    Optional<RefrigeratorItem> findByMemberAndName(Member member, String name);

    /**
     * 특정 회원의 모든 식재료 조회 (이름순 정렬)
     */
    List<RefrigeratorItem> findByMemberOrderByNameAsc(Member member);

    /**
     * 특정 회원의 모든 식재료 조회 (등록일순 정렬)
     */
    List<RefrigeratorItem> findByMemberOrderByCreatedAtDesc(Member member);

    /**
     * REF-07: 회원 ID로 모든 식재료 조회
     */
    List<RefrigeratorItem> findByMemberId(Long memberId);

    /**
     * 특정 회원의 특정 이름과 소비기한으로 식재료 조회
     */
    Optional<RefrigeratorItem> findByMemberAndNameAndExpirationDate(Member member, String name, LocalDate expirationDate);

    /**
     * 소비기한이 NULL인 특정 회원의 특정 이름 식재료 조회
     */
    Optional<RefrigeratorItem> findByMemberAndNameAndExpirationDateIsNull(Member member, String name);
}
