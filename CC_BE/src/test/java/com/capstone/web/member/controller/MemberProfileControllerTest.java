package com.capstone.web.member.controller;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.capstone.web.auth.dto.LoginRequest;
import com.capstone.web.member.domain.Member;
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MemberProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private MemberBlockRepository memberBlockRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {
        if (memberBlockRepository != null) memberBlockRepository.deleteAll();
        memberRepository.deleteAll();
    }

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

    @DisplayName("내 프로필 조회 성공")
    @Test
    void me_success() throws Exception {
        String token = loginAndGetToken("me@example.com", "Abcd1234!", "나자신");

        mockMvc.perform(get("/api/v1/members/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("me@example.com")))
                .andExpect(jsonPath("$.nickname", is("나자신")))
                .andExpect(jsonPath("$.joinedAt", notNullValue()));
    }

    @DisplayName("토큰 없이 내 프로필 조회하면 403 Forbidden")
    @Test
    void me_unauthorized_noToken() throws Exception {
        // 인증 정보가 없으므로 보호된 리소스 접근이 금지됨 (403)
        mockMvc.perform(get("/api/v1/members/me"))
                .andExpect(status().isForbidden());
    }

    @DisplayName("잘못된 토큰으로 조회하면 401")
    @Test
    void me_invalidToken() throws Exception {
        mockMvc.perform(get("/api/v1/members/me")
                        .header("Authorization", "Bearer invalid.token.value"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code", is("AUTH_INVALID_TOKEN")));
    }

    @DisplayName("회원 단건 조회 성공")
    @Test
    void getById_success() throws Exception {
        Member m = Member.builder()
                .email("target@example.com")
                .password(passwordEncoder.encode("Abcd1234!"))
                .nickname("타겟")
                .build();
        memberRepository.save(m);

        mockMvc.perform(get("/api/v1/members/" + m.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("target@example.com")))
                .andExpect(jsonPath("$.nickname", is("타겟")));
    }

    @DisplayName("없는 회원을 조회하면 404")
    @Test
    void getById_notFound() throws Exception {
        mockMvc.perform(get("/api/v1/members/999999"))
                .andExpect(status().isNotFound());
    }
}
