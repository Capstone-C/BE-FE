package com.capstone.web.member.dto;

import jakarta.validation.constraints.NotNull;

public record BlockRequest(@NotNull Long blockedId) {}
