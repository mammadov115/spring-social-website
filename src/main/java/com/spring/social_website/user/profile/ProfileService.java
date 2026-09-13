package com.spring.social_website.user.profile;

import com.spring.social_website.user.UserEntity;
import com.spring.social_website.user.UserRepository;
import com.spring.social_website.user.profile.dto.MyProfileResponseDto;
import com.spring.social_website.user.profile.dto.ProfileResponseDto;
import com.spring.social_website.user.profile.dto.UpdateProfileRequestDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    public void createForUser(UserEntity user) {
        ProfileEntity profile = ProfileEntity.builder()
                .user(user)
                .build();
        profileRepository.save(profile);
    }

    public MyProfileResponseDto getMyProfile(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        ProfileEntity profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Profile not found"));

        return MyProfileResponseDto.from(user, profile);
    }

    public ProfileResponseDto getProfileByUserId(UUID targetUserId) {
        ProfileEntity profile = profileRepository.findByUserIdWithUser(targetUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return ProfileResponseDto.from(profile.getUser(), profile);
    }

    @Transactional
    public MyProfileResponseDto updateMyProfile(String email, UpdateProfileRequestDto request) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        ProfileEntity profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Profile not found"));

        if (request.bio() != null)               profile.setBio(request.bio());
        if (request.avatarUrl() != null)         profile.setAvatarUrl(request.avatarUrl());
        if (request.birthDate() != null)         profile.setBirthDate(request.birthDate());
        if (request.location() != null)          profile.setLocation(request.location());
        if (request.isEmailPublic() != null)     profile.setEmailPublic(request.isEmailPublic());
        if (request.isBirthDatePublic() != null) profile.setBirthDatePublic(request.isBirthDatePublic());
        if (request.isLocationPublic() != null)  profile.setLocationPublic(request.isLocationPublic());

        if (request.firstName() != null) user.setFirstName(request.firstName());
        if (request.lastName() != null)  user.setLastName(request.lastName());

        profileRepository.save(profile);

        return MyProfileResponseDto.from(user, profile);
    }
}
