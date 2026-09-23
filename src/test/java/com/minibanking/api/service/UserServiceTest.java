package com.minibanking.api.service;

import com.minibanking.api.exception.DuplicateResourceException;
import com.minibanking.api.exception.ResourceNotFoundException;
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
        User input = new User(null, "John Doe", "john@bank.com", "USER", null);
        User saved = new User(1L, "John Doe", "john@bank.com", "USER", Instant.now());

        when(userRepository.existsByEmail("john@bank.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(saved);

        User result = userService.createUser(input);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("john@bank.com");
        verify(userRepository, times(1)).save(input);
    }

    @Test
    @DisplayName("createUser should throw DuplicateResourceException when email already exists")
    void createUser_DuplicateEmail_ThrowsException() {
        User duplicate = new User(null, "Jane Doe", "existing@bank.com", "USER", null);

        when(userRepository.existsByEmail("existing@bank.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(duplicate))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Email is already registered");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("getUserById should throw ResourceNotFoundException when user does not exist")
    void getUserById_NotFound_ThrowsException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with id: 999");
    }

    @Test
    @DisplayName("deleteUser should successfully call repository delete when user exists")
    void deleteUser_Success() {
        User user = new User(1L, "Bob", "bob@bank.com", "USER", Instant.now());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteUser(1L);

        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("deleteUser should throw ResourceNotFoundException when user does not exist")
    void deleteUser_NotFound_ThrowsException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser(999L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(userRepository, never()).deleteById(anyLong());
    }
}
