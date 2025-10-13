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
    @Autowired PasswordEncoder passwordEncoder;

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

    @Test
    @DisplayName("비밀번호 변경 성공")
    void changePassword_success() throws Exception {
	String email = "pw1@example.com";
	String oldPassword = "Abcd1234!";
	String newPassword = "Efgh5678!";
	String nickname = "비번변경";
	String token = loginAndGetToken(email, oldPassword, nickname);

	String reqBody = "{" +
		"\"oldPassword\":\"" + oldPassword + "\"," +
		"\"newPassword\":\"" + newPassword + "\"," +
		"\"newPasswordConfirm\":\"" + newPassword + "\"}";

	mockMvc.perform(patch("/api/v1/members/password")
			.header("Authorization", "Bearer " + token)
			.contentType(MediaType.APPLICATION_JSON)
			.content(reqBody))
		.andExpect(status().isNoContent());

	Member updated = memberRepository.findByEmail(email).orElseThrow();
	assertThat(passwordEncoder.matches(newPassword, updated.getPassword())).isTrue();
    }

    @Test
    @DisplayName("기존 비밀번호 불일치시 400")
    void changePassword_invalidOld() throws Exception {
	String email = "pw2@example.com";
	String oldPassword = "Abcd1234!";
	String newPassword = "Efgh5678!";
	String nickname = "비번불일치";
	String token = loginAndGetToken(email, oldPassword, nickname);

	String reqBody = "{" +
		"\"oldPassword\":\"잘못된비번\"," +
		"\"newPassword\":\"" + newPassword + "\"," +
		"\"newPasswordConfirm\":\"" + newPassword + "\"}";

	mockMvc.perform(patch("/api/v1/members/password")
			.header("Authorization", "Bearer " + token)
			.contentType(MediaType.APPLICATION_JSON)
			.content(reqBody))
		.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("새 비밀번호가 기존과 동일하면 400")
    void changePassword_sameAsOld() throws Exception {
	String email = "pw3@example.com";
	String oldPassword = "Abcd1234!";
	String nickname = "동일비번";
	String token = loginAndGetToken(email, oldPassword, nickname);

	String reqBody = "{" +
		"\"oldPassword\":\"" + oldPassword + "\"," +
		"\"newPassword\":\"" + oldPassword + "\"," +
		"\"newPasswordConfirm\":\"" + oldPassword + "\"}";

	mockMvc.perform(patch("/api/v1/members/password")
			.header("Authorization", "Bearer " + token)
			.contentType(MediaType.APPLICATION_JSON)
			.content(reqBody))
		.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("oldPassword 공백이면 400")
    void changePassword_blankOldPassword() throws Exception {
    	String email = "pw4@example.com";
    	String oldPassword = "Abcd1234!";
    	String nickname = "공백올드";
    	String token = loginAndGetToken(email, oldPassword, nickname);

    	String reqBody = "{" +
    		"\"oldPassword\":\"\"," +
    		"\"newPassword\":\"Efgh5678!\"," +
    		"\"newPasswordConfirm\":\"Efgh5678!\"}";

    	mockMvc.perform(patch("/api/v1/members/password")
    		.header("Authorization", "Bearer " + token)
    		.contentType(MediaType.APPLICATION_JSON)
    		.content(reqBody))
    		.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("새 비밀번호 정책 위반(너무 짧음) 400")
    void changePassword_policyTooShort() throws Exception {
    	String email = "pw5@example.com";
    	String oldPassword = "Abcd1234!";
    	String nickname = "정책짧음";
    	String token = loginAndGetToken(email, oldPassword, nickname);

    	String reqBody = "{" +
    		"\"oldPassword\":\"" + oldPassword + "\"," +
    		"\"newPassword\":\"Ab1!\"," +  // 4자
    		"\"newPasswordConfirm\":\"Ab1!\"}";

    	mockMvc.perform(patch("/api/v1/members/password")
    		.header("Authorization", "Bearer " + token)
    		.contentType(MediaType.APPLICATION_JSON)
    		.content(reqBody))
    		.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("새 비밀번호와 확인 불일치 400")
    void changePassword_newPasswordMismatch() throws Exception {
    	String email = "pw6@example.com";
    	String oldPassword = "Abcd1234!";
    	String nickname = "불일치";
    	String token = loginAndGetToken(email, oldPassword, nickname);

    	String reqBody = "{" +
    		"\"oldPassword\":\"" + oldPassword + "\"," +
    		"\"newPassword\":\"Efgh5678!\"," +
    		"\"newPasswordConfirm\":\"Zzzz9999!\"}";

    	mockMvc.perform(patch("/api/v1/members/password")
    		.header("Authorization", "Bearer " + token)
    		.contentType(MediaType.APPLICATION_JSON)
    		.content(reqBody))
    		.andExpect(status().isBadRequest());
    }
}
