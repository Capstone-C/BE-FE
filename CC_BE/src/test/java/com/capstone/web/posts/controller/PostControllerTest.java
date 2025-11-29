package com.capstone.web.posts.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.capstone.web.auth.dto.LoginRequest;
import com.capstone.web.category.domain.Category;
import com.capstone.web.category.repository.CategoryRepository;
import com.capstone.web.common.S3UploadService;
import com.capstone.web.member.domain.Member;
import com.capstone.web.member.repository.MemberRepository;
import com.capstone.web.posts.domain.Posts;
import com.capstone.web.posts.dto.PostDto;
import com.capstone.web.posts.dto.PostIngredientDto;
import com.capstone.web.posts.repository.PostsRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean; // Spring Boot 3.4+
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

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

    @MockitoBean
    private S3UploadService s3UploadService;

    private Member author;
    private Category category;
    private String userToken;

    private final Posts.DietType DEFAULT_DIET = Posts.DietType.GENERAL;
    private final List<PostIngredientDto.Request> DEFAULT_ING = Collections.emptyList();

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
        when(s3UploadService.uploadFile(any(MultipartFile.class))).thenReturn("https://mock-s3-url.com/image.jpg");

        postsRepository.deleteAll();
        memberRepository.deleteAll();
        categoryRepository.deleteAll();

        author = memberRepository.save(Member.builder().email("author@example.com").nickname("글쓴이").password(passwordEncoder.encode("pw")).build());
        category = categoryRepository.save(Category.builder().name("자유").type(Category.CategoryType.FREE).build());
        userToken = loginAndGetToken(author, "pw");
    }

    @DisplayName("게시글 생성 (썸네일 + 추가 이미지 다중 업로드) 성공")
    @Test
    void createPost_Success() throws Exception {
        PostDto.CreateRequest requestDto = new PostDto.CreateRequest(
                category.getId(),
                "다중 파일 업로드 제목",
                "내용입니다내용입니다",
                Posts.PostStatus.PUBLISHED,
                false,
                null, // thumbnailUrl
                DEFAULT_DIET, 30, 1, Posts.Difficulty.LOW, DEFAULT_ING
        );

        MockMultipartFile jsonPart = new MockMultipartFile(
                "request", "", "application/json",
                objectMapper.writeValueAsString(requestDto).getBytes(StandardCharsets.UTF_8)
        );

        MockMultipartFile thumbnailPart = new MockMultipartFile(
                "thumbnail", "thumb.jpg", "image/jpeg", "thumbnail content".getBytes()
        );

        MockMultipartFile extraFile1 = new MockMultipartFile(
                "files", "extra1.jpg", "image/jpeg", "extra content 1".getBytes()
        );

        MockMultipartFile extraFile2 = new MockMultipartFile(
                "files", "extra2.jpg", "image/jpeg", "extra content 2".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/posts")
                        .file(jsonPart)
                        .file(thumbnailPart)
                        .file(extraFile1)
                        .file(extraFile2)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith("/api/posts/")));
    }

    @DisplayName("유효성 검증 실패 시 400 Bad Request")
    @Test
    void createPost_Fail_Validation() throws Exception {
        PostDto.CreateRequest requestDto = new PostDto.CreateRequest(
                category.getId(),
                "", // 제목 비움 (실패 유도)
                "내용",
                Posts.PostStatus.PUBLISHED,
                false,
                null,
                DEFAULT_DIET, 30, 1, Posts.Difficulty.LOW, DEFAULT_ING
        );

        MockMultipartFile jsonPart = new MockMultipartFile(
                "request", "", "application/json",
                objectMapper.writeValueAsString(requestDto).getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/v1/posts")
                        .file(jsonPart)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("ID로 게시글 조회 성공")
    @Test
    void getPostById_Success() throws Exception {
        Posts savedPost = postsRepository.save(Posts.builder().authorId(author).category(category).title("조회할 게시글").content("내용").status(Posts.PostStatus.PUBLISHED).isRecipe(false).build());

        mockMvc.perform(get("/api/v1/posts/{id}", savedPost.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("조회할 게시글")));
    }

    @DisplayName("게시글 수정 (Multipart -> PUT) 성공")
    @Test
    void updatePost_Success() throws Exception {
        Posts saved = postsRepository.save(Posts.builder().authorId(author).category(category).title("원제목").content("원내용").build());

        // [수정] 제목 길이를 5자 이상으로 변경
        PostDto.UpdateRequest updateDto = new PostDto.UpdateRequest(
                "수정된제목입니다", // 5자 이상 (필수)
                "수정내용입니다. 길이는 충분합니다.", // 10자 이상 (필수)
                category.getId(),
                Posts.PostStatus.PUBLISHED,
                false,
                null, // thumbnailUrl
                DEFAULT_DIET, 30, 1, Posts.Difficulty.LOW, DEFAULT_ING
        );

        MockMultipartFile jsonPart = new MockMultipartFile(
                "request", "", "application/json",
                objectMapper.writeValueAsString(updateDto).getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/v1/posts/{id}", saved.getId())
                        .file(jsonPart)
                        .with(req -> { req.setMethod("PUT"); return req; })
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());
    }

    @DisplayName("게시글 삭제 성공")
    @Test
    void deletePost_Success() throws Exception {
        Posts post = postsRepository.save(Posts.builder().authorId(author).category(category).title("삭제").content("내용").build());

        mockMvc.perform(delete("/api/v1/posts/{id}", post.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNoContent());
    }
}