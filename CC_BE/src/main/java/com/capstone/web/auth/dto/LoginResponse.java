package com.capstone.web.auth.dto;

import com.capstone.web.member.domain.Member;
import com.capstone.web.member.domain.MemberRole;

public record LoginResponse(String accessToken, MemberSummary member) {

    public static LoginResponse of(String token, Member member) {
        return new LoginResponse(token, new MemberSummary(member.getId(), member.getEmail(), member.getNickname(), member.getRole()));
    }

    public record MemberSummary(Long id, String email, String nickname, MemberRole role) {
    }
}
