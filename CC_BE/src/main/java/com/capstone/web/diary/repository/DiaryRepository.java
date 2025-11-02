package com.capstone.web.diary.repository;

import com.capstone.web.diary.domain.Diary;
import com.capstone.web.diary.domain.Diary.MealType;
import com.capstone.web.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, Long> {

    /**
     * 특정 회원의 특정 연월에 해당하는 모든 식단 기록 조회
     */
    @Query("SELECT d FROM Diary d WHERE d.member = :member " +
           "AND YEAR(d.date) = :year AND MONTH(d.date) = :month " +
           "ORDER BY d.date ASC, " +
           "CASE d.mealType " +
           "WHEN com.capstone.web.diary.domain.Diary$MealType.BREAKFAST THEN 0 " +
           "WHEN com.capstone.web.diary.domain.Diary$MealType.LUNCH THEN 1 " +
           "WHEN com.capstone.web.diary.domain.Diary$MealType.DINNER THEN 2 " +
           "WHEN com.capstone.web.diary.domain.Diary$MealType.SNACK THEN 3 " +
           "END ASC")
    List<Diary> findByMemberAndYearMonth(@Param("member") Member member,
                                          @Param("year") int year,
                                          @Param("month") int month);

    /**
     * 특정 회원의 특정 날짜에 해당하는 모든 식단 기록 조회
     */
    @Query("SELECT d FROM Diary d WHERE d.member = :member AND d.date = :date " +
           "ORDER BY " +
           "CASE d.mealType " +
           "WHEN com.capstone.web.diary.domain.Diary$MealType.BREAKFAST THEN 0 " +
           "WHEN com.capstone.web.diary.domain.Diary$MealType.LUNCH THEN 1 " +
           "WHEN com.capstone.web.diary.domain.Diary$MealType.DINNER THEN 2 " +
           "WHEN com.capstone.web.diary.domain.Diary$MealType.SNACK THEN 3 " +
           "END ASC")
    List<Diary> findByMemberAndDateOrderByMealTypeAsc(@Param("member") Member member,
                                                       @Param("date") LocalDate date);

    /**
     * 특정 회원의 특정 날짜, 특정 식사 타입에 기록이 있는지 확인
     */
    boolean existsByMemberAndDateAndMealType(Member member, LocalDate date, MealType mealType);

    /**
     * 특정 회원의 특정 날짜, 특정 식사 타입 기록 조회
     */
    Optional<Diary> findByMemberAndDateAndMealType(Member member, LocalDate date, MealType mealType);
}
