package com.capstone.web.posts.service;

import com.capstone.web.category.domain.Category;
import com.capstone.web.category.exception.CategoryNotFoundException;
import com.capstone.web.category.repository.CategoryRepository;
import com.capstone.web.member.domain.Member;
import com.capstone.web.member.exception.UserNotFoundException;
import com.capstone.web.member.repository.MemberRepository;
import com.capstone.web.posts.domain.PostLike;
import com.capstone.web.posts.domain.Posts;
import com.capstone.web.posts.dto.PostDto;
import com.capstone.web.posts.dto.PostListRequest;
import com.capstone.web.posts.exception.PostNotFoundException;
import com.capstone.web.posts.exception.PostPermissionException; // (추가) 권한 예외 임포트
import com.capstone.web.posts.repository.PostsRepository;
import com.capstone.web.posts.repository.PostLikeRepository;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostsRepository postsRepository;
    private final CategoryRepository categoryRepository;
    private final MemberRepository memberRepository;
    private final PostLikeRepository postLikeRepository;

    private String sanitizeHtml(String html) {
        // 허용할 태그/속성만 화이트리스트로 지정 (기본: 단순 텍스트 + img/a 일부)
        Safelist safelist = Safelist.relaxed()
                .addTags("img")
                .addAttributes("img", "src", "alt", "title")
                .addAttributes("a", "href", "title", "target")
                .addProtocols("a", "href", "http", "https")
                .preserveRelativeLinks(true);
        return Jsoup.clean(html == null ? "" : html, safelist);
    }

    @Transactional
    public Long createPost(Long memberId, PostDto.CreateRequest request) { // (수정) memberId 받기
        Member author = memberRepository.findById(memberId)
                .orElseThrow(() -> new UserNotFoundException("작성자를 찾을 수 없습니다. ID: " + memberId));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException("카테고리를 찾을 수 없습니다. ID: " + request.getCategoryId()));

        Posts post = Posts.builder()
                .authorId(author)
                .category(category)
                .title(request.getTitle().trim())
                .content(sanitizeHtml(request.getContent()))
                .status(request.getStatus())
                .isRecipe(request.getIsRecipe())
                .build();

        Posts savedPost = postsRepository.save(post);
        return savedPost.getId();
    }

    @Transactional
    public PostDto.Response getPostById(Long id) {
        Posts post = postsNextPage(id);
        post.increaseViewCount();
        return new PostDto.Response(post);
    }

    @Transactional
    public PostDto.Response getPostById(Long id, Long viewerIdOrNull) {
        Posts post = postsNextPage(id);
        post.increaseViewCount();
        Boolean liked = null;
        if (viewerIdOrNull != null) {
            liked = postLikeRepository.findByPostIdAndMemberId(id, viewerIdOrNull).isPresent();
        }
        return new PostDto.Response(post, liked);
    }

    private Posts postsNextPage(Long id) {
        return postsRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException("게시글을 찾을 수 없습니다. ID: " + id));
    }

    // N+1 문제 해결을 위해 findAllWithAuthor() 사용 (PostsRepository에 @Query 필요)
    public List<PostDto.Response> getAllPosts() {
        return postsRepository.findAllWithAuthor().stream()
                .map(PostDto.Response::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updatePost(Long id, Long memberId, PostDto.UpdateRequest request) { // (수정) memberId 받기
        Posts post = postsNextPage(id);

        if (!post.getAuthorId().getId().equals(memberId)) {
            throw new PostPermissionException("게시글을 수정할 권한이 없습니다.");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException("카테고리를 찾을 수 없습니다. ID: " + request.getCategoryId()));

        post.update(request.getTitle().trim(), sanitizeHtml(request.getContent()), request.getStatus(), category, request.getIsRecipe());
    }

    @Transactional
    public void deletePost(Long id, Long memberId) { // (수정) memberId 받기
        Posts post = postsNextPage(id);

        if (!post.getAuthorId().getId().equals(memberId)) {
            throw new PostPermissionException("게시글을 삭제할 권한이 없습니다.");
        }

        postsRepository.delete(post);
    }

    public Page<PostDto.Response> list(PostListRequest req) {
        Pageable pageable = PageRequest.of(req.pageIndex(), req.getSize(), req.sort());

        Specification<Posts> spec = Specification.where(null);
        if (req.getBoardId() != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("category").get("id"), req.getBoardId()));
        }
        if (req.getAuthorId() != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("authorId").get("id"), req.getAuthorId()));
        }
        if (req.getSearchType() != null && req.getKeyword() != null && !req.getKeyword().isBlank()) {
            String kw = "%" + req.getKeyword().trim() + "%";
            switch (req.getSearchType().toUpperCase()) {
                case "TITLE":
                    spec = spec.and((root, q, cb) -> cb.like(root.get("title"), kw));
                    break;
                case "CONTENT":
                    spec = spec.and((root, q, cb) -> cb.like(root.get("content"), kw));
                    break;
                case "AUTHOR":
                    spec = spec.and((root, q, cb) -> cb.like(root.get("authorId").get("nickname"), kw));
                    break;
                default:
            }
        }

        Page<Posts> page = postsRepository.findAll(spec, pageable);
        List<PostDto.Response> content = page.getContent().stream().map(PostDto.Response::new).collect(Collectors.toList());
        return new PageImpl<>(content, pageable, page.getTotalElements());
    }

    @Transactional
    public ToggleLikeResult toggleLike(Long postId, Long memberId) {
        Posts post = postsNextPage(postId);
        boolean liked;
        var existing = postLikeRepository.findByPostIdAndMemberId(postId, memberId);
        if (existing.isPresent()) {
            // unlike
            postLikeRepository.delete(existing.get());
            post.decreaseLikeCount();
            liked = false;
        } else {
            // like
            var member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. ID: " + memberId));
            postLikeRepository.save(new PostLike(post, member));
            post.increaseLikeCount();
            liked = true;
        }
        return new ToggleLikeResult(liked, post.getLikeCount());
    }

    public record ToggleLikeResult(boolean liked, int likeCount) {
    }
}
