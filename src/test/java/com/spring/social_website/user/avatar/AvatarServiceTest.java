package com.spring.social_website.user.avatar;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import com.spring.social_website.user.UserEntity;
import com.spring.social_website.user.UserRepository;
import com.spring.social_website.user.profile.ProfileEntity;
import com.spring.social_website.user.profile.ProfileRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AvatarServiceTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private MultipartFile file;

    @InjectMocks
    private AvatarService avatarService;

    private UserEntity user;
    private ProfileEntity profile;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        user = new UserEntity();
        user.setId(userId);
        user.setEmail("test@test.com");

        profile = new ProfileEntity();
        profile.setId(UUID.randomUUID());
        profile.setAvatarUrl(null);

        when(cloudinary.uploader()).thenReturn(uploader);
    }

    @Test
    void uploadAvatar_happyPath_noExistingAvatar() throws IOException {
        // Arrange
        byte[] imageBytes = new byte[]{1, 2, 3};
        String expectedUrl = "https://res.cloudinary.com/demo/image/upload/v123/avatars/abc.jpg";

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(file.getBytes()).thenReturn(imageBytes);
        when(uploader.upload(eq(imageBytes), any(Map.class))).thenReturn(Map.of("secure_url", expectedUrl));

        // Act
        String result = avatarService.uploadAvatar("test@test.com", file);

        // Assert
        assertThat(result).isEqualTo(expectedUrl);
        assertThat(profile.getAvatarUrl()).isEqualTo(expectedUrl);
        verify(profileRepository).save(profile);
        verify(uploader, never()).destroy(any(), any());
    }

    @Test
    void uploadAvatar_deletesOldAvatar_whenExistingAvatarPresent() throws IOException {
        // Arrange
        String oldUrl = "https://res.cloudinary.com/demo/image/upload/v123/avatars/old.jpg";
        String newUrl = "https://res.cloudinary.com/demo/image/upload/v456/avatars/new.jpg";
        profile.setAvatarUrl(oldUrl);

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(file.getBytes()).thenReturn(new byte[]{1, 2, 3});
        when(uploader.destroy(eq("avatars/old"), any())).thenReturn(Map.of());
        when(uploader.upload(any(byte[].class), any(Map.class))).thenReturn(Map.of("secure_url", newUrl));

        // Act
        String result = avatarService.uploadAvatar("test@test.com", file);

        // Assert
        assertThat(result).isEqualTo(newUrl);
        verify(uploader).destroy(eq("avatars/old"), any());
    }

    @Test
    void uploadAvatar_throwsEntityNotFoundException_whenUserNotFound() {
        // Arrange
        when(userRepository.findByEmail("ghost@test.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> avatarService.uploadAvatar("ghost@test.com", file))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User not found");

        verifyNoInteractions(profileRepository, uploader);
    }

    @Test
    void uploadAvatar_throwsEntityNotFoundException_whenProfileNotFound() {
        // Arrange
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> avatarService.uploadAvatar("test@test.com", file))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Profile not found");

        verifyNoInteractions(uploader);
    }

    @Test
    void uploadAvatar_propagatesIOException_whenCloudinaryFails() throws IOException {
        // Arrange
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(file.getBytes()).thenReturn(new byte[]{1, 2, 3});
        when(uploader.upload(any(byte[].class), any(Map.class))).thenThrow(new IOException("Cloudinary error"));

        // Act & Assert
        assertThatThrownBy(() -> avatarService.uploadAvatar("test@test.com", file))
                .isInstanceOf(IOException.class)
                .hasMessage("Cloudinary error");

        verify(profileRepository, never()).save(any());
    }

    @Test
    void uploadAvatar_extractsPublicId_withVersionSegment() throws IOException {
        // Arrange - URL with version segment (v1789301667/)
        String oldUrl = "https://res.cloudinary.com/demo/image/upload/v1789301667/avatars/abc.jpg";
        profile.setAvatarUrl(oldUrl);

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(file.getBytes()).thenReturn(new byte[]{1, 2, 3});
        when(uploader.destroy(eq("avatars/abc"), any())).thenReturn(Map.of());
        when(uploader.upload(any(byte[].class), any(Map.class)))
                .thenReturn(Map.of("secure_url", "https://res.cloudinary.com/demo/image/upload/v2/avatars/new.jpg"));

        // Act
        avatarService.uploadAvatar("test@test.com", file);

        // Assert - version segment stripped correctly
        verify(uploader).destroy(eq("avatars/abc"), any());
    }
}
