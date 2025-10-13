package com.capstone.web.member.dto;

import java.time.LocalDateTime;

public record BlockListResponse(Long id, Long blockedId, String blockedEmail, LocalDateTime createdAt) {}
