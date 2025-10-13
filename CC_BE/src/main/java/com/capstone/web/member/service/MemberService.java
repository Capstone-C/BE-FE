package com.capstone.web.member.service;


import com.capstone.web.member.domain.Member;
import com.capstone.web.member.dto.MemberRegisterRequest;
import com.capstone.web.member.dto.MemberRegisterResponse;
import com.capstone.web.member.dto.MemberPasswordChangeRequest;
import com.capstone.web.member.exception.InvalidOldPasswordException;
import com.capstone.web.member.exception.SameAsOldPasswordException;
import com.capstone.web.member.exception.RecentPasswordReuseException;
import com.capstone.web.member.exception.DuplicateEmailException;
import com.capstone.web.member.exception.DuplicateNicknameException;
import com.capstone.web.member.repository.MemberRepository;
import com.capstone.web.member.repository.MemberPasswordHistoryRepository;
import com.capstone.web.member.domain.MemberPasswordHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberPasswordHistoryRepository memberPasswordHistoryRepository;
    private final PasswordEncoder passwordEncoder;

    public void changePassword(Long memberId, MemberPasswordChangeRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // 기존 비밀번호 불일치
        if (!passwordEncoder.matches(request.oldPassword(), member.getPassword())) {
            throw new InvalidOldPasswordException();
        }
        // 새 비밀번호가 기존과 동일
        if (passwordEncoder.matches(request.newPassword(), member.getPassword())) {
            throw new SameAsOldPasswordException();
        }

        // 최근 5회 이내 사용 비밀번호 재사용 금지
        var recentHistories = memberPasswordHistoryRepository.findTop5ByMemberOrderByChangedAtDesc(member);
        for (MemberPasswordHistory history : recentHistories) {
            if (passwordEncoder.matches(request.newPassword(), history.getPassword())) {
                throw new RecentPasswordReuseException();
            }
        }

        // 비밀번호 변경
        String encodedNewPassword = passwordEncoder.encode(request.newPassword());
        member.changePassword(encodedNewPassword);

        // 비밀번호 변경 이력 저장
        MemberPasswordHistory history = MemberPasswordHistory.builder()
                .member(member)
                .password(encodedNewPassword)
                .changedAt(java.time.LocalDateTime.now())
                .build();
        memberPasswordHistoryRepository.save(history);

        // pruning: 회원별 5개 초과 시 가장 오래된 이력 제거 (간단 구현)
        // NOTE: 효율적 쿼리를 위해선 countByMember + findIdsForPruning 등의 커스텀 쿼리 고려
        // pruning 실제 구현: 회원별 5개 유지
        long historyCount = memberPasswordHistoryRepository.countByMember(member);
        if (historyCount > 5) {
            var histories = memberPasswordHistoryRepository.findByMemberOrderByChangedAtDesc(member); // 최신 내림차순
            for (int i = 5; i < histories.size(); i++) {
                memberPasswordHistoryRepository.delete(histories.get(i));
            }
        }
    }

    public MemberRegisterResponse register(MemberRegisterRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException();
        }
        if (memberRepository.existsByNickname(request.nickname())) {
            throw new DuplicateNicknameException();
        }

        Member member = Member.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .nickname(request.nickname())
                .build();

        Member saved = memberRepository.save(member);
        return MemberRegisterResponse.of(saved.getId(), saved.getEmail(), saved.getNickname());
    }
}
