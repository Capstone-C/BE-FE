package com.capstone.web.diary.controller;

import com.capstone.web.auth.jwt.JwtAuthenticationFilter.MemberPrincipal;
import com.capstone.web.diary.dto.DiaryDto;
import com.capstone.web.diary.service.DiaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@Tag(name = "Diary", description = "식단 다이어리 API")
@RestController
@RequestMapping("/api/v1/diary")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;

    @Operation(
        summary = "월별 캘린더 조회",
        description = "특정 연도와 월의 식단 다이어리 캘린더를 조회합니다.",
        security = @SecurityRequirement(name = "JWT")
    )
    @GetMapping
    public ResponseEntity<DiaryDto.MonthlyCalendarResponse> getMonthlyCalendar(
        @RequestParam int year,
        @RequestParam int month,
        Authentication authentication
    ) {
        Long memberId = extractMemberId(authentication);
        DiaryDto.MonthlyCalendarResponse response = diaryService.getMonthlyCalendar(memberId, year, month);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "날짜별 식단 조회",
        description = "특정 날짜의 식단 기록을 조회합니다.",
        security = @SecurityRequirement(name = "JWT")
    )
    @GetMapping("/{date}")
    public ResponseEntity<List<DiaryDto.Response>> getDiaryByDate(
        @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
        Authentication authentication
    ) {
        Long memberId = extractMemberId(authentication);
        List<DiaryDto.Response> response = diaryService.getDiaryByDate(memberId, date);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "식단 기록 생성",
        description = """
            새로운 식단 기록을 생성합니다.
            
            **DIARY-03**: 사용자가 직접 입력하여 식단 기록 생성
            **DIARY-06**: 레시피에서 가져와서 식단 기록 생성
            - 레시피의 제목 → content
            - 레시피의 이미지 → imageUrl
            - 레시피의 ID → recipeId
            
            같은 날짜, 같은 식사 타입에 중복 기록은 불가능합니다.
            """,
        security = @SecurityRequirement(name = "JWT")
    )
    @PostMapping
    public ResponseEntity<DiaryDto.Response> createDiary(
        @Valid @RequestBody DiaryDto.CreateRequest request,
        Authentication authentication
    ) {
        Long memberId = extractMemberId(authentication);
        DiaryDto.Response response = diaryService.createDiary(memberId, request);
        
        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(response.getId())
            .toUri();
        
        return ResponseEntity.created(location).body(response);
    }

    @Operation(
        summary = "식단 기록 수정",
        description = "기존 식단 기록을 수정합니다.",
        security = @SecurityRequirement(name = "JWT")
    )
    @PutMapping("/{id}")
    public ResponseEntity<DiaryDto.Response> updateDiary(
        @PathVariable Long id,
        @Valid @RequestBody DiaryDto.UpdateRequest request,
        Authentication authentication
    ) {
        Long memberId = extractMemberId(authentication);
        DiaryDto.Response response = diaryService.updateDiary(memberId, id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "식단 기록 삭제",
        description = "기존 식단 기록을 삭제합니다.",
        security = @SecurityRequirement(name = "JWT")
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDiary(
        @PathVariable Long id,
        Authentication authentication
    ) {
        Long memberId = extractMemberId(authentication);
        diaryService.deleteDiary(memberId, id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Authentication에서 회원 ID 추출
     */
    private Long extractMemberId(Authentication authentication) {
        MemberPrincipal principal = (MemberPrincipal) authentication.getPrincipal();
        return principal.id();
    }
}
