package com.spring.social_website.auth.token;

import com.spring.social_website.exception.InvalidTokenException;
import com.spring.social_website.user.UserEntity;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Value("${app.jwt.refresh-token-expiration-days}")
    private int REFRESH_TOKEN_DAYS;

    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public String createAndPersist(UserEntity user) {
        refreshTokenRepository.deleteByUser(user);
        String raw = UUID.randomUUID().toString();
        RefreshTokenEntity entity = RefreshTokenEntity.builder()
                .token(raw)
                .user(user)
                .expiresAt(LocalDateTime.now().plusDays(REFRESH_TOKEN_DAYS))
                .createdAt(LocalDateTime.now())
                .build();
        refreshTokenRepository.save(entity);
        return raw;
    }

    public RefreshTokenEntity validate(String token) {
        RefreshTokenEntity entity = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));
        if (entity.isExpired()) {
            refreshTokenRepository.delete(entity);
            throw new InvalidTokenException("Refresh token expired");
        }
        return entity;
    }

    @Transactional
    public void logout(String token, HttpServletResponse response) {
        refreshTokenRepository.deleteByToken(token);
        Cookie cookie = new Cookie("refresh_token", "");
        cookie.setHttpOnly(true);
        cookie.setPath("/api/auth");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    public void addCookie(String token, HttpServletResponse response) {
        Cookie cookie = new Cookie("refresh_token", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/api/auth");
        cookie.setMaxAge(REFRESH_TOKEN_DAYS * 24 * 60 * 60);
        response.addCookie(cookie);
    }
}
