package com.spring.social_website.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Access token response")
public record LoginResponseDto(
        @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiJ9...")
        String accessToken,

        @Schema(description = "Token type", example = "Bearer")
        String tokenType
) {
    public LoginResponseDto(String accessToken) {
        this(accessToken, "Bearer");
    }
}
