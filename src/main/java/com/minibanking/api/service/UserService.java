package com.minibanking.api.service;

import com.minibanking.api.model.User;

import java.util.List;

/**
 * Service contract defining banking user business operations.
 */
public interface UserService {
    List<User> getAllUsers();
    User getUserById(Long id);
    User createUser(User user);
    void deleteUser(Long id);
}
