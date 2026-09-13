package com.spring.social_website.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Registration result message")
public record RegisterResponseDto(
        @Schema(description = "Result message", example = "User registered successfully")
        String message
) {}
