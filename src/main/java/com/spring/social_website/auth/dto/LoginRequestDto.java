package com.spring.social_website.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Login credentials")
public record LoginRequestDto(
        @Schema(description = "User email address", example = "jane@example.com")
        @Email(message = "Email format not valid")
        @NotBlank(message = "Email not be empty")
        String email,

        @Schema(description = "User password", example = "secret123")
        @NotBlank(message = "Password not be empty")
        String password
) {}
