package com.spring.social_website.auth;

import com.spring.social_website.auth.dto.ChangePasswordRequestDto;
import com.spring.social_website.auth.dto.ForgotPasswordRequestDto;
import com.spring.social_website.auth.dto.LoginRequestDto;
import com.spring.social_website.auth.dto.LoginResponseDto;
import com.spring.social_website.auth.dto.RegisterRequestDto;
import com.spring.social_website.auth.dto.RegisterResponseDto;
import com.spring.social_website.auth.dto.ResetPasswordRequestDto;
import com.spring.social_website.auth.token.RefreshTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Authentication and account management")
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;

    @Operation(summary = "Login", description = "Authenticates a user and returns an access token. A refresh token is set as an HttpOnly cookie.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login successful"),
        @ApiResponse(responseCode = "401", description = "Invalid email or password")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(
            @Valid @RequestBody LoginRequestDto request,
            HttpServletResponse response) {
        return ResponseEntity.ok(authService.login(request, response));
    }

    @Operation(summary = "Register", description = "Creates a new user account.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Registration successful"),
        @ApiResponse(responseCode = "409", description = "Email already in use"),
        @ApiResponse(responseCode = "422", description = "Validation failed")
    })
    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDto> register(@Valid @RequestBody RegisterRequestDto request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @Operation(summary = "Refresh token", description = "Issues a new access token using the refresh token cookie.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "New access token issued"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid refresh token")
    })
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDto> refresh(
            @CookieValue(name = "refresh_token", required = false) String refreshToken,
            HttpServletResponse response) {
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(authService.refresh(refreshToken));
    }

    @Operation(summary = "Logout", description = "Invalidates the refresh token cookie and logs the user out.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Logged out successfully")
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = "refresh_token", required = false) String refreshToken,
            HttpServletResponse response) {
        if (refreshToken != null) {
            refreshTokenService.logout(refreshToken, response);
        }
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Change password", description = "Changes the authenticated user's password. Requires the current password.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Password changed successfully"),
        @ApiResponse(responseCode = "400", description = "Current password is incorrect"),
        @ApiResponse(responseCode = "401", description = "Unauthorized  missing or invalid token"),
        @ApiResponse(responseCode = "422", description = "Validation failed")
    })
    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal String email,
            @Valid @RequestBody ChangePasswordRequestDto request) {
        authService.changePassword(email, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Forgot password", description = "Sends a password reset link to the given email if it is registered. Always returns success to prevent email enumeration.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Reset link sent if email is registered"),
        @ApiResponse(responseCode = "422", description = "Validation failed")
    })
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDto request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(Map.of("status", "success", "data", Map.of("message", "If this email is registered, a reset link has been sent")));
    }

    @Operation(summary = "Reset password", description = "Resets the user's password using a valid reset token.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Password reset successfully"),
        @ApiResponse(responseCode = "401", description = "Invalid or expired reset token"),
        @ApiResponse(responseCode = "422", description = "Validation failed")
    })
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@Valid @RequestBody ResetPasswordRequestDto request) {
        authService.resetPassword(request);
        Map<String, Object> data = new HashMap<>();
        data.put("status", "success");
        data.put("data", Map.of("message", "Password reset successfully"));
        return ResponseEntity.ok(data);
    }
}
