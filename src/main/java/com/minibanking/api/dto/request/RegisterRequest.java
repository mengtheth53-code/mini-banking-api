package com.minibanking.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank(message = "Full name cannot be blank")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    String fullName,

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email must be a valid email address")
    String email,

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 6, max = 50, message = "Password must be between 6 and 50 characters")
    String password,

    String role // optional, defaults to ROLE_USER
) {}
