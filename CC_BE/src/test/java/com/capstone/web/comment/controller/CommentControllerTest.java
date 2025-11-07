package com.capstone.web.comment.controller;

import com.capstone.web.auth.dto.LoginRequest;
import com.capstone.web.category.domain.Category;
import com.capstone.web.category.repository.CategoryRepository;
import com.capstone.web.comment.domain.Comment;
import com.capstone.web.comment.dto.CommentDto;
import com.capstone.web.comment.repository.CommentRepository;
import com.capstone.web.member.domain.Member;
import com.capstone.web.member.repository.MemberRepository;
import com.capstone.web.posts.domain.Posts;
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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CommentControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private CommentRepository commentRepository;
    @Autowired private PostsRepository postsRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private Member author;
    private Posts post;
    private String userToken;

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
        commentRepository.deleteAll();
        postsRepository.deleteAll();
        memberRepository.deleteAll();
        categoryRepository.deleteAll();

        author = memberRepository.save(Member.builder()
                .email("user@example.com")
                .password(passwordEncoder.encode("password123!"))
                .nickname("테스터")
                .build());

        Category category = categoryRepository.save(Category.builder().name("자유").type(Category.CategoryType.FREE).build());
        post = postsRepository.save(Posts.builder().authorId(author).category(category).title("글").content("내용").build());

        userToken = loginAndGetToken(author, "password123!");
    }

    @DisplayName("댓글 생성 API 호출에 성공한다")
    @Test
    void createComment_ApiSuccess() throws Exception {
        // given
        CommentDto.CreateRequest request = new CommentDto.CreateRequest(null, "API 테스트 댓글");
        String requestBody = objectMapper.writeValueAsString(request);

        // when
        ResultActions result = mockMvc.perform(post("/api/v1/posts/{postId}/comments", post.getId())
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

        // then
        result.andExpect(status().isCreated());
    }

    @DisplayName("댓글 목록 조회 API 호출 시 계층 구조로 응답한다")
    @Test
    void getComments_ApiSuccess() throws Exception {
        // given
        Comment parent = commentRepository.save(Comment.builder().post(post).author(author).content("부모").build());
        commentRepository.save(Comment.builder().post(post).author(author).content("자식").parent(parent).build());

        // when
        ResultActions result = mockMvc.perform(get("/api/v1/posts/{postId}/comments", post.getId())
                .header("Authorization", "Bearer " + userToken));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1))) // 최상위 댓글 1개
                .andExpect(jsonPath("$[0].content", is("부모")))
                .andExpect(jsonPath("$[0].children", hasSize(1))) // 자식 댓글 1개
                .andExpect(jsonPath("$[0].children[0].content", is("자식")));
    }

    @DisplayName("댓글 삭제 API 호출에 성공한다")
    @Test
    void deleteComment_ApiSuccess() throws Exception {
        // given
        Comment comment = commentRepository.save(Comment.builder().post(post).author(author).content("삭제될 댓글").build());

        // when
        ResultActions result = mockMvc.perform(delete("/api/v1/posts/{postId}/comments/{commentId}", post.getId(), comment.getId())
                .header("Authorization", "Bearer " + userToken));

        // then
        result.andExpect(status().isNoContent());
    }

}