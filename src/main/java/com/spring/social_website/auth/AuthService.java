package com.spring.social_website.auth;

import com.spring.social_website.auth.dto.ChangePasswordRequestDto;
import com.spring.social_website.auth.dto.LoginRequestDto;
import com.spring.social_website.auth.dto.LoginResponseDto;
import com.spring.social_website.auth.dto.RegisterResponseDto;
import com.spring.social_website.auth.dto.RegisterRequestDto;
import com.spring.social_website.auth.jwt.JwtService;
import com.spring.social_website.auth.token.RefreshTokenEntity;
import com.spring.social_website.auth.token.RefreshTokenService;
import com.spring.social_website.exception.EmailAlreadyInUseException;
import com.spring.social_website.exception.InvalidPasswordException;
import com.spring.social_website.user.UserEntity;
import com.spring.social_website.user.UserRepository;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;

    public LoginResponseDto login(LoginRequestDto request, HttpServletResponse response) {
        var auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()));

        UserEntity user = (UserEntity) auth.getPrincipal();

        String refreshToken = refreshTokenService.createAndPersist(user);
        refreshTokenService.addCookie(refreshToken, response);

        String accessToken = jwtService.generateToken(request.email());
        return new LoginResponseDto(accessToken);
    }

    public RegisterResponseDto register(RegisterRequestDto request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyInUseException();
        }

        UserEntity user = UserEntity.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .firstName(request.firstName())
                .lastName(request.lastName())
                .build();

        userRepository.save(user);

        return new RegisterResponseDto("User registered successfully");
    }

    @Transactional 
    public void changePassword(String email, ChangePasswordRequestDto request){
        UserEntity user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        if(!passwordEncoder.matches(request.currentPassword(), user.getPassword())){
            throw new InvalidPasswordException();
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
    }

    public LoginResponseDto refresh(String refreshToken){
        RefreshTokenEntity tokenEntity = refreshTokenService.validate(refreshToken);
        String newAccessToken = jwtService.generateToken(tokenEntity.getUser().getEmail());
        return new LoginResponseDto(newAccessToken);
    }

}