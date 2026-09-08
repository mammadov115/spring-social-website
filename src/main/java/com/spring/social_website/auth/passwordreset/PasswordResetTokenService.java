package com.spring.social_website.auth.passwordreset;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.spring.social_website.exception.InvalidTokenException;
import com.spring.social_website.user.UserEntity;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetTokenService {
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final MailService mailService;

    @Value("${app.base-url}")
    private String baseUrl;


    @Transactional
    public void createAndSend(UserEntity user) {
        passwordResetTokenRepository.deleteByUserId(user.getId());

        String token = UUID.randomUUID().toString();

        PasswordResetTokenEntity entity = PasswordResetTokenEntity.builder()
                .token(token)
                .user(user)
                .expiresAt(java.time.LocalDateTime.now().plusMinutes(15))
                .createdAt(java.time.LocalDateTime.now())
                .build();

        passwordResetTokenRepository.save(entity);

        String resetLink = baseUrl + "/api/auth/reset-password?token=" + token;

        mailService.sendMailAsync(user.getEmail(), resetLink);
    }



    @Transactional
    public PasswordResetTokenEntity validateAndGet(String token) {
        PasswordResetTokenEntity entity = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid or expired token"));

        if (entity.isExpired()) {
            passwordResetTokenRepository.delete(entity);
            throw new InvalidTokenException("Invalid or expired token");
        }

        return entity;
    }

    @Transactional
    public void deleteByUserId(UUID userId) {
        passwordResetTokenRepository.deleteByUserId(userId);
    }
}
