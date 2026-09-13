package com.spring.social_website.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to reset password using a reset token")
public record ResetPasswordRequestDto(
        @Schema(description = "Password reset token received via email", example = "abc123token")
        @NotBlank(message = "Token is required")
        String token,

        @Schema(description = "New password minimum 8 characters", example = "newSecret456")
        @NotBlank(message = "New password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String newPassword
) {}
