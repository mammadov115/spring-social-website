package com.spring.social_website.user.profile.dto;

import com.spring.social_website.user.UserEntity;
import com.spring.social_website.user.profile.ProfileEntity;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Public profile of a user. Private fields are null if hidden by privacy settings.")
public record ProfileResponseDto(
        @Schema(description = "User ID", example = "e69d0ccf-c1cf-42af-ba41-d23f2e569a12")
        UUID id,

        @Schema(description = "First name", example = "Jane")
        String firstName,

        @Schema(description = "Last name", example = "Smith")
        String lastName,

        @Schema(description = "Bio", example = "Software engineer from Baku")
        String bio,

        @Schema(description = "Avatar URL", example = "https://example.com/avatar.jpg")
        String avatarUrl,

        @Schema(description = "Email — null if emailPublic is false", example = "jane@example.com", nullable = true)
        String email,

        @Schema(description = "Date of birth — null if birthDatePublic is false", example = "1995-06-15", nullable = true)
        LocalDate birthDate,

        @Schema(description = "Location — null if locationPublic is false", example = "Baku, Azerbaijan", nullable = true)
        String location
) {
    public static ProfileResponseDto from(UserEntity user, ProfileEntity profile) {
        return new ProfileResponseDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                profile.getBio(),
                profile.getAvatarUrl(),
                profile.isEmailPublic()     ? user.getEmail()        : null,
                profile.isBirthDatePublic() ? profile.getBirthDate() : null,
                profile.isLocationPublic()  ? profile.getLocation()  : null
        );
    }
}
