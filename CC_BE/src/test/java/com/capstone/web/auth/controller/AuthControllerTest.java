package com.capstone.web.auth.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.capstone.web.auth.dto.LoginRequest;
import com.capstone.web.member.domain.Member;
import com.capstone.web.member.repository.MemberRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.LocalDateTime;
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerTest {

    private static final String LOGIN_URL = "/api/v1/auth/login";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void clean() {
        memberRepository.deleteAll();
    }

    @DisplayName("로그인 성공 시 토큰과 회원 요약 정보를 반환한다")
    @Test
    void login_success() throws Exception {
        Member member = Member.builder()
                .email("login@example.com")
                .password(passwordEncoder.encode("Abcd1234!"))
                .nickname("로그인유저")
                .build();
        memberRepository.save(member);

        LoginRequest request = new LoginRequest("login@example.com", "Abcd1234!");

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                        .andExpect(jsonPath("$.accessToken", not(blankOrNullString())))
                .andExpect(jsonPath("$.member.email", is("login@example.com")))
                .andExpect(jsonPath("$.member.nickname", is("로그인유저")))
                .andExpect(jsonPath("$.member.role", is("USER")));

        Member refreshed = memberRepository.findByEmail("login@example.com").orElseThrow();
        assertThat(refreshed.getLastLoginAt()).isNotNull();
        assertThat(Duration.between(refreshed.getLastLoginAt(), LocalDateTime.now()).abs()).isLessThan(Duration.ofSeconds(5));
    }

    @DisplayName("등록되지 않은 이메일로 로그인 시도하면 401을 반환한다")
    @Test
    void login_unknownEmail() throws Exception {
        LoginRequest request = new LoginRequest("unknown@example.com", "Abcd1234!");

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code", is("AUTH_INVALID_CREDENTIALS")));
    }

    @DisplayName("비밀번호 불일치 시 401을 반환한다")
    @Test
    void login_wrongPassword() throws Exception {
        Member member = Member.builder()
                .email("wrongpw@example.com")
                .password(passwordEncoder.encode("Abcd1234!"))
                .nickname("비번유저")
                .build();
        memberRepository.save(member);

        LoginRequest request = new LoginRequest("wrongpw@example.com", "Wrong1234!");

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code", is("AUTH_INVALID_CREDENTIALS")));
    }

    @DisplayName("탈퇴한 회원은 로그인할 수 없다")
    @Test
    void login_withdrawnMember() throws Exception {
        Member member = Member.builder()
                .email("withdrawn@example.com")
                .password(passwordEncoder.encode("Abcd1234!"))
                .nickname("탈퇴회원")
                .build();
        member.softDelete();
        memberRepository.save(member);

        LoginRequest request = new LoginRequest("withdrawn@example.com", "Abcd1234!");

        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code", is("AUTH_WITHDRAWN_MEMBER")));
    }
}
