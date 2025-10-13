package com.capstone.web.member.controller;

import com.capstone.web.auth.dto.LoginRequest;
import com.capstone.web.member.domain.Member;
import com.capstone.web.member.repository.MemberPasswordHistoryRepository;
import com.capstone.web.member.repository.MemberRepository;
import com.capstone.web.member.repository.MemberBlockRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MemberControllerPasswordChangeTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired MemberRepository memberRepository;
    @Autowired MemberBlockRepository memberBlockRepository;
    @Autowired MemberPasswordHistoryRepository memberPasswordHistoryRepository;
    @Autowired PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {
        // Clean up FK-dependent tables first
        memberPasswordHistoryRepository.deleteAll();
        memberBlockRepository.deleteAll();
        memberRepository.deleteAll();
    }

    private String jsonBody(String oldPw, String newPw, String confirmPw) {
        return "{" +
            "\"oldPassword\":\"" + oldPw + "\"," +
            "\"newPassword\":\"" + newPw + "\"," +
            "\"newPasswordConfirm\":\"" + confirmPw + "\"}";
    }

    private String loginAndGetToken(String email, String rawPassword, String nickname) throws Exception {
        Member existing = memberRepository.findByEmail(email).orElse(null);
        if (existing == null) {
            Member m = Member.builder()
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .nickname(nickname)
                .build();
            memberRepository.save(m);
        }
        LoginRequest req = new LoginRequest(email, rawPassword);
        String body = objectMapper.writeValueAsString(req);
        String response = mockMvc.perform(post("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("accessToken").asText();
    }

    private void changePasswordExpectNoContent(String token, String oldPw, String newPw) throws Exception {
        mockMvc.perform(patch("/api/v1/members/password")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonBody(oldPw, newPw, newPw)))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("비밀번호 변경 성공")
    void changePassword_success() throws Exception {
        String email = "pw_success@example.com";
        String oldPassword = "Abcd1234!";
        String newPassword = "Efgh5678!";
        String token = loginAndGetToken(email, oldPassword, "성공");
        changePasswordExpectNoContent(token, oldPassword, newPassword);
        Member updated = memberRepository.findByEmail(email).orElseThrow();
        assertThat(passwordEncoder.matches(newPassword, updated.getPassword())).isTrue();
    }

    @Test
    @DisplayName("동일한 새 비밀번호 사용 시 400")
    void changePassword_sameAsOld() throws Exception {
        String email = "pw_same@example.com";
        String oldPassword = "Abcd1234!";
        String token = loginAndGetToken(email, oldPassword, "동일");
        mockMvc.perform(patch("/api/v1/members/password")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody(oldPassword, oldPassword, oldPassword)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("oldPassword 공백이면 400")
    void changePassword_blankOldPassword() throws Exception {
        String email = "pw_blank_old@example.com";
        String oldPassword = "Abcd1234!";
        String token = loginAndGetToken(email, oldPassword, "공백올드");
        mockMvc.perform(patch("/api/v1/members/password")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody("", "Efgh5678!", "Efgh5678!")))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("새 비밀번호 정책 위반(길이 부족) 400")
    void changePassword_policyTooShort() throws Exception {
        String email = "pw_short@example.com";
        String oldPassword = "Abcd1234!";
        String token = loginAndGetToken(email, oldPassword, "짧음");
        mockMvc.perform(patch("/api/v1/members/password")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody(oldPassword, "Ab1!", "Ab1!"))) // 4자
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("새 비밀번호와 확인 불일치 400")
    void changePassword_newPasswordMismatch() throws Exception {
        String email = "pw_mismatch@example.com";
        String oldPassword = "Abcd1234!";
        String token = loginAndGetToken(email, oldPassword, "불일치");
        mockMvc.perform(patch("/api/v1/members/password")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody(oldPassword, "Efgh5678!", "Zzzz9999!")))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("여러 비밀번호 정책 위반 사유 동시 반환")
    void changePassword_multiplePolicyViolations() throws Exception {
        String email = "pw_multi@example.com";
        String oldPassword = "Abcd1234!";
        String token = loginAndGetToken(email, oldPassword, "다중위반");
        // 너무 짧고 + 조합 불충족 (예: 소문자만)
        String body = jsonBody(oldPassword, "abc", "abc");
        String response = mockMvc.perform(patch("/api/v1/members/password")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest())
            .andReturn().getResponse().getContentAsString();
        assertThat(response).contains("8자 이상");
        assertThat(response).contains("2종 이상");
    }

    @Test
    @DisplayName("최근 5회 이내 사용한 비밀번호 재사용 시 400")
    void changePassword_reuseRecentPassword() throws Exception {
        String email = "pw_reuse@example.com";
        String oldPassword = "Abcd1234!";
        String nickname = "재사용";
        String token = loginAndGetToken(email, oldPassword, nickname);

        String p1 = "Efgh5678!";
        changePasswordExpectNoContent(token, oldPassword, p1);
        token = loginAndGetToken(email, p1, nickname);

        String p2 = "Ijkl9012!";
        changePasswordExpectNoContent(token, p1, p2);
        token = loginAndGetToken(email, p2, nickname);

        String p3 = "Mnop3456!";
        changePasswordExpectNoContent(token, p2, p3);
        token = loginAndGetToken(email, p3, nickname);

        // 최근(3회) 사용했던 p1 재사용 시도 -> 400
        String response = mockMvc.perform(patch("/api/v1/members/password")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody(p3, p1, p1)))
            .andExpect(status().isBadRequest())
            .andReturn().getResponse().getContentAsString();
        assertThat(response).contains("최근에 사용한 비밀번호");
    }

    @Test
    @DisplayName("비밀번호 이력 5개 초과 시 pruning (가장 오래된 삭제)")
    void changePassword_pruningKeepsOnlyFive() throws Exception {
        String email = "pw_prune@example.com";
        String oldPassword = "Abcd1234!";
        String nickname = "프루닝";
        String token = loginAndGetToken(email, oldPassword, nickname);

        String[] passwords = {"Pppp1111!", "Qqqq2222!", "Rrrr3333!", "Ssss4444!", "Tttt5555!", "Uuuu6666!"};
        String prev = oldPassword;
        for (String pw : passwords) {
            changePasswordExpectNoContent(token, prev, pw);
            token = loginAndGetToken(email, pw, nickname);
            prev = pw;
        }
        // 이제 history에는 최신 5개만 존재해야 함
    // 사용자 기준 history 개수 검증 (전체 count 는 의미 없음)
        Member member = memberRepository.findByEmail(email).orElseThrow();
        long memberHistoryCount = memberPasswordHistoryRepository.findByMemberOrderByChangedAtDesc(member).size();
        assertThat(memberHistoryCount).isEqualTo(5);
    }
}
