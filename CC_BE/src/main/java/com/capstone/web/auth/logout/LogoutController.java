package com.capstone.web.auth.logout;

import com.capstone.web.auth.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class LogoutController {

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklist tokenBlacklist;

    @Operation(summary = "로그아웃", description = "현재 Access Token 을 블랙리스트에 등록하여 이후 요청을 차단합니다.\n- 이미 로그아웃된(블랙리스트에 있는) 토큰으로 다시 호출해도 200을 반환(멱등성).\n- 서버 재기동 시 In-memory 블랙리스트는 초기화됩니다(개발용). 실서비스에서는 Redis 등 외부 저장소 권장.", security = @SecurityRequirement(name = "JWT"))
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
