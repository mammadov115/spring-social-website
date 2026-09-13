package com.spring.social_website.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to change the authenticated user's password")
public record ChangePasswordRequestDto(
        @Schema(description = "Current password", example = "oldSecret123")
        @NotBlank(message = "Current password is required")
        String currentPassword,

        @Schema(description = "New password  minimum 6 characters", example = "newSecret456")
        @NotBlank(message = "New password is required")
        @Size(min = 6, message = "New password must be at least 6 characters")
        String newPassword
) {}
