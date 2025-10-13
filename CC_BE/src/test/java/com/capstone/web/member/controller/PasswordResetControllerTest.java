package com.capstone.web.member.controller;

import com.capstone.web.member.domain.Member;
import com.capstone.web.member.domain.PasswordResetToken;
import com.capstone.web.member.dto.PasswordResetRequest;
import com.capstone.web.member.dto.PasswordResetConfirmRequest;
import com.capstone.web.member.repository.MemberRepository;
import com.capstone.web.member.repository.PasswordResetTokenRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PasswordResetControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired MemberRepository memberRepository;
    @Autowired PasswordResetTokenRepository tokenRepository;
    @Autowired PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        tokenRepository.deleteAll();
        memberRepository.deleteAll();
    }

    private Member createMember(String email, String password, String nickname) {
        Member m = Member.builder()
                .email(email)
                // 테스트에서도 실제 운영 경로와 동일하게 인코딩하여 동일 비밀번호 감지 로직이 정상 동작하도록 한다.
                .password(passwordEncoder.encode(password))
                .nickname(nickname)
                .build();
        return memberRepository.save(m);
    }

    @Test
    @DisplayName("비밀번호 재설정 요청 성공 시 204 및 토큰 생성")
    void requestReset_success() throws Exception {
        createMember("reset1@example.com", "Abcd1234!", "사용자1");
        PasswordResetRequest req = new PasswordResetRequest("reset1@example.com");
        mockMvc.perform(post("/api/v1/auth/password-reset")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isNoContent());
        assertThat(tokenRepository.findAll()).hasSize(1);
    }

    @Test
    @DisplayName("존재하지 않는 이메일 비밀번호 재설정 요청 시 404")
    void requestReset_emailNotFound() throws Exception {
        PasswordResetRequest req = new PasswordResetRequest("nope@example.com");
        mockMvc.perform(post("/api/v1/auth/password-reset")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("이전 활성 토큰 무효화 후 새 토큰 발급")
    void requestReset_invalidatePrevious() throws Exception {
        Member m = createMember("reset2@example.com", "Abcd1234!", "사용자2");
        // 첫 요청
        mockMvc.perform(post("/api/v1/auth/password-reset")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new PasswordResetRequest(m.getEmail()))))
            .andExpect(status().isNoContent());
        assertThat(tokenRepository.findAll()).hasSize(1);
        PasswordResetToken first = tokenRepository.findAll().get(0);
        // 둘째 요청 -> 이전 토큰 invalidatedAt 세팅
        mockMvc.perform(post("/api/v1/auth/password-reset")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new PasswordResetRequest(m.getEmail()))))
            .andExpect(status().isNoContent());
        assertThat(tokenRepository.findAll()).hasSize(2);
        PasswordResetToken refetchedFirst = tokenRepository.findById(first.getId()).orElseThrow();
        assertThat(refetchedFirst.getInvalidatedAt()).isNotNull();
    }

    @Test
    @DisplayName("만료된 토큰으로 재설정 시 404")
    void confirmReset_expired() throws Exception {
        Member m = createMember("reset3@example.com", "Abcd1234!", "사용자3");
        // 토큰 직접 저장 (만료된 상태)
        PasswordResetToken token = PasswordResetToken.builder()
                .member(m)
                .token("expired-token")
                .createdAt(LocalDateTime.now().minusHours(1))
                .expiresAt(LocalDateTime.now().minusMinutes(1))
                .build();
        tokenRepository.save(token);
        PasswordResetConfirmRequest req = new PasswordResetConfirmRequest("expired-token", "Newpass1!", "Newpass1!");
        mockMvc.perform(post("/api/v1/auth/password-reset/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("토큰 사용 후 재사용 시 400")
    void confirmReset_reuseToken() throws Exception {
        Member m = createMember("reset4@example.com", "Abcd1234!", "사용자4");
        mockMvc.perform(post("/api/v1/auth/password-reset")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new PasswordResetRequest(m.getEmail()))))
            .andExpect(status().isNoContent());
        PasswordResetToken token = tokenRepository.findAll().get(0);
        PasswordResetConfirmRequest first = new PasswordResetConfirmRequest(token.getToken(), "Newpass1!", "Newpass1!");
        mockMvc.perform(post("/api/v1/auth/password-reset/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(first)))
            .andExpect(status().isNoContent());
        // 재사용
        PasswordResetConfirmRequest second = new PasswordResetConfirmRequest(token.getToken(), "Another2!", "Another2!");
        mockMvc.perform(post("/api/v1/auth/password-reset/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(second)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("비밀번호 확인 불일치 시 400")
    void confirmReset_mismatch() throws Exception {
        Member m = createMember("reset5@example.com", "Abcd1234!", "사용자5");
        mockMvc.perform(post("/api/v1/auth/password-reset")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new PasswordResetRequest(m.getEmail()))))
            .andExpect(status().isNoContent());
        PasswordResetToken token = tokenRepository.findAll().get(0);
        PasswordResetConfirmRequest req = new PasswordResetConfirmRequest(token.getToken(), "Newpass1!", "Mismatch1!");
        mockMvc.perform(post("/api/v1/auth/password-reset/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("동일 비밀번호로 재설정 시 400")
    void confirmReset_sameAsOld() throws Exception {
        Member m = createMember("reset6@example.com", "Abcd1234!", "사용자6");
        mockMvc.perform(post("/api/v1/auth/password-reset")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new PasswordResetRequest(m.getEmail()))))
            .andExpect(status().isNoContent());
        PasswordResetToken token = tokenRepository.findAll().get(0);
        PasswordResetConfirmRequest req = new PasswordResetConfirmRequest(token.getToken(), "Abcd1234!", "Abcd1234!");
        mockMvc.perform(post("/api/v1/auth/password-reset/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("정상 재설정 후 토큰 usedAt 세팅 및 비밀번호 변경")
    void confirmReset_success() throws Exception {
        Member m = createMember("reset7@example.com", "Abcd1234!", "사용자7");
        mockMvc.perform(post("/api/v1/auth/password-reset")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new PasswordResetRequest(m.getEmail()))))
            .andExpect(status().isNoContent());
        PasswordResetToken token = tokenRepository.findAll().get(0);
        PasswordResetConfirmRequest req = new PasswordResetConfirmRequest(token.getToken(), "Newpass1!", "Newpass1!");
        mockMvc.perform(post("/api/v1/auth/password-reset/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isNoContent());
        PasswordResetToken updated = tokenRepository.findById(token.getId()).orElseThrow();
        assertThat(updated.getUsedAt()).isNotNull();
    }
}
