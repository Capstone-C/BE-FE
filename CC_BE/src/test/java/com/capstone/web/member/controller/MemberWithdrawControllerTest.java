package com.capstone.web.member.controller;

import com.capstone.web.auth.dto.LoginRequest;
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
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MemberWithdrawControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private MemberRepository memberRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() { memberRepository.deleteAll(); }

    private String loginAndGetToken(String email, String rawPassword, String nickname) throws Exception {
        Member m = Member.builder()
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .nickname(nickname)
                .build();
        memberRepository.save(m);

        LoginRequest req = new LoginRequest(email, rawPassword);
        String body = objectMapper.writeValueAsString(req);
        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("accessToken").asText();
    }

    @DisplayName("탈퇴 성공 후 재요청은 idempotent 하게 200 반환")
    @Test
    void withdraw_idempotent() throws Exception {
        String token = loginAndGetToken("wd1@example.com", "Abcd1234!", "탈퇴닉");

        // 1차 탈퇴
        var first = mockMvc.perform(delete("/api/v1/members/me")
                        .header("Authorization", "Bearer " + token))
                .andReturn();
        System.out.println("FIRST WITHDRAW status=" + first.getResponse().getStatus() + " body=" + first.getResponse().getContentAsString());
        org.assertj.core.api.Assertions.assertThat(first.getResponse().getStatus()).isEqualTo(200);

        // 2차 탈퇴(이미 탈퇴) - 여전히 200
        var second = mockMvc.perform(delete("/api/v1/members/me")
                        .header("Authorization", "Bearer " + token))
                .andReturn();
        System.out.println("SECOND WITHDRAW status=" + second.getResponse().getStatus() + " body=" + second.getResponse().getContentAsString());
        org.assertj.core.api.Assertions.assertThat(second.getResponse().getStatus()).isEqualTo(200);
    }

    @DisplayName("탈퇴 후 보호 API 접근 시 FORBIDDEN(AUTH_WITHDRAWN_MEMBER)")
    @Test
    void withdrawnMember_accessProtectedApi() throws Exception {
        String token = loginAndGetToken("wd2@example.com", "Abcd1234!", "탈퇴2");

        // 탈퇴 처리
        mockMvc.perform(delete("/api/v1/members/me")
                        .header("Authorization", "Bearer " + token))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        // 탈퇴 후 프로필 조회 -> FORBIDDEN
        mockMvc.perform(get("/api/v1/members/me")
                        .header("Authorization", "Bearer " + token))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code", is("AUTH_WITHDRAWN_MEMBER")));
    }

    @DisplayName("탈퇴 후 로그인 시도 시 FORBIDDEN(AUTH_WITHDRAWN_MEMBER)")
    @Test
    void withdrawnMember_loginAttempt() throws Exception {
        String email = "wd3@example.com";
        String password = "Abcd1234!";
        String token = loginAndGetToken(email, password, "탈퇴3");

        // 탈퇴
        mockMvc.perform(delete("/api/v1/members/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // 로그인 재시도
        LoginRequest req = new LoginRequest(email, password);
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code", is("AUTH_WITHDRAWN_MEMBER")));
    }
}
