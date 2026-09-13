package com.spring.social_website.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request to send a password reset link")
public record ForgotPasswordRequestDto(
        @Schema(description = "Registered email address", example = "jane@example.com")
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email
) {}
