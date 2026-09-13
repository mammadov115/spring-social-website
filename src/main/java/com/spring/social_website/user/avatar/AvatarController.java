package com.spring.social_website.user.avatar;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
@Tag(name = "Avatar", description = "User avatar upload")
public class AvatarController {

    private final AvatarService avatarService;

    @Operation(
        summary = "Upload avatar",
        description = "Uploads a new avatar image. Accepted formats: jpg, jpeg, png, webp. Max size: 2MB. Replaces existing avatar.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Avatar uploaded successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid file - wrong format or missing file"),
        @ApiResponse(responseCode = "401", description = "Unauthorized - missing or invalid token")
    })
    @PutMapping(value = "/avatar", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_OCTET_STREAM_VALUE, MediaType.ALL_VALUE})
    public ResponseEntity<?> uploadAvatar(
            @AuthenticationPrincipal String email,
            @RequestParam(value = "file", required = false) MultipartFile file) throws IOException {

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(
                Map.of("status", "fail", "data", Map.of("file", "File is required"))
            );
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest().body(
                Map.of("status", "fail", "data", Map.of("file", "Only image files are allowed"))
            );
        }

        String avatarUrl = avatarService.uploadAvatar(email, file);
        return ResponseEntity.ok(
            Map.of("status", "success", "data", Map.of("avatarUrl", avatarUrl))
        );
    }
}
