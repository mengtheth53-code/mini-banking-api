package com.minibanking.api.service;

import com.minibanking.api.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service contract defining full CRUD operations and Pagination for Banking Users.
 */
public interface UserService {
    List<User> getAllUsers();
    Page<User> getAllUsers(Pageable pageable);
    User getUserById(Long id);
    User createUser(User user);
    User updateUser(Long id, User user);
    User patchUser(Long id, User partialUser);
    void deleteUser(Long id);
}
