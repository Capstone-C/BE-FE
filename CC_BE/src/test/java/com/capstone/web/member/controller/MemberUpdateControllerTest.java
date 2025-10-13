package com.capstone.web.member.controller;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MemberUpdateControllerTest {

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
        String response = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("accessToken").asText();
    }

        @DisplayName("닉네임만 수정 성공")
        @Test
        void updateNickname() throws Exception {
                String token = loginAndGetToken("upd1@example.com", "Abcd1234!", "초기닉");
                MockMultipartFile nicknamePart = new MockMultipartFile("nickname", "", "text/plain", "새닉".getBytes());
                mockMvc.perform(multipart("/api/v1/members/me")
                                .file(nicknamePart)
                                .with(req -> { req.setMethod("PATCH"); return req; })
                                .header("Authorization", "Bearer " + token))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.nickname", is("새닉")));
        }

        @DisplayName("이미지와 닉네임 동시 수정 성공")
        @Test
        void updateNicknameAndImage() throws Exception {
                String token = loginAndGetToken("upd2@example.com", "Abcd1234!", "닉2");
                MockMultipartFile image = new MockMultipartFile("profileImage", "a.png", "image/png", new byte[]{1,2,3});

                MockMultipartFile nicknamePart = new MockMultipartFile("nickname", "", "text/plain", "바뀐닉".getBytes());
                mockMvc.perform(multipart("/api/v1/members/me")
                                .file(image)
                                .file(nicknamePart)
                                .with(req -> { req.setMethod("PATCH"); return req; })
                                .header("Authorization", "Bearer " + token))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.nickname", is("바뀐닉")))
                                .andExpect(jsonPath("$.profile", notNullValue()));
        }

        @DisplayName("잘못된 닉네임 형식 400")
        @Test
        void invalidNickname() throws Exception {
                String token = loginAndGetToken("upd3@example.com", "Abcd1234!", "닉3");
                MockMultipartFile nicknamePart = new MockMultipartFile("nickname", "", "text/plain", "a".getBytes());
                mockMvc.perform(multipart("/api/v1/members/me")
                                .file(nicknamePart)
                                .with(req -> { req.setMethod("PATCH"); return req; })
                                .header("Authorization", "Bearer " + token))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code", is("MEMBER_INVALID_NICKNAME")));
        }

    @DisplayName("중복 닉네임 409")
    @Test
    void duplicateNickname() throws Exception {
        // 다른 사용자 닉네임 선점
        Member other = Member.builder()
                .email("other@example.com")
                .password(passwordEncoder.encode("Abcd1234!"))
                .nickname("중복" )
                .build();
        memberRepository.save(other);

        String token = loginAndGetToken("upd4@example.com", "Abcd1234!", "내닉");

        MockMultipartFile nicknamePart = new MockMultipartFile("nickname", "", "text/plain", "중복".getBytes());
        mockMvc.perform(multipart("/api/v1/members/me")
                        .file(nicknamePart)
                        .with(req -> { req.setMethod("PATCH"); return req; })
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code", is("MEMBER_DUPLICATE_NICKNAME")));
    }

    @DisplayName("잘못된 이미지 타입 400")
    @Test
    void invalidImageType() throws Exception {
        String token = loginAndGetToken("upd5@example.com", "Abcd1234!", "닉5");
        MockMultipartFile image = new MockMultipartFile("profileImage", "a.txt", "text/plain", "abc".getBytes());
        mockMvc.perform(multipart("/api/v1/members/me")
                        .file(image)
                        .with(req -> { req.setMethod("PATCH"); return req; })
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("MEMBER_PROFILE_INVALID_TYPE")));
    }
}
