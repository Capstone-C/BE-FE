package com.capstone.web.posts.service;

import com.capstone.web.category.domain.Category;
import com.capstone.web.category.repository.CategoryRepository;
import com.capstone.web.common.S3UploadService;
import com.capstone.web.media.repository.MediaRepository;
import com.capstone.web.member.domain.Member;
import com.capstone.web.member.repository.MemberRepository;
import com.capstone.web.posts.domain.Posts;
import com.capstone.web.posts.dto.PostDto;
import com.capstone.web.posts.repository.PostsRepository;
import com.capstone.web.refrigerator.repository.RefrigeratorItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
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

    // [수정] 복잡한 TestBeans 대신 MockBean 사용
    @MockitoBean
    private S3UploadService s3UploadService;
    @MockitoBean private MediaRepository mediaRepository;
    @MockitoBean private RefrigeratorItemRepository refrigeratorItemRepository;

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
        // given
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

        // when: 조회 (viewCount 증가)
        postService.getPostById(postId);

        // then
        Posts afterView = postsRepository.findById(postId).orElseThrow();
        assertThat(afterView.getViewCount()).isEqualTo(1);
        assertThat(afterView.getUpdatedAt()).isEqualTo(originalUpdatedAt);
    }

    @DisplayName("본문/메타 수정 시 updatedAt은 변경된다")
    @Test
    void updateChangesUpdatedAt() throws InterruptedException {
        // given
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

        Thread.sleep(20); // 시간 차 확보 (DB 타임스탬프 정밀도 고려)

        // when: 수정
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

        // then
        Posts afterUpdate = postsRepository.findById(postId).orElseThrow();
        assertThat(afterUpdate.getUpdatedAt()).isAfter(beforeUpdate);
    }
}