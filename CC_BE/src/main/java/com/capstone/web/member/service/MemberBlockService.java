package com.capstone.web.member.service;

import com.capstone.web.member.domain.Member;
import com.capstone.web.member.domain.MemberBlock;
import com.capstone.web.member.dto.BlockListResponse;
import com.capstone.web.member.dto.BlockRequest;
import com.capstone.web.member.exception.MemberBlockErrorCode;
import com.capstone.web.member.exception.MemberBlockException;
import com.capstone.web.member.repository.MemberBlockRepository;
import com.capstone.web.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberBlockService {
    private final MemberRepository memberRepository;
    private final MemberBlockRepository memberBlockRepository;

    public void block(Long blockerId, BlockRequest request) {
        if (blockerId.equals(request.blockedId())) {
            throw new MemberBlockException(MemberBlockErrorCode.SELF_BLOCK);
        }
        Member blocker = memberRepository.findById(blockerId)
                .orElseThrow(() -> new MemberBlockException(MemberBlockErrorCode.MEMBER_NOT_FOUND));
        Member blocked = memberRepository.findById(request.blockedId())
                .orElseThrow(() -> new MemberBlockException(MemberBlockErrorCode.MEMBER_NOT_FOUND));
        if (memberBlockRepository.existsByBlockerAndBlocked(blocker, blocked)) {
            throw new MemberBlockException(MemberBlockErrorCode.DUPLICATE_BLOCK);
        }
        MemberBlock entity = MemberBlock.builder()
                .blocker(blocker)
                .blocked(blocked)
                .build();
        memberBlockRepository.save(entity);
    }

    public void unblock(Long blockerId, Long blockedId) {
        Member blocker = memberRepository.findById(blockerId)
                .orElseThrow(() -> new MemberBlockException(MemberBlockErrorCode.MEMBER_NOT_FOUND));
        Member blocked = memberRepository.findById(blockedId)
                .orElseThrow(() -> new MemberBlockException(MemberBlockErrorCode.MEMBER_NOT_FOUND));
        MemberBlock existing = memberBlockRepository.findByBlockerAndBlocked(blocker, blocked)
                .orElseThrow(() -> new MemberBlockException(MemberBlockErrorCode.NOT_BLOCKED));
        memberBlockRepository.delete(existing);
    }

    @Transactional(readOnly = true)
    public List<BlockListResponse> list(Long blockerId) {
        Member blocker = memberRepository.findById(blockerId)
                .orElseThrow(() -> new MemberBlockException(MemberBlockErrorCode.MEMBER_NOT_FOUND));
        return memberBlockRepository.findAllByBlockerOrderByCreatedAtDesc(blocker).stream()
                .map(b -> new BlockListResponse(
                        b.getId(),
                        b.getBlocked().getId(),
                        b.getBlocked().getEmail(),
                        b.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }
}
