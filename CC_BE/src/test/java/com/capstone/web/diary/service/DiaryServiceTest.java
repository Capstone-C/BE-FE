package com.capstone.web.diary.service;

import static org.assertj.core.api.Assertions.*;

import com.capstone.web.diary.domain.Diary;
import com.capstone.web.diary.dto.DiaryDto;
import com.capstone.web.diary.exception.DiaryNotFoundException;
import com.capstone.web.diary.exception.DuplicateDiaryEntryException;
import com.capstone.web.diary.exception.UnauthorizedDiaryAccessException;
import com.capstone.web.diary.repository.DiaryRepository;
import com.capstone.web.member.domain.Member;
import com.capstone.web.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DiaryServiceTest {

    @Autowired private DiaryService diaryService;
    @Autowired private DiaryRepository diaryRepository;
    @Autowired private MemberRepository memberRepository;

    private Member testMember;
    private Member otherMember;

    @BeforeEach
    void setup() {
        diaryRepository.deleteAll();
        memberRepository.deleteAll();

        testMember = memberRepository.save(Member.builder()
                .email("test@test.com")
                .password("password")
                .nickname("테스터")
                .build());

        otherMember = memberRepository.save(Member.builder()
                .email("other@test.com")
                .password("password")
                .nickname("타인")
                .build());
    }

    // ========== DIARY-01: 월별 캘린더 조회 테스트 ==========

    @DisplayName("월별 캘린더 조회 - 빈 월")
    @Test
    void getMonthlyCalendar_EmptyMonth() {
        // given
        int year = 2025;
        int month = 1;

        // when
        DiaryDto.MonthlyCalendarResponse response = diaryService.getMonthlyCalendar(testMember.getId(), year, month);

        // then
        assertThat(response.getYear()).isEqualTo(year);
        assertThat(response.getMonth()).isEqualTo(month);
        assertThat(response.getDailyEntries()).isEmpty();
    }

    @DisplayName("월별 캘린더 조회 - 여러 날짜, 여러 식사 타입")
    @Test
    void getMonthlyCalendar_WithMultipleEntries() {
        // given
        LocalDate date1 = LocalDate.of(2025, 1, 15);
        LocalDate date2 = LocalDate.of(2025, 1, 16);

        diaryRepository.save(Diary.builder()
                .member(testMember)
                .date(date1)
                .mealType(Diary.MealType.BREAKFAST)
                .content("아침")
                .imageUrl("image1.jpg")
                .build());

        diaryRepository.save(Diary.builder()
                .member(testMember)
                .date(date1)
                .mealType(Diary.MealType.LUNCH)
                .content("점심")
                .build());

        diaryRepository.save(Diary.builder()
                .member(testMember)
                .date(date2)
                .mealType(Diary.MealType.DINNER)
                .content("저녁")
                .imageUrl("image2.jpg")
                .build());

        // when
        DiaryDto.MonthlyCalendarResponse response = diaryService.getMonthlyCalendar(testMember.getId(), 2025, 1);

        // then
        assertThat(response.getDailyEntries()).hasSize(2);

        DiaryDto.DailyEntry day1 = response.getDailyEntries().stream()
                .filter(e -> e.getDate().equals(date1))
                .findFirst()
                .orElseThrow();
        assertThat(day1.isHasBreakfast()).isTrue();
        assertThat(day1.isHasLunch()).isTrue();
        assertThat(day1.isHasDinner()).isFalse();
        assertThat(day1.isHasSnack()).isFalse();
        assertThat(day1.getThumbnailUrl()).isEqualTo("image1.jpg");

        DiaryDto.DailyEntry day2 = response.getDailyEntries().stream()
                .filter(e -> e.getDate().equals(date2))
                .findFirst()
                .orElseThrow();
        assertThat(day2.isHasDinner()).isTrue();
        assertThat(day2.getThumbnailUrl()).isEqualTo("image2.jpg");
    }

    @DisplayName("월별 캘린더 조회 - 다른 사용자 데이터는 조회되지 않음")
    @Test
    void getMonthlyCalendar_OnlyOwnEntries() {
        // given
        LocalDate date = LocalDate.of(2025, 1, 15);

        diaryRepository.save(Diary.builder()
                .member(testMember)
                .date(date)
                .mealType(Diary.MealType.BREAKFAST)
                .content("내 아침")
                .build());

        diaryRepository.save(Diary.builder()
                .member(otherMember)
                .date(date)
                .mealType(Diary.MealType.BREAKFAST)
                .content("타인 아침")
                .build());

        // when
        DiaryDto.MonthlyCalendarResponse response = diaryService.getMonthlyCalendar(testMember.getId(), 2025, 1);

        // then
        assertThat(response.getDailyEntries()).hasSize(1);
        assertThat(response.getDailyEntries().get(0).getDate()).isEqualTo(date);
    }

    // ========== DIARY-02: 날짜별 식단 조회 테스트 ==========

    @DisplayName("날짜별 식단 조회 - 해당 날짜 기록 없음")
    @Test
    void getDiaryByDate_NoEntries() {
        // given
        LocalDate date = LocalDate.of(2025, 1, 15);

        // when
        List<DiaryDto.Response> responses = diaryService.getDiaryByDate(testMember.getId(), date);

        // then
        assertThat(responses).isEmpty();
    }

    @DisplayName("날짜별 식단 조회 - 여러 식사 타입, 정렬 순서 확인")
    @Test
    void getDiaryByDate_MultipleMeals_Sorted() {
        // given
        LocalDate date = LocalDate.of(2025, 1, 15);

        diaryRepository.save(Diary.builder()
                .member(testMember)
                .date(date)
                .mealType(Diary.MealType.DINNER)
                .content("저녁")
                .build());

        diaryRepository.save(Diary.builder()
                .member(testMember)
                .date(date)
                .mealType(Diary.MealType.BREAKFAST)
                .content("아침")
                .build());

        diaryRepository.save(Diary.builder()
                .member(testMember)
                .date(date)
                .mealType(Diary.MealType.LUNCH)
                .content("점심")
                .build());

        // when
        List<DiaryDto.Response> responses = diaryService.getDiaryByDate(testMember.getId(), date);

        // then
        assertThat(responses).hasSize(3);
        assertThat(responses.get(0).getMealType()).isEqualTo(Diary.MealType.BREAKFAST);
        assertThat(responses.get(1).getMealType()).isEqualTo(Diary.MealType.LUNCH);
        assertThat(responses.get(2).getMealType()).isEqualTo(Diary.MealType.DINNER);
    }

    // ========== DIARY-03: 식단 기록 추가 테스트 ==========

    @DisplayName("식단 기록 추가 - 성공")
    @Test
    void createDiary_Success() {
        // given
        DiaryDto.CreateRequest request = DiaryDto.CreateRequest.builder()
                .date(LocalDate.of(2025, 1, 15))
                .mealType(Diary.MealType.BREAKFAST)
                .content("아침 식사")
                .imageUrl("image.jpg")
                .recipeId(123L)
                .build();

        // when
        DiaryDto.Response response = diaryService.createDiary(testMember.getId(), request);

        // then
        assertThat(response.getId()).isNotNull();
        assertThat(response.getDate()).isEqualTo(request.getDate());
        assertThat(response.getMealType()).isEqualTo(request.getMealType());
        assertThat(response.getContent()).isEqualTo(request.getContent());
        assertThat(response.getImageUrl()).isEqualTo(request.getImageUrl());
        assertThat(response.getRecipeId()).isEqualTo(request.getRecipeId());
    }

    @DisplayName("식단 기록 추가 - 중복 기록 시 예외")
    @Test
    void createDiary_Fail_Duplicate() {
        // given
        LocalDate date = LocalDate.of(2025, 1, 15);
        Diary.MealType mealType = Diary.MealType.BREAKFAST;

        diaryRepository.save(Diary.builder()
                .member(testMember)
                .date(date)
                .mealType(mealType)
                .content("기존 기록")
                .build());

        DiaryDto.CreateRequest request = DiaryDto.CreateRequest.builder()
                .date(date)
                .mealType(mealType)
                .content("새로운 기록")
                .build();

        // when & then
        assertThatThrownBy(() -> diaryService.createDiary(testMember.getId(), request))
                .isInstanceOf(DuplicateDiaryEntryException.class)
                .hasMessageContaining("해당 날짜")
                .hasMessageContaining("이미 기록이 존재합니다");
    }

    // ========== DIARY-04: 식단 기록 수정 테스트 ==========

    @DisplayName("식단 기록 수정 - 성공")
    @Test
    void updateDiary_Success() {
        // given
        Diary saved = diaryRepository.save(Diary.builder()
                .member(testMember)
                .date(LocalDate.of(2025, 1, 15))
                .mealType(Diary.MealType.BREAKFAST)
                .content("기존 내용")
                .build());

        DiaryDto.UpdateRequest request = DiaryDto.UpdateRequest.builder()
                .mealType(Diary.MealType.LUNCH)
                .content("수정된 내용")
                .imageUrl("new.jpg")
                .recipeId(456L)
                .build();

        // when
        DiaryDto.Response response = diaryService.updateDiary(testMember.getId(), saved.getId(), request);

        // then
        assertThat(response.getMealType()).isEqualTo(Diary.MealType.LUNCH);
        assertThat(response.getContent()).isEqualTo("수정된 내용");
        assertThat(response.getImageUrl()).isEqualTo("new.jpg");
        assertThat(response.getRecipeId()).isEqualTo(456L);
    }

    @DisplayName("식단 기록 수정 - 존재하지 않는 ID로 수정 시 예외")
    @Test
    void updateDiary_Fail_NotFound() {
        // given
        Long nonExistentId = 999L;
        DiaryDto.UpdateRequest request = DiaryDto.UpdateRequest.builder()
                .mealType(Diary.MealType.LUNCH)
                .content("수정 내용")
                .build();

        // when & then
        assertThatThrownBy(() -> diaryService.updateDiary(testMember.getId(), nonExistentId, request))
                .isInstanceOf(DiaryNotFoundException.class);
    }

    @DisplayName("식단 기록 수정 - 권한 없는 사용자가 수정 시 예외")
    @Test
    void updateDiary_Fail_Unauthorized() {
        // given
        Diary saved = diaryRepository.save(Diary.builder()
                .member(testMember)
                .date(LocalDate.of(2025, 1, 15))
                .mealType(Diary.MealType.BREAKFAST)
                .content("테스터 기록")
                .build());

        DiaryDto.UpdateRequest request = DiaryDto.UpdateRequest.builder()
                .mealType(Diary.MealType.LUNCH)
                .content("타인이 수정 시도")
                .build();

        // when & then
        assertThatThrownBy(() -> diaryService.updateDiary(otherMember.getId(), saved.getId(), request))
                .isInstanceOf(UnauthorizedDiaryAccessException.class)
                .hasMessageContaining("권한이 없습니다");
    }

    // ========== DIARY-05: 식단 기록 삭제 테스트 ==========

    @DisplayName("식단 기록 삭제 - 성공")
    @Test
    void deleteDiary_Success() {
        // given
        Diary saved = diaryRepository.save(Diary.builder()
                .member(testMember)
                .date(LocalDate.of(2025, 1, 15))
                .mealType(Diary.MealType.BREAKFAST)
                .content("삭제할 기록")
                .build());

        // when
        diaryService.deleteDiary(testMember.getId(), saved.getId());

        // then
        assertThat(diaryRepository.existsById(saved.getId())).isFalse();
    }

    @DisplayName("식단 기록 삭제 - 존재하지 않는 ID로 삭제 시 예외")
    @Test
    void deleteDiary_Fail_NotFound() {
        // given
        Long nonExistentId = 999L;

        // when & then
        assertThatThrownBy(() -> diaryService.deleteDiary(testMember.getId(), nonExistentId))
                .isInstanceOf(DiaryNotFoundException.class);
    }

    @DisplayName("식단 기록 삭제 - 권한 없는 사용자가 삭제 시 예외")
    @Test
    void deleteDiary_Fail_Unauthorized() {
        // given
        Diary saved = diaryRepository.save(Diary.builder()
                .member(testMember)
                .date(LocalDate.of(2025, 1, 15))
                .mealType(Diary.MealType.BREAKFAST)
                .content("테스터 기록")
                .build());

        // when & then
        assertThatThrownBy(() -> diaryService.deleteDiary(otherMember.getId(), saved.getId()))
                .isInstanceOf(UnauthorizedDiaryAccessException.class)
                .hasMessageContaining("권한이 없습니다");
    }
}
