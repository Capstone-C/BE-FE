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
import com.capstone.web.posts.dto.PostIngredientDto; // (추가)
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

import java.util.Collections; // (추가)
import java.util.List; // (추가)

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

    // (추가) 레시피 필드를 위한 기본값
    private final Posts.DietType DEFAULT_DIET_TYPE = Posts.DietType.GENERAL;
    private final Integer DEFAULT_COOK_TIME = 30;
    private final Integer DEFAULT_SERVINGS = 2;
    private final Posts.Difficulty DEFAULT_DIFFICULTY = Posts.Difficulty.MEDIUM;
    private final List<PostIngredientDto.Request> DEFAULT_INGREDIENTS = Collections.emptyList();


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

        author = Member.builder()
                .email("author@example.com")
                .nickname("글쓴이")
                .password(passwordEncoder.encode("password123!"))
                .build();
        memberRepository.save(author);

        category = categoryRepository.save(Category.builder().name("자유게시판").type(Category.CategoryType.FREE).build());
        userToken = loginAndGetToken(author, "password123!");
    }

    @DisplayName("게시글 생성 요청에 성공하고 201 Created를 반환한다")
    @Test
    void createPost_Success() throws Exception {
        // given
        // (수정) 10개 인자 모두 전달
        PostDto.CreateRequest request = new PostDto.CreateRequest(
                category.getId(),
                "새 게시글 제목입니다",
                "새 게시글의 내용입니다. 10자 이상이어야 합니다.",
                Posts.PostStatus.PUBLISHED,
                false, // isRecipe
                DEFAULT_DIET_TYPE,
                DEFAULT_COOK_TIME,
                DEFAULT_SERVINGS,
                DEFAULT_DIFFICULTY,
                DEFAULT_INGREDIENTS
        );
        String requestBody = objectMapper.writeValueAsString(request);

        // when
        // (수정 없음) JSON 요청이 맞음
        ResultActions result = mockMvc.perform(post("/api/v1/posts")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        result.andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith("/api/posts/")));
    }

    @DisplayName("유효성 검증 실패 시 400 Bad Request를 반환한다")
    @Test
    void createPost_Fail_Validation() throws Exception {
        // given
        // (수정) 10개 인자 모두 전달, 제목 비움
        PostDto.CreateRequest request = new PostDto.CreateRequest(
                category.getId(),
                "", // 제목 비움
                "내용",
                Posts.PostStatus.PUBLISHED,
                false,
                DEFAULT_DIET_TYPE,
                DEFAULT_COOK_TIME,
                DEFAULT_SERVINGS,
                DEFAULT_DIFFICULTY,
                DEFAULT_INGREDIENTS
        );
        String requestBody = objectMapper.writeValueAsString(request);

        // when
        // (수정 없음) JSON 요청이 맞음
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
        ResultActions result = mockMvc.perform(get("/api/v1/posts/{id}", nonExistentId)
                .header("Authorization", "Bearer " + userToken));

        // then
        result.andExpect(status().isNotFound());
    }

    @DisplayName("게시글을 성공적으로 수정하고 200 OK를 반환한다")
    @Test
    void updatePost_Success() throws Exception {
        // given
        Posts originalPost = postsRepository.save(Posts.builder()
                .authorId(author)
                .category(category)
                .title("원본 제목입니다")
                .content("원본 내용입니다. 10자 이상입니다.")
                .status(Posts.PostStatus.PUBLISHED)
                .isRecipe(false)
                .build());
        Category newCategory = categoryRepository.save(Category.builder()
                .name("공지사항")
                .type(Category.CategoryType.QA)
                .build());

        // (수정) 10개 인자 모두 전달
        PostDto.UpdateRequest request = new PostDto.UpdateRequest(
                "수정된 제목입니다",
                "수정된 내용입니다. 10자 이상이어야 합니다.",
                newCategory.getId(),
                Posts.PostStatus.ARCHIVED,
                true, // isRecipe
                DEFAULT_DIET_TYPE,
                DEFAULT_COOK_TIME,
                DEFAULT_SERVINGS,
                DEFAULT_DIFFICULTY,
                DEFAULT_INGREDIENTS
        );
        String requestBody = objectMapper.writeValueAsString(request);

        // when
        // (수정 없음) JSON 요청이 맞음
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
        ResultActions result = mockMvc.perform(delete("/api/v1/posts/{id}", postToDelete.getId())
                .header("Authorization", "Bearer " + userToken));

        // then
        result.andExpect(status().isNoContent());
    }
}