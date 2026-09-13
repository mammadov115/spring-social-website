package com.spring.social_website.user.profile;

import com.spring.social_website.user.profile.dto.MyProfileResponseDto;
import com.spring.social_website.user.profile.dto.ProfileResponseDto;
import com.spring.social_website.user.profile.dto.UpdateProfileRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Profile", description = "User profile management")
public class ProfileController {

    private final ProfileService profileService;

    @Operation(summary = "Get my profile", description = "Returns the authenticated user's full profile including private fields", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Profile returned successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized — missing or invalid token")
    })
    @GetMapping("/api/me/profile")
    public ResponseEntity<?> getMyProfile(@AuthenticationPrincipal String email) {
        MyProfileResponseDto profile = profileService.getMyProfile(email);
        return ResponseEntity.ok(Map.of("status", "success", "data", Map.of("profile", profile)));
    }

    @Operation(summary = "Update my profile", description = "Partially updates the authenticated user's profile. All fields are optional.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized — missing or invalid token"),
        @ApiResponse(responseCode = "422", description = "Validation failed — one or more fields exceed allowed length")
    })
    @PutMapping("/api/me/profile")
    public ResponseEntity<?> updateMyProfile(
            @AuthenticationPrincipal String email,
            @Valid @RequestBody UpdateProfileRequestDto request) {
        MyProfileResponseDto profile = profileService.updateMyProfile(email, request);
        return ResponseEntity.ok(Map.of("status", "success", "data", Map.of("profile", profile)));
    }

    @Operation(summary = "Get public profile", description = "Returns a user's public profile. Private fields (email, birthDate, location) are hidden based on the user's privacy settings.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Profile returned successfully"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/api/users/{id}/profile")
    public ResponseEntity<?> getPublicProfile(@PathVariable UUID id) {
        ProfileResponseDto profile = profileService.getProfileByUserId(id);
        return ResponseEntity.ok(Map.of("status", "success", "data", Map.of("profile", profile)));
    }
}
