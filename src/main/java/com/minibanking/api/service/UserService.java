package com.minibanking.api.service;

import com.minibanking.api.model.User;

import java.util.List;

/**
 * Service contract defining full CRUD operations for Banking Users.
 */
public interface UserService {
    List<User> getAllUsers();
    User getUserById(Long id);
    User createUser(User user);
    User updateUser(Long id, User user);
    User patchUser(Long id, User partialUser);
    void deleteUser(Long id);
}
