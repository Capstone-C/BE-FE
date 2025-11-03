package com.capstone.web.posts.controller;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.capstone.web.auth.dto.LoginRequest;
import com.capstone.web.category.domain.Category;
import com.capstone.web.category.repository.CategoryRepository;
import com.capstone.web.member.domain.Member;
import com.capstone.web.member.repository.MemberRepository;
import com.capstone.web.posts.domain.Posts;
import com.capstone.web.posts.dto.PostDto;
import com.capstone.web.posts.repository.PostsRepository;
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
class PostControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private PostsRepository postsRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private Member author;
    private Category category;
    private String userToken;

    // 로그인 후 토큰을 반환하는 헬퍼 메서드
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
        postsRepository.deleteAll();
        memberRepository.deleteAll();
        categoryRepository.deleteAll();

        // 테스트용 사용자 생성 (비밀번호 암호화)
        author = Member.builder()
                .email("author@example.com")
                .nickname("글쓴이")
                .password(passwordEncoder.encode("password123!"))
                .build();
        memberRepository.save(author);

        // 테스트용 카테고리 생성
        category = categoryRepository.save(Category.builder().name("자유게시판").type(Category.CategoryType.FREE).build());

        // 모든 테스트에서 사용할 토큰을 미리 발급
        userToken = loginAndGetToken(author, "password123!");
    }

    @DisplayName("게시글 생성 요청에 성공하고 201 Created를 반환한다")
    @Test
    void createPost_Success() throws Exception {
        // given
        // DTO에서 authorId 제거
        PostDto.CreateRequest request = new PostDto.CreateRequest(category.getId(), "새 게시글 제목", "새 게시글 내용", Posts.PostStatus.PUBLISHED, false);
        String requestBody = objectMapper.writeValueAsString(request);

        // when
        // (수정) URL에 /v1 추가
        ResultActions result = mockMvc.perform(post("/api/v1/posts")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        result.andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith("/api/posts/"))); // Location 헤더는 v1을 포함하지 않을 수 있으므로 /api/posts/로 검증
    }

    @DisplayName("유효성 검증 실패 시 400 Bad Request를 반환한다")
    @Test
    void createPost_Fail_Validation() throws Exception {
        // given
        // DTO에서 authorId 제거
        PostDto.CreateRequest request = new PostDto.CreateRequest(category.getId(), "", "내용", Posts.PostStatus.PUBLISHED, false); // 제목 비움
        String requestBody = objectMapper.writeValueAsString(request);

        // when
        // (수정) URL에 /v1 추가
        ResultActions result = mockMvc.perform(post("/api/v1/posts")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        result.andExpect(status().isBadRequest());
    }

    @DisplayName("ID로 게시글을 성공적으로 조회하고 200 OK를 반환한다")
    @Test
    void getPostById_Success() throws Exception {
        // given
        Posts savedPost = postsRepository.save(Posts.builder().authorId(author).category(category).title("조회할 게시글").content("내용").status(Posts.PostStatus.PUBLISHED).build());

        // when
        // (수정) URL에 /v1 추가
        ResultActions result = mockMvc.perform(get("/api/v1/posts/{id}", savedPost.getId())
                .header("Authorization", "Bearer " + userToken));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(savedPost.getId().intValue())))
                .andExpect(jsonPath("$.title", is("조회할 게시글")))
                .andExpect(jsonPath("$.authorId", is(author.getId().intValue())));
    }

    @DisplayName("존재하지 않는 ID로 조회 시 404 Not Found를 반환한다")
    @Test
    void getPostById_Fail_NotFound() throws Exception {
        // given
        Long nonExistentId = 999L;

        // when
        // (수정) URL에 /v1 추가
        ResultActions result = mockMvc.perform(get("/api/v1/posts/{id}", nonExistentId)
                .header("Authorization", "Bearer " + userToken));

        // then
        result.andExpect(status().isNotFound());
    }

    @DisplayName("게시글을 성공적으로 수정하고 200 OK를 반환한다")
    @Test
    void updatePost_Success() throws Exception {
        // given
        Posts originalPost = postsRepository.save(Posts.builder().authorId(author).category(category).title("원본 제목").content("원본 내용").status(Posts.PostStatus.PUBLISHED).build());
        Category newCategory = categoryRepository.save(Category.builder().name("공지사항").type(Category.CategoryType.QA).build());
        PostDto.UpdateRequest request = new PostDto.UpdateRequest("수정된 제목", "수정된 내용", newCategory.getId(), Posts.PostStatus.ARCHIVED, true);
        String requestBody = objectMapper.writeValueAsString(request);

        // when
        // (수정) URL에 /v1 추가
        ResultActions result = mockMvc.perform(put("/api/v1/posts/{id}", originalPost.getId())
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        result.andExpect(status().isOk());
    }

    @DisplayName("게시글을 성공적으로 삭제하고 204 No Content를 반환한다")
    @Test
    void deletePost_Success() throws Exception {
        // given
        Posts postToDelete = postsRepository.save(Posts.builder().authorId(author).category(category).title("삭제될 게시글").content("내용").build());

        // when
        // (수정) URL에 /v1 추가
        ResultActions result = mockMvc.perform(delete("/api/v1/posts/{id}", postToDelete.getId())
                .header("Authorization", "Bearer " + userToken));

        // then
        result.andExpect(status().isNoContent());
    }
}