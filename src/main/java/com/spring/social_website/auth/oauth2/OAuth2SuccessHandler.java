package com.spring.social_website.auth.oauth2;

import com.spring.social_website.auth.jwt.JwtService;
import com.spring.social_website.auth.token.RefreshTokenService;
import com.spring.social_website.user.UserEntity;
import com.spring.social_website.user.UserRepository;
import com.spring.social_website.user.profile.ProfileEntity;
import com.spring.social_website.user.profile.ProfileRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();

        String email = oauth2User.getAttribute("email");
        String firstName = oauth2User.getAttribute("given_name");
        String lastName = oauth2User.getAttribute("family_name");
        String providerId = oauth2User.getAttribute("sub");

        UserEntity user = findOrCreateUser(email, firstName, lastName, providerId);

        String accessToken = jwtService.generateToken(user.getEmail());
        String refreshToken = refreshTokenService.createAndPersist(user);
        refreshTokenService.addCookie(refreshToken, response);

        getRedirectStrategy().sendRedirect(request, response,
                "/api/auth/oauth2/callback?token=" + accessToken);
    }

    private UserEntity findOrCreateUser(String email, String firstName,
                                        String lastName, String providerId) {
        Optional<UserEntity> existing = userRepository.findByEmail(email);

        if (existing.isPresent()) {
            return existing.get();
        }

        UserEntity user = UserEntity.builder()
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .provider("google")
                .providerId(providerId)
                .password(null)
                .build();

        user = userRepository.save(user);

        ProfileEntity profile = ProfileEntity.builder()
                .user(user)
                .build();
        profileRepository.save(profile);

        return user;
    }
}
