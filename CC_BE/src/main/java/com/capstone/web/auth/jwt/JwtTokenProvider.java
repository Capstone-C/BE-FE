package com.capstone.web.auth.jwt;

import com.capstone.web.member.domain.MemberRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private final JwtProperties properties;
    private SecretKey signingKey;

    public JwtTokenProvider(JwtProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    void init() {
        byte[] keyBytes = Decoders.BASE64.decode(properties.getSecret());
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
