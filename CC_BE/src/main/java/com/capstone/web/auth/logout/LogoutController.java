package com.capstone.web.auth.logout;

import com.capstone.web.auth.jwt.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "Auth", description = "인증 관련 API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class LogoutController {

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklist tokenBlacklist;

    @Operation(
        summary = "로그아웃",
        description = """
            현재 Access Token을 블랙리스트에 등록하여 이후 요청을 차단합니다.
            
            **특징**:
            - 멱등성 보장: 이미 로그아웃된 토큰으로 재호출해도 성공 응답
            - In-memory 블랙리스트 (개발용)
            - 실서비스에서는 Redis 등 외부 저장소 권장
            """,
        security = @SecurityRequirement(name = "JWT")
    )
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (!StringUtils.hasText(header) || !header.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Authorization header missing or malformed"
            ));
        }
        String token = header.substring(7);
        if (tokenBlacklist.isBlacklisted(token)) {
            // 멱등성 보장 - 이미 블랙리스트면 그대로 성공
            return ResponseEntity.ok(Map.of("message", "LOGGED_OUT"));
        }
        long expireAt = jwtTokenProvider.getExpirationEpochMillis(token);
        tokenBlacklist.blacklist(token, expireAt);
        return ResponseEntity.ok(Map.of("message", "LOGGED_OUT"));
    }
}
