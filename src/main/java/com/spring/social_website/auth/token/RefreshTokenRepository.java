package com.spring.social_website.auth.token;

import com.spring.social_website.user.UserEntity;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {
    @EntityGraph(attributePaths = "user")
    Optional<RefreshTokenEntity> findByToken(String token);
    void deleteByUser(UserEntity user);
}