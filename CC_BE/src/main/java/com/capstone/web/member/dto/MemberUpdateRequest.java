package com.capstone.web.member.dto;

import org.springframework.web.multipart.MultipartFile;

/**
 * multipart/form-data 로 nickname, profileImage(선택) 전달.
 */
public record MemberUpdateRequest(String nickname, MultipartFile profileImage) {
}
