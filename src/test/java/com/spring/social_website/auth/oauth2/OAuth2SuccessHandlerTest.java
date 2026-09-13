package com.spring.social_website.auth.oauth2;

import com.spring.social_website.auth.jwt.JwtService;
import com.spring.social_website.auth.token.RefreshTokenService;
import com.spring.social_website.user.UserEntity;
import com.spring.social_website.user.UserRepository;
import com.spring.social_website.user.profile.ProfileEntity;
import com.spring.social_website.user.profile.ProfileRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.RedirectStrategy;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuth2SuccessHandlerTest {

    @Mock private UserRepository userRepository;
    @Mock private ProfileRepository profileRepository;
    @Mock private JwtService jwtService;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private Authentication authentication;
    @Mock private OAuth2User oauth2User;
    @Mock private RedirectStrategy redirectStrategy;

    @InjectMocks
    private OAuth2SuccessHandler handler;

    private static final String EMAIL = "jane@example.com";
    private static final String FIRST_NAME = "Jane";
    private static final String LAST_NAME = "Smith";
    private static final String PROVIDER_ID = "google-sub-123";
    private static final String ACCESS_TOKEN = "jwt-access-token";
    private static final String REFRESH_TOKEN = "uuid-refresh-token";

    @BeforeEach
    void setUp() {
        // inject mock redirect strategy into the handler
        handler.setRedirectStrategy(redirectStrategy);

        when(authentication.getPrincipal()).thenReturn(oauth2User);
        when(oauth2User.getAttribute("email")).thenReturn(EMAIL);
        when(oauth2User.getAttribute("given_name")).thenReturn(FIRST_NAME);
        when(oauth2User.getAttribute("family_name")).thenReturn(LAST_NAME);
        when(oauth2User.getAttribute("sub")).thenReturn(PROVIDER_ID);
    }

    @Test
    void onAuthenticationSuccess_newUser_createsUserAndProfile() throws IOException {
        // Arrange
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateToken(EMAIL)).thenReturn(ACCESS_TOKEN);
        when(refreshTokenService.createAndPersist(any(UserEntity.class))).thenReturn(REFRESH_TOKEN);

        // Act
        handler.onAuthenticationSuccess(request, response, authentication);

        // Assert - user saved with correct fields
        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(userCaptor.capture());
        UserEntity saved = userCaptor.getValue();
        assertThat(saved.getEmail()).isEqualTo(EMAIL);
        assertThat(saved.getFirstName()).isEqualTo(FIRST_NAME);
        assertThat(saved.getLastName()).isEqualTo(LAST_NAME);
        assertThat(saved.getProvider()).isEqualTo("google");
        assertThat(saved.getProviderId()).isEqualTo(PROVIDER_ID);
        assertThat(saved.getPassword()).isNull();

        // Assert - profile created for new user
        ArgumentCaptor<ProfileEntity> profileCaptor = ArgumentCaptor.forClass(ProfileEntity.class);
        verify(profileRepository).save(profileCaptor.capture());
        assertThat(profileCaptor.getValue().getUser()).isEqualTo(saved);
    }

    @Test
    void onAuthenticationSuccess_existingUser_doesNotCreateDuplicate() throws IOException {
        // Arrange
        UserEntity existing = new UserEntity();
        existing.setId(UUID.randomUUID());
        existing.setEmail(EMAIL);
        existing.setProvider("google");

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existing));
        when(jwtService.generateToken(EMAIL)).thenReturn(ACCESS_TOKEN);
        when(refreshTokenService.createAndPersist(existing)).thenReturn(REFRESH_TOKEN);

        // Act
        handler.onAuthenticationSuccess(request, response, authentication);

        // Assert - no new user or profile inserted
        verify(userRepository, never()).save(any());
        verify(profileRepository, never()).save(any());
    }

    @Test
    void onAuthenticationSuccess_redirectsToCallbackWithToken() throws IOException {
        // Arrange
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateToken(EMAIL)).thenReturn(ACCESS_TOKEN);
        when(refreshTokenService.createAndPersist(any())).thenReturn(REFRESH_TOKEN);

        // Act
        handler.onAuthenticationSuccess(request, response, authentication);

        // Assert
        verify(redirectStrategy).sendRedirect(
                eq(request), eq(response),
                eq("/api/auth/oauth2/callback?token=" + ACCESS_TOKEN));
    }

    @Test
    void onAuthenticationSuccess_existingUser_redirectsWithFreshToken() throws IOException {
        // Arrange
        UserEntity existing = new UserEntity();
        existing.setId(UUID.randomUUID());
        existing.setEmail(EMAIL);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existing));
        when(jwtService.generateToken(EMAIL)).thenReturn("fresh-token");
        when(refreshTokenService.createAndPersist(existing)).thenReturn(REFRESH_TOKEN);

        // Act
        handler.onAuthenticationSuccess(request, response, authentication);

        // Assert
        verify(redirectStrategy).sendRedirect(
                eq(request), eq(response),
                eq("/api/auth/oauth2/callback?token=fresh-token"));
    }

    @Test
    void onAuthenticationSuccess_setsRefreshTokenCookie() throws IOException {
        // Arrange
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateToken(EMAIL)).thenReturn(ACCESS_TOKEN);
        when(refreshTokenService.createAndPersist(any())).thenReturn(REFRESH_TOKEN);

        // Act
        handler.onAuthenticationSuccess(request, response, authentication);

        // Assert
        verify(refreshTokenService).addCookie(REFRESH_TOKEN, response);
    }
}
