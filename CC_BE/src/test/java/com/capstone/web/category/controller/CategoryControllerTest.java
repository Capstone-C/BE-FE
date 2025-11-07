package com.capstone.web.category.controller;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.capstone.web.auth.dto.LoginRequest;
import com.capstone.web.category.domain.Category;
import com.capstone.web.category.dto.CategoryRequest;
import com.capstone.web.category.repository.CategoryRepository;
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CategoryControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private String userToken;

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
        categoryRepository.deleteAll();
        memberRepository.deleteAll();

        Member user = memberRepository.save(Member.builder()
                .email("user@example.com")
                .password(passwordEncoder.encode("password123!"))
                .nickname("테스터")
                .build());
        userToken = loginAndGetToken(user, "password123!");
    }

    @DisplayName("새 카테고리 생성 API 호출에 성공한다")
    @Test
    void createCategory_ApiSuccess() throws Exception {
        // given
        CategoryRequest request = new CategoryRequest("API 테스트", Category.CategoryType.FREE, null);
        String requestBody = objectMapper.writeValueAsString(request);

        // when
        ResultActions result = mockMvc.perform(post("/api/v1/categories")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        result.andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith("/api/v1/categories")));
    }

    @DisplayName("ID로 카테고리 조회 API 호출에 성공한다")
    @Test
    void getCategory_ApiSuccess() throws Exception {
        // given
        Category saved = categoryRepository.save(Category.builder().name("조회용").type(Category.CategoryType.QA).build());

        // when
        // [수정] "categories{id}"가 올바른 경로입니다. ("categories1" (X))
        ResultActions result = mockMvc.perform(get("/api/v1/categories/{id}", saved.getId())
                .header("Authorization", "Bearer " + userToken));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(saved.getId().intValue())))
                .andExpect(jsonPath("$.name", is("조회용")));
    }

    @DisplayName("카테고리 삭제 API 호출에 성공한다")
    @Test
    void deleteCategory_ApiSuccess() throws Exception {
        // given
        Category saved = categoryRepository.save(Category.builder().name("삭제용").type(Category.CategoryType.FREE).build());

        // when
        // [수정] "categories{id}"가 올바른 경로입니다. ("categories2" (X))
        ResultActions result = mockMvc.perform(delete("/api/v1/categories/{id}", saved.getId())
                .header("Authorization", "Bearer " + userToken));

        // then
        result.andExpect(status().isNoContent());
    }
}