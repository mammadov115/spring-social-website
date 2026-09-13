package com.spring.social_website.user.avatar;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AvatarControllerTest {

    @Mock
    private AvatarService avatarService;

    @InjectMocks
    private AvatarController avatarController;

    @SuppressWarnings("unchecked")
    private Map<String, Object> bodyAs(ResponseEntity<?> response) {
        return (Map<String, Object>) response.getBody();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> dataFrom(Map<String, Object> body) {
        return (Map<String, Object>) body.get("data");
    }

    @Test
    void uploadAvatar_happyPath_returns200WithUrl() throws IOException {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("file", "avatar.jpg", "image/jpeg", new byte[]{1, 2, 3});
        String expectedUrl = "https://res.cloudinary.com/demo/image/upload/avatars/abc.jpg";
        when(avatarService.uploadAvatar("user@test.com", file)).thenReturn(expectedUrl);

        // Act
        ResponseEntity<?> response = avatarController.uploadAvatar("user@test.com", file);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> body = bodyAs(response);
        assertThat(body).containsEntry("status", "success");
        assertThat(dataFrom(body)).containsEntry("avatarUrl", expectedUrl);
    }

    @Test
    void uploadAvatar_nullFile_returns400() throws IOException {
        // Act
        ResponseEntity<?> response = avatarController.uploadAvatar("user@test.com", null);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Map<String, Object> body = bodyAs(response);
        assertThat(body).containsEntry("status", "fail");
        assertThat(dataFrom(body)).containsEntry("file", "File is required");
        verifyNoInteractions(avatarService);
    }

    @Test
    void uploadAvatar_emptyFile_returns400() throws IOException {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("file", "avatar.jpg", "image/jpeg", new byte[0]);

        // Act
        ResponseEntity<?> response = avatarController.uploadAvatar("user@test.com", file);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Map<String, Object> body = bodyAs(response);
        assertThat(body).containsEntry("status", "fail");
        assertThat(dataFrom(body)).containsEntry("file", "File is required");
        verifyNoInteractions(avatarService);
    }

    @Test
    void uploadAvatar_nonImageContentType_returns400() throws IOException {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", new byte[]{1, 2, 3});

        // Act
        ResponseEntity<?> response = avatarController.uploadAvatar("user@test.com", file);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Map<String, Object> body = bodyAs(response);
        assertThat(dataFrom(body)).containsEntry("file", "Only image files are allowed");
        verifyNoInteractions(avatarService);
    }

    @Test
    void uploadAvatar_nullContentType_returns400() throws IOException {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("file", "avatar.jpg", null, new byte[]{1, 2, 3});

        // Act
        ResponseEntity<?> response = avatarController.uploadAvatar("user@test.com", file);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Map<String, Object> body = bodyAs(response);
        assertThat(dataFrom(body)).containsEntry("file", "Only image files are allowed");
        verifyNoInteractions(avatarService);
    }
}
