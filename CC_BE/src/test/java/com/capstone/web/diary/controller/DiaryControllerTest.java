package com.capstone.web.diary.controller;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.capstone.web.auth.dto.LoginRequest;
import com.capstone.web.diary.domain.Diary;
import com.capstone.web.diary.dto.DiaryDto;
import com.capstone.web.diary.repository.DiaryRepository;
import com.capstone.web.member.domain.Member;
import com.capstone.web.member.repository.MemberRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class DiaryControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private DiaryRepository diaryRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private String userToken;
    private Member testMember;

    private String loginAndGetToken(Member member, String rawPassword) throws Exception {
        LoginRequest req = new LoginRequest(member.getEmail(), rawPassword);
        String body = objectMapper.writeValueAsString(req);
        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("accessToken").asText();
    }

    @BeforeEach
    void setup() throws Exception {
        diaryRepository.deleteAll();
        memberRepository.deleteAll();

        testMember = memberRepository.save(Member.builder()
                .email("user@example.com")
                .password(passwordEncoder.encode("password123!"))
                .nickname("테스터")
                .build());
        userToken = loginAndGetToken(testMember, "password123!");
    }

    // ========== DIARY-01: 월별 캘린더 조회 API 테스트 ==========

    @DisplayName("월별 캘린더 조회 API 호출 성공 - 빈 월")
    @Test
    void getMonthlyCalendar_ApiSuccess_EmptyMonth() throws Exception {
        // when
        ResultActions result = mockMvc.perform(get("/api/v1/diary")
                .param("year", "2025")
                .param("month", "1")
                .header("Authorization", "Bearer " + userToken));

        // then
        result.andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.year", is(2025)))
                .andExpect(jsonPath("$.month", is(1)))
                .andExpect(jsonPath("$.dailyEntries", hasSize(0)));
    }

    @DisplayName("월별 캘린더 조회 API 호출 성공 - 여러 날짜 데이터 존재")
    @Test
    void getMonthlyCalendar_ApiSuccess_WithData() throws Exception {
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
                .date(date2)
                .mealType(Diary.MealType.LUNCH)
                .content("점심")
                .build());

        // when
        ResultActions result = mockMvc.perform(get("/api/v1/diary")
                .param("year", "2025")
                .param("month", "1")
                .header("Authorization", "Bearer " + userToken));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.year", is(2025)))
                .andExpect(jsonPath("$.month", is(1)))
                .andExpect(jsonPath("$.dailyEntries", hasSize(2)))
                .andExpect(jsonPath("$.dailyEntries[0].date", is("2025-01-15")))
                .andExpect(jsonPath("$.dailyEntries[0].hasBreakfast", is(true)))
                .andExpect(jsonPath("$.dailyEntries[0].hasLunch", is(false)))
                .andExpect(jsonPath("$.dailyEntries[0].thumbnailUrl", is("image1.jpg")))
                .andExpect(jsonPath("$.dailyEntries[1].date", is("2025-01-16")))
                .andExpect(jsonPath("$.dailyEntries[1].hasLunch", is(true)));
    }

    // ========== DIARY-02: 날짜별 식단 조회 API 테스트 ==========

    @DisplayName("날짜별 식단 조회 API 호출 성공 - 기록 없음")
    @Test
    void getDiaryByDate_ApiSuccess_NoData() throws Exception {
        // when
        ResultActions result = mockMvc.perform(get("/api/v1/diary/daily")
                .param("date", "2025-01-15")
                .header("Authorization", "Bearer " + userToken));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @DisplayName("날짜별 식단 조회 API 호출 성공 - 여러 식사 기록")
    @Test
    void getDiaryByDate_ApiSuccess_WithData() throws Exception {
        // given
        LocalDate date = LocalDate.of(2025, 1, 15);

        diaryRepository.save(Diary.builder()
                .member(testMember)
                .date(date)
                .mealType(Diary.MealType.BREAKFAST)
                .content("아침 메뉴")
                .build());

        diaryRepository.save(Diary.builder()
                .member(testMember)
                .date(date)
                .mealType(Diary.MealType.LUNCH)
                .content("점심 메뉴")
                .build());

        // when
        ResultActions result = mockMvc.perform(get("/api/v1/diary/daily")
                .param("date", "2025-01-15")
                .header("Authorization", "Bearer " + userToken));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].mealType", is("BREAKFAST")))
                .andExpect(jsonPath("$[0].content", is("아침 메뉴")))
                .andExpect(jsonPath("$[1].mealType", is("LUNCH")))
                .andExpect(jsonPath("$[1].content", is("점심 메뉴")));
    }

    // ========== DIARY-03: 식단 기록 생성 API 테스트 ==========

    @DisplayName("식단 기록 생성 API 호출 성공")
    @Test
    void createDiary_ApiSuccess() throws Exception {
        // given
        DiaryDto.CreateRequest request = DiaryDto.CreateRequest.builder()
                .date(LocalDate.of(2025, 1, 15))
                .mealType(Diary.MealType.BREAKFAST)
                .content("아침 식사 내용")
                .imageUrl("image.jpg")
                .recipeId(123L)
                .build();

        String requestBody = objectMapper.writeValueAsString(request);

        // when
        ResultActions result = mockMvc.perform(post("/api/v1/diary")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        result.andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.date", is("2025-01-15")))
                .andExpect(jsonPath("$.mealType", is("BREAKFAST")))
                .andExpect(jsonPath("$.content", is("아침 식사 내용")))
                .andExpect(jsonPath("$.imageUrl", is("image.jpg")))
                .andExpect(jsonPath("$.recipeId", is(123)));
    }

    @DisplayName("식단 기록 생성 API 호출 실패 - 중복 기록")
    @Test
    void createDiary_ApiFail_Duplicate() throws Exception {
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
                .content("새 기록")
                .build();

        String requestBody = objectMapper.writeValueAsString(request);

        // when
        ResultActions result = mockMvc.perform(post("/api/v1/diary")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        result.andExpect(status().isConflict())
                .andExpect(jsonPath("$.code", is("DUPLICATE_DIARY_ENTRY")));
    }

    @DisplayName("식단 기록 생성 API 호출 실패 - Validation 오류 (날짜 없음)")
    @Test
    void createDiary_ApiFail_Validation() throws Exception {
        // given
        DiaryDto.CreateRequest request = DiaryDto.CreateRequest.builder()
                .date(null)
                .mealType(Diary.MealType.BREAKFAST)
                .content("내용")
                .build();

        String requestBody = objectMapper.writeValueAsString(request);

        // when
        ResultActions result = mockMvc.perform(post("/api/v1/diary")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        result.andExpect(status().isBadRequest());
    }

    // ========== DIARY-04: 식단 기록 수정 API 테스트 ==========

    @DisplayName("식단 기록 수정 API 호출 성공")
    @Test
    void updateDiary_ApiSuccess() throws Exception {
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

        String requestBody = objectMapper.writeValueAsString(request);

        // when
        ResultActions result = mockMvc.perform(put("/api/v1/diary/{id}", saved.getId())
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(saved.getId().intValue())))
                .andExpect(jsonPath("$.mealType", is("LUNCH")))
                .andExpect(jsonPath("$.content", is("수정된 내용")))
                .andExpect(jsonPath("$.imageUrl", is("new.jpg")))
                .andExpect(jsonPath("$.recipeId", is(456)));
    }

    @DisplayName("식단 기록 수정 API 호출 실패 - 존재하지 않는 ID")
    @Test
    void updateDiary_ApiFail_NotFound() throws Exception {
        // given
        Long nonExistentId = 999L;
        DiaryDto.UpdateRequest request = DiaryDto.UpdateRequest.builder()
                .mealType(Diary.MealType.LUNCH)
                .content("수정 내용")
                .build();

        String requestBody = objectMapper.writeValueAsString(request);

        // when
        ResultActions result = mockMvc.perform(put("/api/v1/diary/{id}", nonExistentId)
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        result.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is("DIARY_NOT_FOUND")));
    }

    @DisplayName("식단 기록 수정 API 호출 실패 - 권한 없음")
    @Test
    void updateDiary_ApiFail_Unauthorized() throws Exception {
        // given
        Member otherMember = memberRepository.save(Member.builder()
                .email("other@test.com")
                .password(passwordEncoder.encode("password"))
                .nickname("타인")
                .build());

        Diary saved = diaryRepository.save(Diary.builder()
                .member(otherMember)
                .date(LocalDate.of(2025, 1, 15))
                .mealType(Diary.MealType.BREAKFAST)
                .content("타인 기록")
                .build());

        DiaryDto.UpdateRequest request = DiaryDto.UpdateRequest.builder()
                .mealType(Diary.MealType.LUNCH)
                .content("수정 시도")
                .build();

        String requestBody = objectMapper.writeValueAsString(request);

        // when
        ResultActions result = mockMvc.perform(put("/api/v1/diary/{id}", saved.getId())
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        result.andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code", is("UNAUTHORIZED_DIARY_ACCESS")));
    }

    // ========== DIARY-05: 식단 기록 삭제 API 테스트 ==========

    @DisplayName("식단 기록 삭제 API 호출 성공")
    @Test
    void deleteDiary_ApiSuccess() throws Exception {
        // given
        Diary saved = diaryRepository.save(Diary.builder()
                .member(testMember)
                .date(LocalDate.of(2025, 1, 15))
                .mealType(Diary.MealType.BREAKFAST)
                .content("삭제할 기록")
                .build());

        // when
        ResultActions result = mockMvc.perform(delete("/api/v1/diary/{id}", saved.getId())
                .header("Authorization", "Bearer " + userToken));

        // then
        result.andExpect(status().isNoContent());
    }

    @DisplayName("식단 기록 삭제 API 호출 실패 - 존재하지 않는 ID")
    @Test
    void deleteDiary_ApiFail_NotFound() throws Exception {
        // given
        Long nonExistentId = 999L;

        // when
        ResultActions result = mockMvc.perform(delete("/api/v1/diary/{id}", nonExistentId)
                .header("Authorization", "Bearer " + userToken));

        // then
        result.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is("DIARY_NOT_FOUND")));
    }

    @DisplayName("식단 기록 삭제 API 호출 실패 - 권한 없음")
    @Test
    void deleteDiary_ApiFail_Unauthorized() throws Exception {
        // given
        Member otherMember = memberRepository.save(Member.builder()
                .email("other@test.com")
                .password(passwordEncoder.encode("password"))
                .nickname("타인")
                .build());

        Diary saved = diaryRepository.save(Diary.builder()
                .member(otherMember)
                .date(LocalDate.of(2025, 1, 15))
                .mealType(Diary.MealType.BREAKFAST)
                .content("타인 기록")
                .build());

        // when
        ResultActions result = mockMvc.perform(delete("/api/v1/diary/{id}", saved.getId())
                .header("Authorization", "Bearer " + userToken));

        // then
        result.andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code", is("UNAUTHORIZED_DIARY_ACCESS")));
    }
}
