package com.spring.social_website.auth;

import com.spring.social_website.auth.dto.LoginRequestDto;
import com.spring.social_website.auth.dto.LoginResponseDto;
import com.spring.social_website.auth.dto.RegisterResponseDto;
import com.spring.social_website.auth.dto.RegisterRequestDto;
import com.spring.social_website.auth.jwt.JwtService;
import com.spring.social_website.exception.EmailAlreadyInUseException;
import com.spring.social_website.user.UserEntity;
import com.spring.social_website.user.UserRepository;

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

    public LoginResponseDto login(LoginRequestDto request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()));

        String token = jwtService.generateToken(request.email());
        return new LoginResponseDto(token);
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
}