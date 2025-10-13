package com.capstone.web.auth.logout;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * In-memory 토큰 블랙리스트. AccessToken 만료 전 수동 무효화를 지원한다.
 * 추후 Redis 등 외부 저장소로 교체시 인터페이스 추출 예정.
 */
@Component
public class TokenBlacklist {

    private final Map<String, Long> store = new ConcurrentHashMap<>(); // token -> expireEpochMillis
    private final Clock clock = Clock.systemUTC();

    public void blacklist(String token, long expireEpochMillis) {
        store.put(token, expireEpochMillis);
    }

    public boolean isBlacklisted(String token) {
        Long expire = store.get(token);
        if (expire == null) return false;
        long now = Instant.now(clock).toEpochMilli();
        if (expire < now) { // 만료 지난 항목은 제거 (lazy clean)
            store.remove(token);
            return false;
        }
        return true;
    }
}
