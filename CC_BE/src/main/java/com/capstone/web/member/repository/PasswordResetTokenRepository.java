package com.capstone.web.member.repository;

import com.capstone.web.member.domain.Member;
import com.capstone.web.member.domain.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);

    @Query("select t from PasswordResetToken t where t.member = :member and t.usedAt is null and t.invalidatedAt is null and t.expiresAt > :now")
    List<PasswordResetToken> findActiveTokens(Member member, LocalDateTime now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update PasswordResetToken t set t.invalidatedAt = :now where t.member = :member and t.usedAt is null and t.invalidatedAt is null and t.expiresAt > :now")
    int invalidateActiveTokens(Member member, LocalDateTime now);
}
