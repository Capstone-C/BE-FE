package com.capstone.web.auth.logout;

import com.capstone.web.auth.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
