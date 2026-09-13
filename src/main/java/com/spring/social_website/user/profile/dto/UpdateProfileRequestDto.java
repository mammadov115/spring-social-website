package com.spring.social_website.user.profile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Schema(description = "Request body for updating the authenticated user's profile. All fields are optional.")
public record UpdateProfileRequestDto(
        @Schema(description = "First name", example = "Jane", maxLength = 100)
        @Size(max = 100) String firstName,

        @Schema(description = "Last name", example = "Smith", maxLength = 100)
        @Size(max = 100) String lastName,

        @Schema(description = "Short bio", example = "Software engineer from Baku", maxLength = 500)
        @Size(max = 500) String bio,

        @Schema(description = "Avatar image URL", example = "https://example.com/avatar.jpg", maxLength = 500)
        @Size(max = 500) String avatarUrl,

        @Schema(description = "Date of birth", example = "1995-06-15")
        LocalDate birthDate,

        @Schema(description = "Location", example = "Baku, Azerbaijan", maxLength = 255)
        @Size(max = 255) String location,

        @Schema(description = "Whether email is visible on public profile", example = "false")
        Boolean isEmailPublic,

        @Schema(description = "Whether birth date is visible on public profile", example = "false")
        Boolean isBirthDatePublic,

        @Schema(description = "Whether location is visible on public profile", example = "true")
        Boolean isLocationPublic
) {}
