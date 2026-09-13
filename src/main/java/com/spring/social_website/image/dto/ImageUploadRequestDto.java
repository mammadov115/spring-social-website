package com.spring.social_website.image.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Image upload request")
public record ImageUploadRequestDto(
        @Schema(description = "Image title", example = "Sunset in Baku")
        @NotBlank(message = "Title is required")
        @Size(max = 255, message = "Title must be at most 255 characters")
        String title,

        @Schema(description = "Image description", example = "A beautiful sunset")
        String description
) {}
