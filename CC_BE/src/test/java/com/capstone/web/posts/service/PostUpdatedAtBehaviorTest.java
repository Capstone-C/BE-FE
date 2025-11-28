package com.capstone.web.posts.service;

import com.capstone.web.category.domain.Category;
import com.capstone.web.category.repository.CategoryRepository;
import com.capstone.web.member.domain.Member;
import com.capstone.web.member.repository.MemberRepository;
import com.capstone.web.posts.domain.Posts;
import com.capstone.web.posts.dto.PostDto;
import com.capstone.web.posts.repository.PostsRepository;
import com.capstone.web.common.S3UploadService;
import io.awspring.cloud.s3.S3Template;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.cloud.aws.s3.bucket=test-bucket")
@Import(PostUpdatedAtBehaviorTest.TestBeans.class)
@Transactional
class PostUpdatedAtBehaviorTest {

    @Autowired
    private PostService postService;
    @Autowired
    private PostsRepository postsRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    private Member author;
    private Category category;

    @BeforeEach
    void setup() {
        postsRepository.deleteAll();
        memberRepository.deleteAll();
        categoryRepository.deleteAll();
        author = memberRepository.save(Member.builder().email("author@example.com").nickname("작성자").password("pw").build());
        category = categoryRepository.save(Category.builder().name("테스트게시판").type(Category.CategoryType.FREE).build());
    }

    @DisplayName("조회(view)로 인한 viewCount 증가 시 updatedAt은 변경되지 않는다")
    @Test
    void viewDoesNotChangeUpdatedAt() {
        PostDto.CreateRequest createReq = new PostDto.CreateRequest();
        createReq.setCategoryId(category.getId());
        createReq.setTitle("제목");
        createReq.setContent("내용");
        createReq.setStatus(Posts.PostStatus.PUBLISHED);
        createReq.setIsRecipe(false);
        createReq.setThumbnailUrl(null);
        createReq.setDietType(Posts.DietType.GENERAL);
        createReq.setCookTimeInMinutes(10);
        createReq.setServings(1);
        createReq.setDifficulty(Posts.Difficulty.MEDIUM);
        createReq.setIngredients(null);

        Long postId = postService.createPost(author.getId(), createReq, null, null);
        Posts saved = postsRepository.findById(postId).orElseThrow();
        var originalUpdatedAt = saved.getUpdatedAt();

        // 조회 (viewCount 증가)
        postService.getPostById(postId);
        Posts afterView = postsRepository.findById(postId).orElseThrow();

        assertThat(afterView.getViewCount()).isEqualTo(1);
        assertThat(afterView.getUpdatedAt()).isEqualTo(originalUpdatedAt);
    }

    @DisplayName("본문/메타 수정 시 updatedAt은 변경된다")
    @Test
    void updateChangesUpdatedAt() throws InterruptedException {
        PostDto.CreateRequest createReq = new PostDto.CreateRequest();
        createReq.setCategoryId(category.getId());
        createReq.setTitle("제목");
        createReq.setContent("내용");
        createReq.setStatus(Posts.PostStatus.PUBLISHED);
        createReq.setIsRecipe(false);
        createReq.setThumbnailUrl(null);
        createReq.setDietType(Posts.DietType.GENERAL);
        createReq.setCookTimeInMinutes(10);
        createReq.setServings(1);
        createReq.setDifficulty(Posts.Difficulty.MEDIUM);
        createReq.setIngredients(null);

        Long postId = postService.createPost(author.getId(), createReq, null, null);
        Posts saved = postsRepository.findById(postId).orElseThrow();
        var beforeUpdate = saved.getUpdatedAt();

        Thread.sleep(20); // 시간 차 확보

        PostDto.UpdateRequest updateReq = new PostDto.UpdateRequest();
        updateReq.setTitle("새로운 제목");
        updateReq.setContent("새로운 내용");
        updateReq.setCategoryId(category.getId());
        updateReq.setStatus(Posts.PostStatus.PUBLISHED);
        updateReq.setIsRecipe(false);
        updateReq.setThumbnailUrl(null);
        updateReq.setDietType(Posts.DietType.GENERAL);
        updateReq.setCookTimeInMinutes(15);
        updateReq.setServings(2);
        updateReq.setDifficulty(Posts.Difficulty.MEDIUM);
        updateReq.setIngredients(null);

        postService.updatePost(postId, author.getId(), updateReq, null, null);
        Posts afterUpdate = postsRepository.findById(postId).orElseThrow();

        assertThat(afterUpdate.getUpdatedAt()).isAfter(beforeUpdate);
    }

    static class TestBeans {
        @Bean
        S3Template s3Template() {
            return mock(S3Template.class);
        }

        @Bean
        S3UploadService s3UploadService(S3Template s3Template) {
            return new S3UploadService(s3Template);
        }
    }
}
