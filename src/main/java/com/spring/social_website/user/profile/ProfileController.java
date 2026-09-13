package com.spring.social_website.user.profile;

import com.spring.social_website.user.profile.dto.MyProfileResponseDto;
import com.spring.social_website.user.profile.dto.ProfileResponseDto;
import com.spring.social_website.user.profile.dto.UpdateProfileRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/api/me/profile")
    public ResponseEntity<?> getMyProfile(@AuthenticationPrincipal UserDetails userDetails) {
        MyProfileResponseDto profile = profileService.getMyProfile(userDetails.getUsername());
        return ResponseEntity.ok(Map.of("status", "success", "data", Map.of("profile", profile)));
    }

    @PutMapping("/api/me/profile")
    public ResponseEntity<?> updateMyProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequestDto request) {
        MyProfileResponseDto profile = profileService.updateMyProfile(userDetails.getUsername(), request);
        return ResponseEntity.ok(Map.of("status", "success", "data", Map.of("profile", profile)));
    }

    @GetMapping("/api/users/{id}/profile")
    public ResponseEntity<?> getPublicProfile(@PathVariable UUID id) {
        ProfileResponseDto profile = profileService.getProfileByUserId(id);
        return ResponseEntity.ok(Map.of("status", "success", "data", Map.of("profile", profile)));
    }
}
