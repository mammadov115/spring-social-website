package com.spring.social_website.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestDto(

        @Email(message = "Email format not valid")
        @NotBlank(message = "Email not be empty")
        String email,

        @NotBlank(message = "Password not be empty")
        String password
) {}