package com.capstone.web.refrigerator.controller;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.capstone.web.auth.dto.LoginRequest;
import com.capstone.web.member.domain.Member;
import com.capstone.web.member.repository.MemberRepository;
import com.capstone.web.refrigerator.domain.RefrigeratorItem;
import com.capstone.web.refrigerator.dto.RefrigeratorDto;
import com.capstone.web.refrigerator.repository.RefrigeratorItemRepository;
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
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RefrigeratorControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private RefrigeratorItemRepository refrigeratorItemRepository;
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
        refrigeratorItemRepository.deleteAll();
        memberRepository.deleteAll();

        testMember = memberRepository.save(Member.builder()
                .email("user@example.com")
                .password(passwordEncoder.encode("password123!"))
                .nickname("테스터")
                .build());
        userToken = loginAndGetToken(testMember, "password123!");
    }

    // ========== REF-01: 냉장고 식재료 목록 조회 API 테스트 ==========

    @DisplayName("식재료 목록 조회 API 호출 성공 - 빈 목록")
    @Test
    void getMyItems_ApiSuccess_EmptyList() throws Exception {
        // when
        ResultActions result = mockMvc.perform(get("/api/v1/refrigerator/items")
                .header("Authorization", "Bearer " + userToken));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)))
                .andExpect(jsonPath("$.totalCount", is(0)))
                .andExpect(jsonPath("$.expiringCount", is(0)))
                .andExpect(jsonPath("$.expiredCount", is(0)));
    }

    @DisplayName("식재료 목록 조회 API 호출 성공 - 데이터 있음")
    @Test
    void getMyItems_ApiSuccess_WithData() throws Exception {
        // given
        refrigeratorItemRepository.save(RefrigeratorItem.builder()
                .member(testMember)
                .name("우유")
                .quantity(1)
                .unit("L")
                .expirationDate(LocalDate.now().plusDays(2))
                .build());

        refrigeratorItemRepository.save(RefrigeratorItem.builder()
                .member(testMember)
                .name("계란")
                .quantity(10)
                .unit("개")
                .expirationDate(LocalDate.now().plusDays(10))
                .build());

        // when
        ResultActions result = mockMvc.perform(get("/api/v1/refrigerator/items")
                .header("Authorization", "Bearer " + userToken));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.totalCount", is(2)))
                .andExpect(jsonPath("$.expiringCount", is(1)))
                .andExpect(jsonPath("$.items[0].name", is("우유")))
                .andExpect(jsonPath("$.items[1].name", is("계란")));
    }

    @DisplayName("식재료 목록 조회 API 호출 성공 - 이름순 정렬")
    @Test
    void getMyItems_ApiSuccess_SortByName() throws Exception {
        // given
        refrigeratorItemRepository.save(RefrigeratorItem.builder()
                .member(testMember)
                .name("토마토")
                .build());

        refrigeratorItemRepository.save(RefrigeratorItem.builder()
                .member(testMember)
                .name("계란")
                .build());

        // when
        ResultActions result = mockMvc.perform(get("/api/v1/refrigerator/items")
                .param("sortBy", "name")
                .header("Authorization", "Bearer " + userToken));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.items[0].name", is("계란")))
                .andExpect(jsonPath("$.items[1].name", is("토마토")));
    }

    // ========== REF-02: 식재료 추가 API 테스트 ==========

    @DisplayName("식재료 추가 API 호출 성공")
    @Test
    void addItem_ApiSuccess() throws Exception {
        // given
        RefrigeratorDto.CreateRequest request = RefrigeratorDto.CreateRequest.builder()
                .name("우유")
                .quantity(1)
                .unit("L")
                .expirationDate(LocalDate.now().plusDays(7))
                .memo("서울우유")
                .build();

        String requestBody = objectMapper.writeValueAsString(request);

        // when
        ResultActions result = mockMvc.perform(post("/api/v1/refrigerator/items")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        result.andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.name", is("우유")))
                .andExpect(jsonPath("$.quantity", is(1)))
                .andExpect(jsonPath("$.unit", is("L")))
                .andExpect(jsonPath("$.memo", is("서울우유")))
                .andExpect(jsonPath("$.daysUntilExpiration", is(7)))
                .andExpect(jsonPath("$.expirationSoon", is(false)))
                .andExpect(jsonPath("$.expired", is(false)));
    }

    @DisplayName("식재료 추가 API - 동일 이름+동일 소비기한 시 수량 합산")
    @Test
    void addItem_ApiSuccess_MergeQuantity() throws Exception {
        // given
        LocalDate expDate = LocalDate.now().plusDays(7);
        
        refrigeratorItemRepository.save(RefrigeratorItem.builder()
                .member(testMember)
                .name("우유")
                .quantity(2)
                .expirationDate(expDate)
                .build());

        RefrigeratorDto.CreateRequest request = RefrigeratorDto.CreateRequest.builder()
                .name("우유")
                .quantity(3)
                .expirationDate(expDate)
                .build();

        String requestBody = objectMapper.writeValueAsString(request);

        // when
        ResultActions result = mockMvc.perform(post("/api/v1/refrigerator/items")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        // 컨트롤러는 중복 병합도 201 Created를 반환합니다
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("우유")))
                .andExpect(jsonPath("$.quantity", is(5))); // 2 + 3
    }

    @DisplayName("식재료 추가 API 호출 실패 - Validation 오류 (이름 없음)")
    @Test
    void addItem_ApiFail_Validation() throws Exception {
        // given
        RefrigeratorDto.CreateRequest request = RefrigeratorDto.CreateRequest.builder()
                .name(null)  // 필수값 누락
                .quantity(1)
                .build();

        String requestBody = objectMapper.writeValueAsString(request);

        // when
        ResultActions result = mockMvc.perform(post("/api/v1/refrigerator/items")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        result.andExpect(status().isBadRequest());
    }

    // ========== REF-03,04: 식재료 일괄 추가 API 테스트 ==========

    @DisplayName("식재료 일괄 추가 API 호출 성공 - 모두 성공")
    @Test
    void addItemsBulk_ApiSuccess_AllSuccess() throws Exception {
        // given
        List<RefrigeratorDto.CreateRequest> items = List.of(
                RefrigeratorDto.CreateRequest.builder()
                        .name("우유")
                        .quantity(1)
                        .unit("L")
                        .build(),
                RefrigeratorDto.CreateRequest.builder()
                        .name("계란")
                        .quantity(10)
                        .unit("개")
                        .build()
        );

        RefrigeratorDto.BulkCreateRequest request = new RefrigeratorDto.BulkCreateRequest(items);
        String requestBody = objectMapper.writeValueAsString(request);

        // when
        ResultActions result = mockMvc.perform(post("/api/v1/refrigerator/items/bulk")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.successCount", is(2)))
                .andExpect(jsonPath("$.failCount", is(0)))
                .andExpect(jsonPath("$.addedItems", hasSize(2)))
                .andExpect(jsonPath("$.failedItems", hasSize(0)));
    }

    @DisplayName("식재료 일괄 추가 API 호출 성공 - 부분 성공")
    @Test
    void addItemsBulk_ApiSuccess_PartialSuccess() throws Exception {
        // given
        refrigeratorItemRepository.save(RefrigeratorItem.builder()
                .member(testMember)
                .name("우유")
                .build());

        List<RefrigeratorDto.CreateRequest> items = List.of(
                RefrigeratorDto.CreateRequest.builder()
                        .name("우유")  // 중복 -> 수량 합산으로 성공 처리
                        .quantity(1)
                        .build(),
                RefrigeratorDto.CreateRequest.builder()
                        .name("계란")  // 성공
                        .quantity(10)
                        .build()
        );

        RefrigeratorDto.BulkCreateRequest request = new RefrigeratorDto.BulkCreateRequest(items);
        String requestBody = objectMapper.writeValueAsString(request);

        // when
        ResultActions result = mockMvc.perform(post("/api/v1/refrigerator/items/bulk")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        // 중복도 수량 합산으로 처리되므로 모두 성공
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.successCount", is(2)))
                .andExpect(jsonPath("$.failCount", is(0)))
                .andExpect(jsonPath("$.addedItems", hasSize(2)))
                .andExpect(jsonPath("$.failedItems", hasSize(0)));
    }

    // ========== REF-05: 식재료 수정 API 테스트 ==========

    @DisplayName("식재료 수정 API 호출 성공")
    @Test
    void updateItem_ApiSuccess() throws Exception {
        // given
        RefrigeratorItem saved = refrigeratorItemRepository.save(RefrigeratorItem.builder()
                .member(testMember)
                .name("우유")
                .quantity(1)
                .unit("L")
                .expirationDate(LocalDate.now().plusDays(7))
                .build());

        RefrigeratorDto.UpdateRequest request = RefrigeratorDto.UpdateRequest.builder()
                .quantity(2)
                .unit("L")
                .expirationDate(LocalDate.now().plusDays(10))
                .memo("추가 구매")
                .build();

        String requestBody = objectMapper.writeValueAsString(request);

        // when
        ResultActions result = mockMvc.perform(put("/api/v1/refrigerator/items/{id}", saved.getId())
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("우유")))  // 이름은 불변
                .andExpect(jsonPath("$.quantity", is(2)))
                .andExpect(jsonPath("$.unit", is("L")))
                .andExpect(jsonPath("$.memo", is("추가 구매")))
                .andExpect(jsonPath("$.daysUntilExpiration", is(10)));
    }

    @DisplayName("식재료 수정 API 호출 실패 - 존재하지 않는 ID")
    @Test
    void updateItem_ApiFail_NotFound() throws Exception {
        // given
        Long nonExistentId = 999L;

        RefrigeratorDto.UpdateRequest request = RefrigeratorDto.UpdateRequest.builder()
                .quantity(2)
                .build();

        String requestBody = objectMapper.writeValueAsString(request);

        // when
        ResultActions result = mockMvc.perform(put("/api/v1/refrigerator/items/{id}", nonExistentId)
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        result.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("찾을 수 없습니다")));
    }

    @DisplayName("식재료 수정 API 호출 실패 - 권한 없음")
    @Test
    void updateItem_ApiFail_Unauthorized() throws Exception {
        // given
        Member otherMember = memberRepository.save(Member.builder()
                .email("other@example.com")
                .password(passwordEncoder.encode("password123!"))
                .nickname("타인")
                .build());

        RefrigeratorItem saved = refrigeratorItemRepository.save(RefrigeratorItem.builder()
                .member(otherMember)
                .name("우유")
                .build());

        RefrigeratorDto.UpdateRequest request = RefrigeratorDto.UpdateRequest.builder()
                .quantity(2)
                .build();

        String requestBody = objectMapper.writeValueAsString(request);

        // when
        ResultActions result = mockMvc.perform(put("/api/v1/refrigerator/items/{id}", saved.getId())
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        result.andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("권한이 없습니다")));
    }

    // ========== REF-06: 식재료 삭제 API 테스트 ==========

    @DisplayName("식재료 삭제 API 호출 성공")
    @Test
    void deleteItem_ApiSuccess() throws Exception {
        // given
        RefrigeratorItem saved = refrigeratorItemRepository.save(RefrigeratorItem.builder()
                .member(testMember)
                .name("우유")
                .build());

        // when
        ResultActions result = mockMvc.perform(delete("/api/v1/refrigerator/items/{id}", saved.getId())
                .header("Authorization", "Bearer " + userToken));

        // then
        result.andExpect(status().isNoContent());
    }

    @DisplayName("식재료 삭제 API 호출 실패 - 존재하지 않는 ID")
    @Test
    void deleteItem_ApiFail_NotFound() throws Exception {
        // given
        Long nonExistentId = 999L;

        // when
        ResultActions result = mockMvc.perform(delete("/api/v1/refrigerator/items/{id}", nonExistentId)
                .header("Authorization", "Bearer " + userToken));

        // then
        result.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("찾을 수 없습니다")));
    }

    @DisplayName("식재료 삭제 API 호출 실패 - 권한 없음")
    @Test
    void deleteItem_ApiFail_Unauthorized() throws Exception {
        // given
        Member otherMember = memberRepository.save(Member.builder()
                .email("other@example.com")
                .password(passwordEncoder.encode("password123!"))
                .nickname("타인")
                .build());

        RefrigeratorItem saved = refrigeratorItemRepository.save(RefrigeratorItem.builder()
                .member(otherMember)
                .name("우유")
                .build());

        // when
        ResultActions result = mockMvc.perform(delete("/api/v1/refrigerator/items/{id}", saved.getId())
                .header("Authorization", "Bearer " + userToken));

        // then
        result.andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message", containsString("권한이 없습니다")));
    }

    @DisplayName("인증 없이 API 호출 시 403 Forbidden을 응답한다")
    @Test
    void allApis_Fail_Unauthorized() throws Exception {
        // when & then
        // 인증 정보가 없으므로 보호된 리소스 접근이 금지됨 (403)
        mockMvc.perform(get("/api/v1/refrigerator/items"))
                .andExpect(status().isForbidden());
    }
}
