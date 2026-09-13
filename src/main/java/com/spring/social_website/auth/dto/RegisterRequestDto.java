package com.spring.social_website.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "New user registration request")
public record RegisterRequestDto(
        @Schema(description = "Email address", example = "jane@example.com")
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @Schema(description = "Password  minimum 6 characters", example = "secret123")
        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        String password,

        @Schema(description = "First name", example = "Jane")
        @NotBlank(message = "First name is required")
        String firstName,

        @Schema(description = "Last name", example = "Smith")
        @NotBlank(message = "Last name is required")
        String lastName
) {}
