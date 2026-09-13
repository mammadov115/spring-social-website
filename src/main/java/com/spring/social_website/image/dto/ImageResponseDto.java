package com.spring.social_website.image.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Image response")
public record ImageResponseDto(
        @Schema(description = "Image ID")
        UUID id,

        @Schema(description = "Image title")
        String title,

        @Schema(description = "Image description")
        String description,

        @Schema(description = "Cloudinary image URL")
        String imageUrl,

        @Schema(description = "Owner email")
        String ownerEmail,

        @Schema(description = "Owner full name")
        String ownerName,

        @Schema(description = "Total like count")
        int likeCount,

        @Schema(description = "Total bookmark count")
        int bookmarkCount,

        @Schema(description = "Whether the current user liked this image")
        boolean likedByMe,

        @Schema(description = "Whether the current user bookmarked this image")
        boolean bookmarkedByMe,

        @Schema(description = "Upload date")
        LocalDateTime createdAt
) {}
