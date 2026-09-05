package com.spring.social_website.auth.token;

import com.spring.social_website.user.UserEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {

    @EntityGraph(attributePaths = "user")
    Optional<RefreshTokenEntity> findByToken(String token);

    @Modifying
    @Query("DELETE FROM RefreshTokenEntity r WHERE r.user = :user")
    void deleteByUser(@Param("user") UserEntity user);

    @Modifying
    @Query("DELETE FROM RefreshTokenEntity r WHERE r.token = :token")
    void deleteByToken(@Param("token") String token);
}
