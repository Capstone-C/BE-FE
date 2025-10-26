package com.capstone.web.comment.service;

import com.capstone.web.comment.domain.Comment;
import com.capstone.web.comment.dto.CommentDto;
import com.capstone.web.comment.dto.CommentQueryDto;
import com.capstone.web.comment.exception.CommentNotFoundException;
import com.capstone.web.comment.exception.CommentPermissionException;
import com.capstone.web.comment.repository.CommentRepository;
import com.capstone.web.member.domain.Member;
import com.capstone.web.member.exception.UserNotFoundException;
import com.capstone.web.member.repository.MemberRepository;
import com.capstone.web.posts.domain.Posts;
import com.capstone.web.posts.exception.PostNotFoundException;
import com.capstone.web.posts.repository.PostsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;
    private final PostsRepository postsRepository;

    @Transactional
    public Long createComment(Long postId, Long memberId, CommentDto.CreateRequest request) {
        Member author = memberRepository.findById(memberId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));
        Posts post = postsRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("게시글을 찾을 수 없습니다."));

        Comment parent = null;
        int depth = 0;
        if (request.getParentId() != null) {
            parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new CommentNotFoundException("부모 댓글을 찾을 수 없습니다."));
            depth = parent.getDepth() + 1;
        }

        Comment comment = Comment.builder()
                .post(post)
                .author(author)
                .content(request.getContent())
                .parent(parent)
                .depth(depth)
                .build();

        return commentRepository.save(comment).getId();
    }

    public List<CommentDto.Response> getCommentsByPost(Long postId) {
        List<Comment> comments = commentRepository.findAllByPostId(postId); // 1. 모든 댓글을 DB에서 가져옴
        List<CommentDto.Response> commentResponses = new ArrayList<>();
        Map<Long, CommentDto.Response> map = new HashMap<>();

        comments.forEach(c -> {
            CommentDto.Response dto = new CommentDto.Response(c); // 2. 각 댓글을 DTO로 변환
            map.put(dto.getId(), dto);

            if (c.getParent() != null) { // 3. 부모가 있는 댓글(대댓글)이라면
                // 4. 맵에서 부모 DTO를 찾아, 그 부모의 children 리스트에 현재 DTO를 추가
                map.get(c.getParent().getId()).getChildren().add(dto);
            } else { // 5. 부모가 없는 댓글(최상위 댓글)이라면
                // 6. 최종 결과 리스트에 바로 추가
                commentResponses.add(dto);
            }
        });
        return commentResponses; // 7. 최상위 댓글 목록만 반환 (자식들은 부모 안에 포함됨)
    }

    @Transactional
    public void updateComment(Long commentId, Long memberId, CommentDto.UpdateRequest request) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("댓글을 찾을 수 없습니다."));

        if (!comment.getAuthor().getId().equals(memberId)) {
            throw new CommentPermissionException("댓글을 수정할 권한이 없습니다.");
        }

        comment.updateContent(request.getContent());
    }

    @Transactional
    public void deleteComment(Long commentId, Long memberId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("댓글을 찾을 수 없습니다."));

        if (!comment.getAuthor().getId().equals(memberId)) {
            throw new CommentPermissionException("댓글을 삭제할 권한이 없습니다.");
        }

        // 자식 댓글이 있는 경우 내용만 변경 (소프트 삭제), 없는 경우 DB에서 완전히 삭제
        if (comment.getChildren().isEmpty()) {
            commentRepository.delete(comment);
        } else {
            comment.softDelete();
        }
    }

    public List<CommentQueryDto> getCommentsByAuthor(Long authorId) {
        // Member가 존재하는지 먼저 확인 (안전 장치)
        if (!memberRepository.existsById(authorId)) {
            throw new UserNotFoundException("사용자를 찾을 수 없습니다. ID: " + authorId);
        }

        List<Comment> comments = commentRepository.findAllByAuthorId(authorId);

        return comments.stream()
                .map(CommentQueryDto::new)
                .collect(Collectors.toList());
    }
}