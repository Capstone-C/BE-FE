package com.capstone.web.common.util;

import com.capstone.web.auth.jwt.JwtAuthenticationFilter.MemberPrincipal;
import org.springframework.security.core.Authentication;

/**
 * 인증 관련 공통 유틸리티
 */
public class AuthenticationUtils {

    private AuthenticationUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Authentication 객체에서 회원 ID 추출
     * 
     * @param authentication Spring Security Authentication 객체
     * @return 회원 ID
     * @throws IllegalStateException Principal이 MemberPrincipal이 아닌 경우
     */
    public static Long extractMemberId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new IllegalStateException("인증 정보가 없습니다.");
        }
        
        Object principal = authentication.getPrincipal();
        if (principal instanceof MemberPrincipal memberPrincipal) {
            return memberPrincipal.id();
        }
        
        throw new IllegalStateException("유효하지 않은 인증 정보입니다: " + principal.getClass().getName());
    }
}
