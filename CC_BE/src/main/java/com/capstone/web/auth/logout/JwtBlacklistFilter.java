package com.capstone.web.auth.logout;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtBlacklistFilter extends OncePerRequestFilter {

    private final TokenBlacklist tokenBlacklist;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        String path = request.getRequestURI();
        // 로그아웃/로그인 엔드포인트에서는 블랙리스트 차단을 적용하지 않아 멱등성 및 재요청 허용
        boolean skip = path.startsWith("/api/v1/auth/login") || path.startsWith("/api/v1/auth/logout");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (!skip && tokenBlacklist.isBlacklisted(token)) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":\"AUTH_TOKEN_BLACKLISTED\",\"message\":\"로그아웃된 토큰입니다.\"}");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
