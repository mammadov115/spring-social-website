package com.spring.social_website.auth.dto;

public record LoginResponseDto(
        String accessToken,
        String tokenType) {
    public LoginResponseDto(String accessToken) {
        this(accessToken, "Bearer");
    }
}