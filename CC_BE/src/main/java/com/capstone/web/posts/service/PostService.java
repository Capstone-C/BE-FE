package com.capstone.web.posts.service;

import com.capstone.web.category.domain.Category;
import com.capstone.web.category.exception.CategoryNotFoundException;
import com.capstone.web.category.repository.CategoryRepository;
import com.capstone.web.member.domain.Member;
import com.capstone.web.member.exception.UserNotFoundException;
import com.capstone.web.member.repository.MemberRepository;
import com.capstone.web.posts.domain.PostLike;
import com.capstone.web.posts.domain.Posts;
import com.capstone.web.posts.domain.PostIngredient;
import com.capstone.web.posts.dto.PostDto;
import com.capstone.web.posts.dto.PostListRequest;
import com.capstone.web.posts.dto.PostIngredientDto;
import com.capstone.web.posts.dto.PostComparisonDto; // (추가)
import com.capstone.web.posts.exception.PostNotFoundException;
import com.capstone.web.posts.exception.PostPermissionException;
import com.capstone.web.posts.repository.PostLikeRepository;
import com.capstone.web.posts.repository.PostsRepository;
import com.capstone.web.posts.repository.PostIngredientRepository;
import com.capstone.web.refrigerator.domain.RefrigeratorItem; // (추가)
import com.capstone.web.refrigerator.repository.RefrigeratorItemRepository; // (추가)
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

import java.util.ArrayList; // (추가)
import java.util.Collections;
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
    private final PostIngredientRepository ingredientRepository;
    private final RefrigeratorItemRepository refrigeratorItemRepository; // (추가)

    private String sanitizeHtml(String html) {
        Safelist safelist = Safelist.relaxed()
                .addTags("img")
                .addAttributes("img", "src", "alt", "title")
                .addAttributes("a", "href", "title", "target")
                .addProtocols("a", "href", "http", "https")
                .preserveRelativeLinks(true);
        return Jsoup.clean(html == null ? "" : html, safelist);
    }

    @Transactional
    public Long createPost(Long memberId, PostDto.CreateRequest request) {
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
                .dietType(request.getDietType())
                .cookTimeInMinutes(request.getCookTimeInMinutes())
                .servings(request.getServings())
                .difficulty(request.getDifficulty())
                .build();

        Posts savedPost = postsRepository.save(post);

        if (request.getIsRecipe() && request.getIngredients() != null) {
            List<PostIngredient> ingredients = request.getIngredients().stream()
                    .map(dto -> PostIngredient.builder()
                            .post(savedPost)
                            .name(dto.getName())
                            .quantity(dto.getQuantity())
                            .unit(dto.getUnit())
                            .memo(dto.getMemo())
                            .expirationDate(dto.getExpirationDate())
                            .build())
                    .collect(Collectors.toList());

            ingredientRepository.saveAll(ingredients);
        }

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
        // (수정) @EntityGraph가 적용된 findById를 호출하여 N+1 문제 해결
        return postsRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException("게시글을 찾을 수 없습니다. ID: " + id));
    }

    @Transactional
    public void updatePost(Long id, Long memberId, PostDto.UpdateRequest request) {
        Posts post = postsNextPage(id);

        if (!post.getAuthorId().getId().equals(memberId)) {
            throw new PostPermissionException("게시글을 수정할 권한이 없습니다.");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException("카테고리를 찾을 수 없습니다. ID: " + request.getCategoryId()));

        post.getIngredients().clear();

        List<PostIngredient> newIngredients;
        if (request.getIsRecipe() && request.getIngredients() != null) {
            newIngredients = request.getIngredients().stream()
                    .map(dto -> PostIngredient.builder()
                            .post(post)
                            .name(dto.getName())
                            .quantity(dto.getQuantity())
                            .unit(dto.getUnit())
                            .memo(dto.getMemo())
                            .expirationDate(dto.getExpirationDate())
                            .build())
                    .collect(Collectors.toList());
        } else {
            newIngredients = Collections.emptyList();
        }

        post.update(
                request.getTitle().trim(),
                sanitizeHtml(request.getContent()),
                request.getStatus(),
                category,
                request.getIsRecipe(),
                request.getDietType(),
                request.getCookTimeInMinutes(),
                request.getServings(),
                request.getDifficulty()
        );

        post.getIngredients().addAll(newIngredients);
    }

    @Transactional
    public void deletePost(Long id, Long memberId) {
        Posts post = postsNextPage(id);

        if (!post.getAuthorId().getId().equals(memberId)) {
            throw new PostPermissionException("게시글을 삭제할 권한이 없습니다.");
        }

        postsRepository.delete(post);
    }

    public Page<PostDto.Response> list(PostListRequest req) {
        Pageable pageable = PageRequest.of(req.pageIndex(), req.getSize(), req.sort());

        Specification<Posts> spec = Specification.anyOf();
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
            postLikeRepository.delete(existing.get());
            post.decreaseLikeCount();
            liked = false;
        } else {
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

    // --- (신규) 내 냉장고와 재료 비교 ---
    public PostComparisonDto.Response compareWithRefrigerator(Long postId, Long memberId) {

        // 1. (필요 재료) 게시글의 재료 목록 조회
        //    (postsNextPage가 @EntityGraph 적용된 findById를 호출하므로 N+1 해결됨)
        Posts post = postsNextPage(postId);
        List<PostIngredient> requiredIngredients = post.getIngredients();

        // 2. (보유 재료) 내 냉장고 재료 목록 조회
        List<RefrigeratorItem> myItems = refrigeratorItemRepository.findByMemberId(memberId);
        List<String> myItemNames = myItems.stream()
                .map(item -> item.getName().toLowerCase().trim())
                .collect(Collectors.toList());

        int ownedCount = 0;
        List<PostComparisonDto.ComparedIngredient> comparedList = new ArrayList<>();

        // 3. (비교) 필요 재료 목록을 기준으로, 내 냉장고에 있는지 확인
        for (PostIngredient required : requiredIngredients) {
            String requiredName = required.getName().toLowerCase().trim();

            // RefrigeratorService의 퍼지 매칭(부분 일치) 로직과 유사하게 구현
            boolean isOwned = myItemNames.stream()
                    .anyMatch(ownedName -> ownedName.contains(requiredName) || requiredName.contains(ownedName));

            // 재료 양(amount) 포맷팅
            String amount = (required.getUnit() != null && !required.getUnit().isBlank())
                    ? required.getQuantity() + required.getUnit()
                    : String.valueOf(required.getQuantity());

            if (isOwned) {
                ownedCount++;
                comparedList.add(PostComparisonDto.ComparedIngredient.builder()
                        .name(required.getName())
                        .amount(amount)
                        .status(PostComparisonDto.ComparisonStatus.OWNED)
                        .build());
            } else {
                comparedList.add(PostComparisonDto.ComparedIngredient.builder()
                        .name(required.getName())
                        .amount(amount)
                        .status(PostComparisonDto.ComparisonStatus.MISSING)
                        .build());
            }
        }

        // 4. (응답)
        return PostComparisonDto.Response.builder()
                .postId(post.getId())
                .postTitle(post.getTitle())
                .ingredients(comparedList)
                .totalNeeded(requiredIngredients.size())
                .ownedCount(ownedCount)
                .missingCount(requiredIngredients.size() - ownedCount)
                .build();
    }
}