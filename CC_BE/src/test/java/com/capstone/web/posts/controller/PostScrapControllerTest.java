package com.capstone.web.posts.controller;

import com.capstone.web.auth.dto.LoginRequest;
import com.capstone.web.category.domain.Category;
import com.capstone.web.category.repository.CategoryRepository;
import com.capstone.web.common.S3UploadService;
import com.capstone.web.member.domain.Member;
import com.capstone.web.member.repository.MemberRepository;
import com.capstone.web.posts.domain.PostScrap;
import com.capstone.web.posts.domain.Posts;
import com.capstone.web.posts.repository.PostScrapRepository;
import com.capstone.web.posts.repository.PostsRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PostScrapControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private MemberRepository memberRepository;
    @Autowired private PostsRepository postsRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private PostScrapRepository postScrapRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @MockBean private S3UploadService s3UploadService; // S3 에러 방지

    private Member member;
    private Posts post;
    private String userToken;

    // 로그인 헬퍼
    private String loginAndGetToken(Member member, String rawPassword) throws Exception {
        LoginRequest req = new LoginRequest(member.getEmail(), rawPassword);
        String body = objectMapper.writeValueAsString(req);
        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("accessToken").asText();
    }

    @BeforeEach
    void setup() throws Exception {
        postScrapRepository.deleteAll();
        postsRepository.deleteAll();
        memberRepository.deleteAll();
        categoryRepository.deleteAll();

        member = memberRepository.save(Member.builder().email("user@example.com").nickname("유저").password(passwordEncoder.encode("pw")).build());
        Category category = categoryRepository.save(Category.builder().name("레시피").type(Category.CategoryType.RECIPE).build());

        post = postsRepository.save(Posts.builder()
                .authorId(member)
                .category(category)
                .title("스크랩할 게시글")
                .content("내용")
                .status(Posts.PostStatus.PUBLISHED)
                .isRecipe(true)
                .build());

        userToken = loginAndGetToken(member, "pw");
    }

    @DisplayName("스크랩 토글 - 저장 성공")
    @Test
    void toggleScrap_Save() throws Exception {
        mockMvc.perform(post("/api/v1/posts/{postId}/scrap", post.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scrapped", is(true)));
    }

    @DisplayName("스크랩 토글 - 취소 성공")
    @Test
    void toggleScrap_Cancel() throws Exception {
        // 미리 스크랩 해둠
        postScrapRepository.save(new PostScrap(member, post));

        mockMvc.perform(post("/api/v1/posts/{postId}/scrap", post.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scrapped", is(false)));
    }

    @DisplayName("내 스크랩 목록 조회 성공")
    @Test
    void getMyScraps_Success() throws Exception {
        // 스크랩 데이터 1개 생성
        postScrapRepository.save(new PostScrap(member, post));

        mockMvc.perform(get("/api/v1/users/me/scraps")
                        .header("Authorization", "Bearer " + userToken)
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title", is("스크랩할 게시글")))
                .andExpect(jsonPath("$.totalElements", is(1)));
    }
}