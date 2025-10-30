package com.capstone.web.diary.service;

import com.capstone.web.diary.domain.Diary;
import com.capstone.web.diary.domain.Diary.MealType;
import com.capstone.web.diary.dto.DiaryDto;
import com.capstone.web.diary.exception.DiaryNotFoundException;
import com.capstone.web.diary.exception.DuplicateDiaryEntryException;
import com.capstone.web.diary.exception.UnauthorizedDiaryAccessException;
import com.capstone.web.diary.repository.DiaryRepository;
import com.capstone.web.member.domain.Member;
import com.capstone.web.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final MemberRepository memberRepository;

    /**
     * 월별 캘린더 조회 (DIARY-01)
     */
    public DiaryDto.MonthlyCalendarResponse getMonthlyCalendar(Long memberId, int year, int month) {
        Member member = getMemberById(memberId);
        List<Diary> diaries = diaryRepository.findByMemberAndYearMonth(member, year, month);
        return DiaryDto.MonthlyCalendarResponse.builder()
            .year(year)
            .month(month)
            .diaryList(diaries)
            .build();
    }

    /**
     * 특정 날짜의 식단 기록 조회 (DIARY-02)
     */
    public List<DiaryDto.Response> getDiaryByDate(Long memberId, LocalDate date) {
        Member member = getMemberById(memberId);
        List<Diary> diaries = diaryRepository.findByMemberAndDateOrderByMealTypeAsc(member, date);
        return diaries.stream()
            .map(DiaryDto.Response::new)
            .collect(Collectors.toList());
    }

    /**
     * 식단 기록 생성 (DIARY-03)
     */
    @Transactional
    public DiaryDto.Response createDiary(Long memberId, DiaryDto.CreateRequest request) {
        Member member = getMemberById(memberId);

        // 중복 체크: 같은 날짜, 같은 식사 타입의 기록이 있는지 확인
        if (diaryRepository.existsByMemberAndDateAndMealType(member, request.getDate(), request.getMealType())) {
            throw new DuplicateDiaryEntryException();
        }

        Diary diary = Diary.builder()
            .member(member)
            .date(request.getDate())
            .mealType(request.getMealType())
            .content(request.getContent())
            .imageUrl(request.getImageUrl())
            .recipeId(request.getRecipeId())
            .build();

        Diary savedDiary = diaryRepository.save(diary);
        return new DiaryDto.Response(savedDiary);
    }

    /**
     * 식단 기록 수정 (DIARY-04)
     */
    @Transactional
    public DiaryDto.Response updateDiary(Long memberId, Long diaryId, DiaryDto.UpdateRequest request) {
        Diary diary = getDiaryById(diaryId);

        // 작성자 권한 확인
        if (!diary.getMember().getId().equals(memberId)) {
            throw new UnauthorizedDiaryAccessException();
        }

        diary.update(
            request.getMealType(),
            request.getContent(),
            request.getImageUrl(),
            request.getRecipeId()
        );

        return new DiaryDto.Response(diary);
    }

    /**
     * 식단 기록 삭제 (DIARY-05)
     */
    @Transactional
    public void deleteDiary(Long memberId, Long diaryId) {
        Diary diary = getDiaryById(diaryId);

        // 작성자 권한 확인
        if (!diary.getMember().getId().equals(memberId)) {
            throw new UnauthorizedDiaryAccessException();
        }

        diaryRepository.delete(diary);
    }

    // === Helper Methods ===

    private Member getMemberById(Long memberId) {
        return memberRepository.findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
    }

    private Diary getDiaryById(Long diaryId) {
        return diaryRepository.findById(diaryId)
            .orElseThrow(DiaryNotFoundException::new);
    }
}
