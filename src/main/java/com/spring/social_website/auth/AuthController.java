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
    public ResponseEntity<Map<String, Object>> login(
            @Valid @RequestBody LoginRequestDto request,
            HttpServletResponse response) {
        LoginResponseDto dto = authService.login(request, response);
        return ResponseEntity.ok(Map.of("status", "success", "data", Map.of("auth", dto)));
    }

    @Operation(summary = "Register", description = "Creates a new user account.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Registration successful"),
        @ApiResponse(responseCode = "409", description = "Email already in use"),
        @ApiResponse(responseCode = "422", description = "Validation failed")
    })
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequestDto request) {
        RegisterResponseDto dto = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("status", "success", "data", Map.of("user", dto)));
    }

    @Operation(summary = "Refresh token", description = "Issues a new access token using the refresh token cookie.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "New access token issued"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid refresh token")
    })
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refresh(
            @CookieValue(name = "refresh_token", required = false) String refreshToken,
            HttpServletResponse response) {
        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("status", "fail", "data", Map.of("token", "Refresh token is missing")));
        }
        LoginResponseDto dto = authService.refresh(refreshToken);
        return ResponseEntity.ok(Map.of("status", "success", "data", Map.of("auth", dto)));
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
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
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
        return ResponseEntity.ok(Map.of("status", "success", "data",
                Map.of("message", "If this email is registered, a reset link has been sent")));
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
        return ResponseEntity.ok(Map.of("status", "success", "data",
                Map.of("message", "Password reset successfully")));
    }

    @Operation(summary = "OAuth2 callback", description = "Receives the access token after OAuth2 login and returns it as JSON.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OAuth2 login successful")
    })
    @GetMapping("/oauth2/callback")
    public ResponseEntity<Map<String, Object>> oauth2Callback(@RequestParam("token") String token) {
        return ResponseEntity.ok(Map.of("status", "success", "data", Map.of("accessToken", token)));
    }
}
