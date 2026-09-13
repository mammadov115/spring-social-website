package com.spring.social_website.user.profile;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProfileRepository extends JpaRepository<ProfileEntity, UUID> {
    Optional<ProfileEntity> findByUserId(UUID id);

    @Query("SELECT p FROM ProfileEntity p JOIN FETCH p.user WHERE p.user.id = :userId")
    Optional<ProfileEntity> findByUserIdWithUser(@Param("userId") UUID userId);
}
