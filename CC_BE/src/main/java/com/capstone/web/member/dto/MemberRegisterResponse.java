package com.capstone.web.member.dto;

public record MemberRegisterResponse(Long id, String email, String nickname, String message) {

    public static MemberRegisterResponse of(Long id, String email, String nickname) {
        return new MemberRegisterResponse(id, email, nickname, "회원가입이 완료되었습니다.");
    }
}
