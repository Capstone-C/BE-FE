package com.capstone.web.member.dto;

import com.capstone.web.member.domain.MemberRole;
import java.time.LocalDateTime;

public record MemberProfileResponse(Long id,
                                    String email,
                                    String nickname,
                                    MemberRole role,
                                    String profile,
                                    Long exportScore,
                                    Long representativeBadgeId,
                                    LocalDateTime joinedAt,
                                    LocalDateTime lastLoginAt) {
}
