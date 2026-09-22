package com.minibanking.api.service;

import com.minibanking.api.model.User;
import com.minibanking.api.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Service implementation containing Business Logic for Bank Users.
 * Uses Constructor Injection to inject UserRepository.
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    // Constructor Injection (Industry Best Practice)
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found with id: " + id));
    }

    @Override
    public User createUser(User user) {
        // Business Rule 1: Email must be unique
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email is already registered: " + user.getEmail());
        }

        // Business Rule 2: Set default role to USER if missing
        if (user.getRole() == null || user.getRole().isBlank()) {
            user.setRole("USER");
        }

        return userRepository.save(user);
    }

    @Override
    public void deleteUser(Long id) {
        if (userRepository.findById(id).isEmpty()) {
            throw new NoSuchElementException("Cannot delete. User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }
}
