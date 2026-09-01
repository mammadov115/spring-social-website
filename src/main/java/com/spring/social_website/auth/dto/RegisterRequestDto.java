package com.spring.social_website.auth.dto;

public record RegisterRequestDto(
        String email,
        String password,
        String firstName,
        String lastName
) {}