package com.capstone.web.member.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.capstone.web.member.domain.Member;
import com.capstone.web.member.dto.MemberRegisterRequest;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MemberControllerTest {

    private static final String SIGN_UP_URL = "/api/v1/members";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

        @BeforeEach
        void setUp() {
                memberRepository.deleteAll();
        }

    @DisplayName("정상적인 회원가입 요청은 201 응답과 함께 사용자 정보를 저장한다")
    @Test
    void registerMember_success() throws Exception {
        MemberRegisterRequest request = new MemberRegisterRequest(
                "test@example.com",
                "Abcd1234!",
                "Abcd1234!",
                "홍길동"
        );

        mockMvc.perform(post(SIGN_UP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.nickname").value("홍길동"))
                .andExpect(jsonPath("$.message").value("회원가입이 완료되었습니다."));

        Member saved = memberRepository.findByEmail("test@example.com").orElseThrow();
        assertThat(passwordEncoder.matches("Abcd1234!", saved.getPassword())).isTrue();
                assertThat(saved.getRole()).isNotNull();
                assertThat(saved.getExportScore()).isZero();
                assertThat(saved.getJoinedAt()).isNotNull();
                assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @DisplayName("중복 이메일로 요청하면 409와 오류 메시지를 반환한다")
    @Test
    void registerMember_duplicateEmail() throws Exception {
        Member existing = Member.builder()
                .email("dup@example.com")
                .password("encoded")
                .nickname("닉네임1")
                .build();
        memberRepository.save(existing);

        MemberRegisterRequest request = new MemberRegisterRequest(
                "dup@example.com",
                "Abcd1234!",
                "Abcd1234!",
                "새닉네임"
        );

        mockMvc.perform(post(SIGN_UP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("MEMBER_DUPLICATE_EMAIL"))
                .andExpect(jsonPath("$.errors[0].field").value("email"));
    }

    @DisplayName("비밀번호 정책 위반 시 400과 상세 오류 메시지를 반환한다")
    @Test
    void registerMember_invalidPasswordPolicy() throws Exception {
        MemberRegisterRequest request = new MemberRegisterRequest(
                "policy@example.com",
                "password",
                "password",
                "닉네임1"
        );

        mockMvc.perform(post(SIGN_UP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("password"))
                .andExpect(jsonPath("$.errors[0].message").value("비밀번호는 8자 이상 20자 이하이며 대문자/소문자/숫자/특수문자 중 2가지 이상을 포함해야 합니다."));
    }

    @DisplayName("비밀번호와 비밀번호 확인이 다르면 400을 반환한다")
    @Test
    void registerMember_passwordMismatch() throws Exception {
        MemberRegisterRequest request = new MemberRegisterRequest(
                "mismatch@example.com",
                "Abcd1234!",
                "Abcd1234?",
                "닉네임1"
        );

        mockMvc.perform(post(SIGN_UP_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("passwordConfirm"))
                .andExpect(jsonPath("$.errors[0].message").value("비밀번호가 일치하지 않습니다."));
    }
}
