package com.spring.social_website.user.profile.dto;

import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record UpdateProfileRequestDto(
        @Size(max = 100) String firstName,
        @Size(max = 100) String lastName,
        @Size(max = 500) String bio,
        @Size(max = 500) String avatarUrl,
        LocalDate birthDate,
        @Size(max = 255) String location,
        Boolean isEmailPublic,
        Boolean isBirthDatePublic,
        Boolean isLocationPublic
) {}