package com.capstone.web.member.service;

import com.capstone.web.member.domain.Member;
import com.capstone.web.member.domain.PasswordResetToken;
import com.capstone.web.member.dto.PasswordResetRequest;
import com.capstone.web.member.dto.PasswordResetConfirmRequest;
import com.capstone.web.member.exception.PasswordResetErrorCode;
import com.capstone.web.member.exception.PasswordResetException;
import com.capstone.web.member.repository.MemberRepository;
import com.capstone.web.member.repository.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PasswordResetService {
    private final MemberRepository memberRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final MemberService memberService;

    private static final int EXPIRY_MINUTES = 30;

    public void requestReset(PasswordResetRequest request) {
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new PasswordResetException(PasswordResetErrorCode.EMAIL_NOT_FOUND));
        // invalidate existing active tokens
        int invalidated = tokenRepository.invalidateActiveTokens(member, LocalDateTime.now());
        if (invalidated > 0) {
            log.debug("Invalidated {} active password reset tokens for member {}", invalidated, member.getId());
        }
        // create new token
        String tokenValue = generateTokenValue();
        PasswordResetToken token = PasswordResetToken.builder()
                .member(member)
                .token(tokenValue)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(EXPIRY_MINUTES))
                .build();
        tokenRepository.save(token);
        // stub email sending
        log.info("[PASSWORD-RESET] token={} email={}", tokenValue, member.getEmail());
    }

    public void confirmReset(PasswordResetConfirmRequest request) {
        PasswordResetToken token = tokenRepository.findByToken(request.token())
                .orElseThrow(() -> new PasswordResetException(PasswordResetErrorCode.TOKEN_NOT_FOUND_OR_EXPIRED));
        if (token.isExpired()) {
            throw new PasswordResetException(PasswordResetErrorCode.TOKEN_NOT_FOUND_OR_EXPIRED);
        }
        if (token.isUsed() || token.isInvalidated()) {
            throw new PasswordResetException(PasswordResetErrorCode.TOKEN_ALREADY_USED_OR_INVALIDATED);
        }
        if (!request.newPassword().equals(request.newPasswordConfirm())) {
            throw new PasswordResetException(PasswordResetErrorCode.PASSWORD_CONFIRM_MISMATCH);
        }
        Member member = token.getMember();
        // 재사용 / 동일 비밀번호 검증은 memberService.resetPassword 내부 처리
        memberService.resetPassword(member, request.newPassword());
        token.markUsed();
    }

    private String generateTokenValue() {
        byte[] bytes = new byte[48];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
