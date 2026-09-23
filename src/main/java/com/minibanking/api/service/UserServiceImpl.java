package com.minibanking.api.service;

import com.minibanking.api.exception.DuplicateResourceException;
import com.minibanking.api.exception.ResourceNotFoundException;
import com.minibanking.api.model.User;
import com.minibanking.api.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service implementation containing Business Logic and validation for Bank Users.
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

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
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    @Override
    public User createUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new DuplicateResourceException("Email is already registered: " + user.getEmail());
        }

        if (user.getRole() == null || user.getRole().isBlank()) {
            user.setRole("USER");
        }

        return userRepository.save(user);
    }

    @Override
    public User updateUser(Long id, User incoming) {
        User existing = getUserById(id);

        // Check email uniqueness if email has changed
        if (!existing.getEmail().equalsIgnoreCase(incoming.getEmail()) &&
                userRepository.existsByEmail(incoming.getEmail())) {
            throw new DuplicateResourceException("Email is already registered: " + incoming.getEmail());
        }

        existing.setFullName(incoming.getFullName());
        existing.setEmail(incoming.getEmail());
        if (incoming.getRole() != null && !incoming.getRole().isBlank()) {
            existing.setRole(incoming.getRole());
        }

        return userRepository.save(existing);
    }

    @Override
    public User patchUser(Long id, User partial) {
        User existing = getUserById(id);

        if (partial.getFullName() != null && !partial.getFullName().isBlank()) {
            existing.setFullName(partial.getFullName());
        }

        if (partial.getEmail() != null && !partial.getEmail().isBlank()) {
            if (!existing.getEmail().equalsIgnoreCase(partial.getEmail()) &&
                    userRepository.existsByEmail(partial.getEmail())) {
                throw new DuplicateResourceException("Email is already registered: " + partial.getEmail());
            }
            existing.setEmail(partial.getEmail());
        }

        if (partial.getRole() != null && !partial.getRole().isBlank()) {
            existing.setRole(partial.getRole());
        }

        return userRepository.save(existing);
    }

    @Override
    public void deleteUser(Long id) {
        if (userRepository.findById(id).isEmpty()) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }
}
