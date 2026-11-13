package com.minibanking.api.service;

import com.minibanking.api.dto.request.LoginRequest;
import com.minibanking.api.dto.request.RegisterRequest;
import com.minibanking.api.dto.response.AuthResponse;
import com.minibanking.api.exception.DuplicateResourceException;
import com.minibanking.api.model.User;
import com.minibanking.api.repository.UserRepository;
import com.minibanking.api.security.CustomUserPrincipal;
import com.minibanking.api.security.jwt.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("register() should encode password, save user, and return JWT AuthResponse")
    void register_Success() {
        RegisterRequest request = new RegisterRequest("Sreyroth Keo", "sreyroth@bank.com", "mypassword123", "ROLE_USER");
        User savedUser = new User(1L, "Sreyroth Keo", "sreyroth@bank.com", "BCryptEncodedPassword", "ROLE_USER", Instant.now());

        when(userRepository.existsByEmail("sreyroth@bank.com")).thenReturn(false);
        when(passwordEncoder.encode("mypassword123")).thenReturn("BCryptEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(any(), eq(1L), eq("ROLE_USER"))).thenReturn("mocked.jwt.token");
        when(jwtService.getExpirationSeconds()).thenReturn(86400L);

        AuthResponse response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo("mocked.jwt.token");
        assertThat(response.user().email()).isEqualTo("sreyroth@bank.com");
        verify(passwordEncoder, times(1)).encode("mypassword123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("register() should throw DuplicateResourceException when email already exists")
    void register_DuplicateEmail_ThrowsException() {
        RegisterRequest request = new RegisterRequest("Existing", "exist@bank.com", "pass123", "ROLE_USER");
        when(userRepository.existsByEmail("exist@bank.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already registered");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("login() should authenticate credentials and return valid JWT")
    void login_Success() {
        LoginRequest request = new LoginRequest("alice@bank.com", "password123");
        User user = new User(1L, "Alice", "alice@bank.com", "hashed", "ROLE_USER", Instant.now());
        CustomUserPrincipal principal = new CustomUserPrincipal(user);

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(principal);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(jwtService.generateToken(any(), eq(1L), eq("ROLE_USER"))).thenReturn("mock.jwt.token");
        when(jwtService.getExpirationSeconds()).thenReturn(86400L);

        AuthResponse response = authService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo("mock.jwt.token");
        assertThat(response.user().email()).isEqualTo("alice@bank.com");
    }
}
