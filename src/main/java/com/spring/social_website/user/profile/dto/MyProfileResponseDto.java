package com.spring.social_website.user.profile.dto;

import com.spring.social_website.user.UserEntity;
import com.spring.social_website.user.profile.ProfileEntity;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Full profile of the authenticated user including private fields")
public record MyProfileResponseDto(
        @Schema(description = "User ID", example = "e69d0ccf-c1cf-42af-ba41-d23f2e569a12")
        UUID id,

        @Schema(description = "Email address", example = "jane@example.com")
        String email,

        @Schema(description = "First name", example = "Jane")
        String firstName,

        @Schema(description = "Last name", example = "Smith")
        String lastName,

        @Schema(description = "Bio", example = "Software engineer from Baku")
        String bio,

        @Schema(description = "Avatar URL", example = "https://example.com/avatar.jpg")
        String avatarUrl,

        @Schema(description = "Date of birth", example = "1995-06-15")
        LocalDate birthDate,

        @Schema(description = "Location", example = "Baku, Azerbaijan")
        String location,

        @Schema(description = "Whether email is visible on public profile", example = "false")
        boolean emailPublic,

        @Schema(description = "Whether birth date is visible on public profile", example = "false")
        boolean birthDatePublic,

        @Schema(description = "Whether location is visible on public profile", example = "true")
        boolean locationPublic,

        @Schema(description = "Profile creation timestamp")
        LocalDateTime createdAt,

        @Schema(description = "Profile last updated timestamp")
        LocalDateTime updatedAt
) {
    public static MyProfileResponseDto from(UserEntity user, ProfileEntity profile) {
        return new MyProfileResponseDto(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                profile.getBio(),
                profile.getAvatarUrl(),
                profile.getBirthDate(),
                profile.getLocation(),
                profile.isEmailPublic(),
                profile.isBirthDatePublic(),
                profile.isLocationPublic(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }
}
