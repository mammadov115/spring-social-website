package com.spring.social_website.user.profile.dto;

import com.spring.social_website.user.UserEntity;
import com.spring.social_website.user.profile.ProfileEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record MyProfileResponseDto(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String bio,
        String avatarUrl,
        LocalDate birthDate,
        String location,
        boolean emailPublic,
        boolean birthDatePublic,
        boolean locationPublic,
        LocalDateTime createdAt,
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
