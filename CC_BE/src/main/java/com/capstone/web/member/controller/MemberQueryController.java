
package com.capstone.web.member.controller;

import com.capstone.web.auth.jwt.JwtAuthenticationFilter.MemberPrincipal;
import com.capstone.web.member.domain.Member;
import com.capstone.web.member.dto.MemberProfileResponse;
import com.capstone.web.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import com.capstone.web.member.service.MemberUpdateService;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberQueryController {

    private final MemberRepository memberRepository;
    private final MemberUpdateService memberUpdateService;

    @Operation(summary = "내 프로필 조회", description = "로그인한 회원의 상세 정보를 반환합니다.", security = @SecurityRequirement(name = "JWT"))
    @GetMapping("/me")
    public ResponseEntity<MemberProfileResponse> me(Authentication authentication) {
        MemberPrincipal principal = (MemberPrincipal) authentication.getPrincipal();
        Member member = memberRepository.findById(principal.id()).orElseThrow();
        // 탈퇴 회원 접근 차단
        if (member.isDeleted()) {
            throw new com.capstone.web.auth.exception.WithdrawnMemberException();
        }
        MemberProfileResponse response = new MemberProfileResponse(
            member.getId(),
            member.getEmail(),
            member.getNickname(),
            member.getRole(),
            member.getProfile(),
            member.getExportScore(),
            member.getRepresentativeBadgeId(),
            member.getJoinedAt(),
            member.getLastLoginAt()
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "내 프로필 수정", description = "닉네임 또는 프로필 이미지를 수정합니다. multipart/form-data 사용. 둘 중 하나만 보내도 됩니다.\n프로필 이미지는 서버 로컬 uploads/profile 에 저장되고 반환되는 URL 은 정적 자원 매핑(추후 CDN/외부 스토리지 대체 예정).", security = @SecurityRequirement(name = "JWT"))
    @PatchMapping(value = "/me", consumes = {"multipart/form-data"})
    public ResponseEntity<MemberProfileResponse> updateMe(Authentication authentication,
                                                         @RequestPart(value = "nickname", required = false) String nickname,
                                                         @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {
        MemberPrincipal principal = (MemberPrincipal) authentication.getPrincipal();
        Member member = memberRepository.findById(principal.id()).orElseThrow();
        if (member.isDeleted()) {
            throw new com.capstone.web.auth.exception.WithdrawnMemberException();
        }
        MemberProfileResponse updated = memberUpdateService.update(member, nickname, profileImage);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "회원 탈퇴", description = "로그인한 회원이 soft delete 처리됩니다.\n- 실제 레코드는 삭제되지 않고 deletedAt 이 세팅됩니다.\n- 탈퇴 후 동일 토큰으로 접근 시 별도 정책에 따라 제한 가능.\n- 재가입 시 이메일/닉네임 중복 정책 고려 필요.", security = @SecurityRequirement(name = "JWT"))
    @DeleteMapping("/me")
    public ResponseEntity<Void> withdrawMe(Authentication authentication) {
        MemberPrincipal principal = (MemberPrincipal) authentication.getPrincipal();
        Member member = memberRepository.findById(principal.id()).orElseThrow();
        if (!member.isDeleted()) {
            member.softDelete();
            memberRepository.save(member);
        }
        return ResponseEntity.ok().build();
    }
}
