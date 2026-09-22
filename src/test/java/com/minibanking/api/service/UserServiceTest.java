package com.minibanking.api.service;

import com.minibanking.api.model.User;
import com.minibanking.api.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("createUser should successfully save and return user when email is unique")
    void createUser_Success() {
        User inputUser = new User(null, "John Doe", "john@bank.com", "USER", null);
        User savedUser = new User(1L, "John Doe", "john@bank.com", "USER", Instant.now());

        when(userRepository.existsByEmail("john@bank.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = userService.createUser(inputUser);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("john@bank.com");
        verify(userRepository, times(1)).save(inputUser);
    }

    @Test
    @DisplayName("createUser should throw IllegalArgumentException when email already exists")
    void createUser_DuplicateEmail_ThrowsException() {
        User duplicateUser = new User(null, "Jane Doe", "existing@bank.com", "USER", null);

        when(userRepository.existsByEmail("existing@bank.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(duplicateUser))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email is already registered");

        verify(userRepository, never()).save(any(User.class));
    }
}
