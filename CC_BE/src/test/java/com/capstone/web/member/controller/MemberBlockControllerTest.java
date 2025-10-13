package com.capstone.web.member.controller;

import com.capstone.web.member.domain.Member;
import com.capstone.web.member.repository.MemberRepository;
import com.capstone.web.auth.jwt.JwtTokenProvider;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MemberBlockControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    com.capstone.web.member.repository.MemberBlockRepository memberBlockRepository;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    JwtTokenProvider jwtTokenProvider;

    Member member1;
    Member member2;
    Member member3;
    String token1;

    @BeforeEach
    void setup() {
        memberBlockRepository.deleteAll();
        memberRepository.deleteAll();
        member1 = memberRepository.save(Member.builder().email("a@test.com").password("pass1").nickname("a").build());
        member2 = memberRepository.save(Member.builder().email("b@test.com").password("pass2").nickname("b").build());
        member3 = memberRepository.save(Member.builder().email("c@test.com").password("pass3").nickname("c").build());
        token1 = jwtTokenProvider.createToken(member1.getId(), member1.getRole());
    }

    @Test
    @DisplayName("차단 성공")
    void block_success() throws Exception {
    String body = "{\"blockedId\": " + member2.getId() + "}";
    mockMvc.perform(post("/api/v1/members/blocks")
            .header("Authorization", "Bearer " + token1)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("자기 자신 차단 불가")
    void self_block_fail() throws Exception {
    String body = "{\"blockedId\": " + member1.getId() + "}";
    mockMvc.perform(post("/api/v1/members/blocks")
            .header("Authorization", "Bearer " + token1)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("중복 차단 불가")
    void duplicate_block_fail() throws Exception {
    String body = "{\"blockedId\": " + member2.getId() + "}";
    mockMvc.perform(post("/api/v1/members/blocks")
            .header("Authorization", "Bearer " + token1)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isCreated());
    mockMvc.perform(post("/api/v1/members/blocks")
            .header("Authorization", "Bearer " + token1)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("차단 목록 조회 정렬 (최신순)")
    void list_blocks() throws Exception {
        mockMvc.perform(post("/api/v1/members/blocks")
        .header("Authorization", "Bearer " + token1)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"blockedId\": " + member2.getId() + "}"))
            .andExpect(status().isCreated());
        Thread.sleep(10); // createdAt ordering 차이를 위해 아주 짧게 대기
        mockMvc.perform(post("/api/v1/members/blocks")
        .header("Authorization", "Bearer " + token1)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"blockedId\": " + member3.getId() + "}"))
            .andExpect(status().isCreated());
    mockMvc.perform(get("/api/v1/members/blocks")
        .header("Authorization", "Bearer " + token1))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].blockedId").value(member3.getId()))
            .andExpect(jsonPath("$[1].blockedId").value(member2.getId()));
    }

    @Test
    @DisplayName("차단 해제 성공")
    void unblock_success() throws Exception {
        String body = "{\"blockedId\": " + member2.getId() + "}";
        mockMvc.perform(post("/api/v1/members/blocks")
        .header("Authorization", "Bearer " + token1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isCreated());
    mockMvc.perform(delete("/api/v1/members/blocks/" + member2.getId())
        .header("Authorization", "Bearer " + token1))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("차단되지 않은 대상 해제 실패")
    void unblock_not_blocked_fail() throws Exception {
    mockMvc.perform(delete("/api/v1/members/blocks/" + member2.getId())
        .header("Authorization", "Bearer " + token1))
            .andExpect(status().isBadRequest());
    }
}
