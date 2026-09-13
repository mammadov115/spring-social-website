package com.spring.social_website.user.profile.dto;

import com.spring.social_website.user.UserEntity;
import com.spring.social_website.user.profile.ProfileEntity;

import java.time.LocalDate;
import java.util.UUID;

public record ProfileResponseDto(
        UUID id,
        String firstName,
        String lastName,
        String bio,
        String avatarUrl,
        String email,
        LocalDate birthDate,
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