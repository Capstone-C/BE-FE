package com.capstone.web.posts.service;

import com.capstone.web.category.domain.Category;
import com.capstone.web.category.exception.CategoryNotFoundException;
import com.capstone.web.category.repository.CategoryRepository;
import com.capstone.web.common.S3UploadService;
import com.capstone.web.media.domain.Media;
import java.util.Optional;
import com.capstone.web.media.repository.MediaRepository;
import com.capstone.web.member.domain.Member;
import com.capstone.web.member.exception.UserNotFoundException;
import com.capstone.web.member.repository.MemberRepository;
import com.capstone.web.posts.domain.PostLike;
import com.capstone.web.posts.domain.Posts;
import com.capstone.web.posts.domain.PostIngredient;
import com.capstone.web.posts.dto.PostDto;
import com.capstone.web.posts.dto.PostListRequest;
import com.capstone.web.posts.dto.PostIngredientDto;
import com.capstone.web.posts.dto.PostComparisonDto;
import com.capstone.web.posts.exception.PostNotFoundException;
import com.capstone.web.posts.exception.PostPermissionException;
import com.capstone.web.posts.repository.PostLikeRepository;
import com.capstone.web.posts.repository.PostsRepository;
import com.capstone.web.posts.repository.PostIngredientRepository;
import com.capstone.web.refrigerator.domain.RefrigeratorItem;
import com.capstone.web.refrigerator.repository.RefrigeratorItemRepository;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
    private final RefrigeratorItemRepository refrigeratorItemRepository;
    private final Optional<S3UploadService> s3UploadService;
    private final MediaRepository mediaRepository;

    private String sanitizeHtml(String html) {
        Safelist safelist = Safelist.relaxed()
                .addTags("img")
                .addAttributes("img", "src", "alt", "title")
                .addAttributes("a", "href", "title", "target")
                .addProtocols("a", "href", "http", "https")
                .preserveRelativeLinks(true);
        return Jsoup.clean(html == null ? "" : html, safelist);
    }

    // (수정) MultipartFile 리스트 추가
    @Transactional
    public Long createPost(Long memberId, PostDto.CreateRequest request, MultipartFile thumbnailFile, List<MultipartFile> files) {
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

        // [수정] 1. 썸네일 처리 (파일 업로드 우선, 없으면 URL 사용)
        if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
            if (s3UploadService.isPresent()) {
                try {
                    String thumbnailUrl = s3UploadService.get().uploadFile(thumbnailFile);
                    addThumbnailMedia(post, thumbnailUrl);
                } catch (IOException e) {
                    throw new RuntimeException("썸네일 업로드 실패", e);
                }
            } else {
                throw new RuntimeException("S3 서비스가 설정되지 않았습니다.");
            }
        } else if (request.getThumbnailUrl() != null && !request.getThumbnailUrl().isBlank()) {
            // [추가] 프론트엔드에서 보낸 URL로 썸네일 설정
            addThumbnailMedia(post, request.getThumbnailUrl());
        }

        // 2. 추가 이미지 업로드 (OrderNum = 1 부터 시작)
        if (files != null && !files.isEmpty()) {
            if (s3UploadService.isPresent()) {
                int orderNum = 1;
                for (MultipartFile file : files) {
                    if (file.isEmpty()) continue;
                    try {
                        String fileUrl = s3UploadService.get().uploadFile(file);
                        Media media = Media.builder()
                                .ownerType(Media.OwnerType.post)
                                .mediaType(Media.MediaType.image)
                                .url(fileUrl)
                                .orderNum(orderNum++)
                                .build();
                        post.addMedia(media);
                    } catch (IOException e) {
                        throw new RuntimeException("추가 이미지 업로드 실패", e);
                    }
                }
            } else {
                throw new RuntimeException("S3 서비스가 설정되지 않았습니다.");
            }
        }

        Posts savedPost = postsRepository.save(post);

        // 3. 재료 저장
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

    // [추가] 썸네일 Media 생성 헬퍼 메서드
    private void addThumbnailMedia(Posts post, String url) {
        Media thumbnailMedia = Media.builder()
                .ownerType(Media.OwnerType.post)
                .mediaType(Media.MediaType.image)
                .url(url)
                .orderNum(0) // 썸네일은 0번
                .build();
        post.addMedia(thumbnailMedia);
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

    // (수정) MultipartFile 리스트 추가
    @Transactional
    public void updatePost(Long id, Long memberId, PostDto.UpdateRequest request, MultipartFile thumbnailFile, List<MultipartFile> files) {
        Posts post = postsNextPage(id);

        if (!post.getAuthorId().getId().equals(memberId)) {
            throw new PostPermissionException("게시글을 수정할 권한이 없습니다.");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException("카테고리를 찾을 수 없습니다. ID: " + request.getCategoryId()));

        // [수정] 1. 썸네일 업데이트
        if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
            // 새 파일이 있으면 기존 썸네일 제거 후 새 파일 업로드
            if (s3UploadService.isPresent()) {
                post.getMedia().removeIf(m -> m.getOwnerType() == Media.OwnerType.post && m.getOrderNum() == 0);
                try {
                    String newThumbnailUrl = s3UploadService.get().uploadFile(thumbnailFile);
                    addThumbnailMedia(post, newThumbnailUrl);
                } catch (IOException e) {
                    throw new RuntimeException("썸네일 업로드 실패", e);
                }
            } else {
                throw new RuntimeException("S3 서비스가 설정되지 않았습니다.");
            }
        } else if (request.getThumbnailUrl() != null) {
            // [추가] URL이 전달된 경우 (빈 문자열이면 삭제, 값이 있으면 업데이트)
            // 기존 썸네일 제거
            post.getMedia().removeIf(m -> m.getOwnerType() == Media.OwnerType.post && m.getOrderNum() == 0);

            if (!request.getThumbnailUrl().isBlank()) {
                addThumbnailMedia(post, request.getThumbnailUrl());
            }
        }

        // 2. 추가 이미지 업로드 (기존 이미지 유지 후 Append)
        if (files != null && !files.isEmpty()) {
            if (s3UploadService.isPresent()) {
                // 현재 가장 큰 orderNum 찾기
                int maxOrderNum = post.getMedia().stream()
                        .filter(m -> m.getOwnerType() == Media.OwnerType.post)
                        .mapToInt(Media::getOrderNum)
                        .max()
                        .orElse(0); // 없으면 0 (썸네일만 있거나 아무것도 없을 때)

                int startOrder = maxOrderNum + 1;

                for (MultipartFile file : files) {
                    if (file.isEmpty()) continue;
                    try {
                        String fileUrl = s3UploadService.get().uploadFile(file);
                        Media media = Media.builder()
                                .ownerType(Media.OwnerType.post)
                                .mediaType(Media.MediaType.image)
                                .url(fileUrl)
                                .orderNum(startOrder++)
                                .build();
                        post.addMedia(media);
                    } catch (IOException e) {
                        throw new RuntimeException("추가 이미지 업로드 실패", e);
                    }
                }
            } else {
                throw new RuntimeException("S3 서비스가 설정되지 않았습니다.");
            }
        }

        // 3. 재료 업데이트
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

        // 4. Post 정보 업데이트
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

        if (req.getBoardId() != null) spec = spec.and((root, q, cb) -> cb.equal(root.get("category").get("id"), req.getBoardId()));
        if (req.getAuthorId() != null) spec = spec.and((root, q, cb) -> cb.equal(root.get("authorId").get("id"), req.getAuthorId()));
        if (req.getSearchType() != null && req.getKeyword() != null && !req.getKeyword().isBlank()) {
            String kw = "%" + req.getKeyword().trim() + "%";
            switch (req.getSearchType().toUpperCase()) {
                case "TITLE": spec = spec.and((root, q, cb) -> cb.like(root.get("title"), kw)); break;
                case "CONTENT": spec = spec.and((root, q, cb) -> cb.like(root.get("content"), kw)); break;
                case "AUTHOR": spec = spec.and((root, q, cb) -> cb.like(root.get("authorId").get("nickname"), kw)); break;
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

    public record ToggleLikeResult(boolean liked, int likeCount) {}

    public PostComparisonDto.Response compareWithRefrigerator(Long postId, Long memberId) {
        Posts post = postsNextPage(postId);
        List<PostIngredient> requiredIngredients = post.getIngredients();
        List<RefrigeratorItem> myItems = refrigeratorItemRepository.findByMemberId(memberId);
        List<String> myItemNames = myItems.stream().map(item -> item.getName().toLowerCase().trim()).collect(Collectors.toList());

        int ownedCount = 0;
        List<PostComparisonDto.ComparedIngredient> comparedList = new ArrayList<>();

        for (PostIngredient required : requiredIngredients) {
            String requiredName = required.getName().toLowerCase().trim();
            boolean isOwned = myItemNames.stream().anyMatch(ownedName -> ownedName.contains(requiredName) || requiredName.contains(ownedName));
            String amount = (required.getUnit() != null && !required.getUnit().isBlank()) ? required.getQuantity() + required.getUnit() : String.valueOf(required.getQuantity());

            if (isOwned) {
                ownedCount++;
                comparedList.add(PostComparisonDto.ComparedIngredient.builder().name(required.getName()).amount(amount).status(PostComparisonDto.ComparisonStatus.OWNED).build());
            } else {
                comparedList.add(PostComparisonDto.ComparedIngredient.builder().name(required.getName()).amount(amount).status(PostComparisonDto.ComparisonStatus.MISSING).build());
            }
        }

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