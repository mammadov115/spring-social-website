package com.spring.social_website.image;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

public interface ImageRepository extends JpaRepository<ImageEntity, UUID> {

    @Query("SELECT i FROM ImageEntity i JOIN FETCH i.owner ORDER BY i.createdAt DESC")
    Page<ImageEntity> findAllWithOwner(Pageable pageable);

    @Query(value = "SELECT i FROM ImageEntity i JOIN FETCH i.owner JOIN i.bookmarkedBy b WHERE b.email = :email ORDER BY i.createdAt DESC",
           countQuery = "SELECT COUNT(i) FROM ImageEntity i JOIN i.bookmarkedBy b WHERE b.email = :email")
    Page<ImageEntity> findBookmarkedByUserEmail(@Param("email") String email, Pageable pageable);

    // single-image queries (getById, toggleLike, toggleBookmark ucun)
    @Query("SELECT COUNT(u) FROM ImageEntity i JOIN i.likedBy u WHERE i.id = :id")
    int countLikes(@Param("id") UUID id);

    @Query("SELECT COUNT(u) FROM ImageEntity i JOIN i.bookmarkedBy u WHERE i.id = :id")
    int countBookmarks(@Param("id") UUID id);

    @Query("SELECT COUNT(u) > 0 FROM ImageEntity i JOIN i.likedBy u WHERE i.id = :imageId AND u.email = :email")
    boolean isLikedBy(@Param("imageId") UUID imageId, @Param("email") String email);

    @Query("SELECT COUNT(u) > 0 FROM ImageEntity i JOIN i.bookmarkedBy u WHERE i.id = :imageId AND u.email = :email")
    boolean isBookmarkedBy(@Param("imageId") UUID imageId, @Param("email") String email);

    // batch queries
    record ImageCount(UUID imageId, long count) {}

    @Query("SELECT i FROM ImageEntity i JOIN FETCH i.owner WHERE i.id = :id")
    Optional<ImageEntity> findByIdWithOwner(@Param("id") UUID id);

    @Query("SELECT new com.spring.social_website.image.ImageRepository$ImageCount(i.id, COUNT(u)) " +
           "FROM ImageEntity i JOIN i.likedBy u WHERE i.id IN :ids GROUP BY i.id")
    List<ImageCount> countLikesBatch(@Param("ids") Collection<UUID> ids);

    @Query("SELECT new com.spring.social_website.image.ImageRepository$ImageCount(i.id, COUNT(u)) " +
           "FROM ImageEntity i JOIN i.bookmarkedBy u WHERE i.id IN :ids GROUP BY i.id")
    List<ImageCount> countBookmarksBatch(@Param("ids") Collection<UUID> ids);

    @Query("SELECT i.id FROM ImageEntity i JOIN i.likedBy u WHERE i.id IN :ids AND u.email = :email")
    List<UUID> findLikedImageIds(@Param("ids") Collection<UUID> ids, @Param("email") String email);

    @Query("SELECT i.id FROM ImageEntity i JOIN i.bookmarkedBy u WHERE i.id IN :ids AND u.email = :email")
    List<UUID> findBookmarkedImageIds(@Param("ids") Collection<UUID> ids, @Param("email") String email);
}
