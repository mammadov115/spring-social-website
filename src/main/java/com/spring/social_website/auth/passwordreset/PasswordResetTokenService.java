package com.spring.social_website.auth.passwordreset;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.spring.social_website.exception.InvalidTokenException;
import com.spring.social_website.user.UserEntity;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PasswordResetTokenService {
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

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

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(fromEmail);
        message.setTo(user.getEmail());
        message.setSubject("Password Reset Request");
        message.setText("Click the link to reset your password (expires in 15 minutes)\n\n " + resetLink);
        mailSender.send(message);
    }

    @Transactional 
    public PasswordResetTokenEntity validateAndGet(String token){
        PasswordResetTokenEntity entity = passwordResetTokenRepository.findByToken(token)
        .orElseThrow(()-> new InvalidTokenException("Invalid or expired token"));

        if(entity.isExpired()){
            passwordResetTokenRepository.delete(entity);
            throw new InvalidTokenException("Invalid or expired token");
        }

        return entity;
    }

    @Transactional 
    public void  deleteByUserId(UUID userId){
        passwordResetTokenRepository.deleteByUserId(userId);
    }
}
