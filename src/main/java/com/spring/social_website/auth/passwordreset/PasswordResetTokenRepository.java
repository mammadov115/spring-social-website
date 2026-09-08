package com.spring.social_website.auth.passwordreset;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetTokenEntity, Long> {
    @EntityGraph(attributePaths = "user")
    Optional<PasswordResetTokenEntity> findByToken(String token);

    void deleteByUserId(UUID userId);
}