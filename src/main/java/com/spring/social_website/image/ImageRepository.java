package com.spring.social_website.image;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ImageRepository extends JpaRepository<ImageEntity, UUID> {

    Page<ImageEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT i FROM ImageEntity i JOIN i.bookmarkedBy b WHERE b.email = :email ORDER BY i.createdAt DESC")
    Page<ImageEntity> findBookmarkedByUserEmail(@Param("email") String email, Pageable pageable);
}
