package com.capstone.web.auth.jwt;

import com.capstone.web.member.domain.MemberRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtTokenProvider {

    private final JwtProperties properties;
    private SecretKey signingKey;

    public JwtTokenProvider(JwtProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    void init() {
        String configured = properties.getSecret();
        byte[] keyBytes = null;

        // Try standard Base64 first
        try {
            keyBytes = Decoders.BASE64.decode(configured);
            log.info("Initialized JWT signing key using standard Base64 decoding ({} bytes).", keyBytes.length);
        } catch (Exception base64Ex) {
            log.warn("Standard Base64 decode failed for JWT secret: {}. Trying Base64URL...", base64Ex.getMessage());
            // Try Base64URL (commonly used for URL-safe env values)
            try {
                keyBytes = Decoders.BASE64URL.decode(configured);
                log.info("Initialized JWT signing key using Base64URL decoding ({} bytes).", keyBytes.length);
            } catch (Exception base64UrlEx) {
                log.warn("Base64URL decode also failed: {}. Falling back to raw UTF-8 bytes.", base64UrlEx.getMessage());
                keyBytes = configured.getBytes(StandardCharsets.UTF_8);
                log.info("Initialized JWT signing key using raw UTF-8 bytes ({} bytes).", keyBytes.length);
            }
        }

        // Enforce minimum length (HS256 needs >=32 bytes of secret material)
        if (keyBytes.length < 32) {
            throw new IllegalStateException("JWT secret material too short (" + keyBytes.length + " bytes). Provide >=32 bytes (recommend >=48) after decoding. Current secret env name: JWT_SECRET.");
        }

        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String createToken(Long memberId, MemberRole role) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(properties.getExpirationMillis());

        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .claim("role", role.name())
                .signWith(signingKey)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isValid(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception ex) {
            return false;
        }
    }

    public Long extractMemberId(String token) {
        Claims claims = parseClaims(token);
        return Long.valueOf(claims.getSubject());
    }

    public MemberRole extractRole(String token) {
        Claims claims = parseClaims(token);
        String roleName = claims.get("role", String.class);
        return MemberRole.valueOf(roleName);
    }

    public long getExpirationEpochMillis(String token) {
        Claims claims = parseClaims(token);
        return claims.getExpiration().getTime();
    }
}
