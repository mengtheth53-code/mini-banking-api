package com.minibanking.api.service;

import com.minibanking.api.dto.request.LoginRequest;
import com.minibanking.api.dto.request.RegisterRequest;
import com.minibanking.api.dto.response.AuthResponse;
import com.minibanking.api.dto.response.UserResponse;
import com.minibanking.api.exception.DuplicateResourceException;
import com.minibanking.api.model.User;
import com.minibanking.api.repository.UserRepository;
import com.minibanking.api.security.CustomUserPrincipal;
import com.minibanking.api.security.jwt.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email is already registered: " + request.email());
        }

        String role = (request.role() != null && !request.role().isBlank())
                ? (request.role().startsWith("ROLE_") ? request.role() : "ROLE_" + request.role())
                : "ROLE_USER";

        User user = new User(
                null,
                request.fullName(),
                request.email(),
                passwordEncoder.encode(request.password()), // Hash password using BCrypt
                role,
                null
        );

        User savedUser = userRepository.save(user);
        CustomUserPrincipal principal = new CustomUserPrincipal(savedUser);
        String token = jwtService.generateToken(principal, savedUser.getId(), savedUser.getRole());

        UserResponse userResponse = new UserResponse(
                savedUser.getId(),
                savedUser.getFullName(),
                savedUser.getEmail(),
                savedUser.getRole(),
                savedUser.getCreatedAt()
        );

        return AuthResponse.of(token, jwtService.getExpirationSeconds(), userResponse);
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();
        User user = principal.getUser();
        String token = jwtService.generateToken(principal, user.getId(), user.getRole());

        UserResponse userResponse = new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt()
        );

        return AuthResponse.of(token, jwtService.getExpirationSeconds(), userResponse);
    }
}
