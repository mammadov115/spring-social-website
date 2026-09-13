package com.spring.social_website.image;

import com.spring.social_website.image.dto.ImageResponseDto;
import com.spring.social_website.image.dto.ImageUploadRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
@Tag(name = "Images", description = "Image upload, feed, likes and bookmarks")
@SecurityRequirement(name = "bearerAuth")
public class ImageController {

    private final ImageService imageService;

    @Operation(summary = "Upload image", description = "Upload a new image to Cloudinary and save metadata.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Image uploaded successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid file or request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> upload(
            @AuthenticationPrincipal String email,
            @Valid @RequestPart("data") ImageUploadRequestDto request,
            @RequestPart("file") MultipartFile file) throws IOException {

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    Map.of("status", "fail", "data", Map.of("file", "File is required")));
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest().body(
                    Map.of("status", "fail", "data", Map.of("file", "Only image files are allowed")));
        }

        ImageResponseDto dto = imageService.upload(email, request, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of("status", "success", "data", Map.of("image", dto)));
    }

    @Operation(summary = "Get feed", description = "Returns paginated list of all images ordered by newest first.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Feed returned"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    public ResponseEntity<Map<String, Object>> getFeed(
            @AuthenticationPrincipal String email,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<ImageResponseDto> page = imageService.getFeed(email, pageable);
        return ResponseEntity.ok(Map.of("status", "success", "data", Map.of("images", page)));
    }

    @Operation(summary = "Get image by ID", description = "Returns a single image by its ID.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Image found"),
        @ApiResponse(responseCode = "404", description = "Image not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(
            @AuthenticationPrincipal String email,
            @PathVariable UUID id) {

        ImageResponseDto dto = imageService.getById(email, id);
        return ResponseEntity.ok(Map.of("status", "success", "data", Map.of("image", dto)));
    }

    @Operation(summary = "Delete image", description = "Deletes the image from Cloudinary and DB. Only the owner can delete.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Image deleted"),
        @ApiResponse(responseCode = "403", description = "Not the owner"),
        @ApiResponse(responseCode = "404", description = "Image not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal String email,
            @PathVariable UUID id) {

        imageService.delete(email, id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Toggle like", description = "Likes or unlikes an image.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Like toggled"),
        @ApiResponse(responseCode = "404", description = "Image not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/{id}/like")
    public ResponseEntity<Map<String, Object>> toggleLike(
            @AuthenticationPrincipal String email,
            @PathVariable UUID id) {

        ImageResponseDto dto = imageService.toggleLike(email, id);
        return ResponseEntity.ok(Map.of("status", "success", "data", Map.of("image", dto)));
    }

    @Operation(summary = "Toggle bookmark", description = "Bookmarks or unbookmarks an image.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Bookmark toggled"),
        @ApiResponse(responseCode = "404", description = "Image not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/{id}/bookmark")
    public ResponseEntity<Map<String, Object>> toggleBookmark(
            @AuthenticationPrincipal String email,
            @PathVariable UUID id) {

        ImageResponseDto dto = imageService.toggleBookmark(email, id);
        return ResponseEntity.ok(Map.of("status", "success", "data", Map.of("image", dto)));
    }

    @Operation(summary = "Get my bookmarks", description = "Returns paginated list of images bookmarked by the current user.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Bookmarks returned"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/bookmarks")
    public ResponseEntity<Map<String, Object>> getBookmarks(
            @AuthenticationPrincipal String email,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<ImageResponseDto> page = imageService.getBookmarks(email, pageable);
        return ResponseEntity.ok(Map.of("status", "success", "data", Map.of("images", page)));
    }
}
