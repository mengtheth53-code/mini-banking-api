package com.minibanking.api.controller;

import com.minibanking.api.model.User;
import com.minibanking.api.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/**
 * REST Controller adhering strictly to RESTful, HTTP & Pagination standards:
 * - GET    /api/v1/users?page=0&size=10&sort=id,asc -> 200 OK with Page<User>
 * - GET    /api/v1/users/{id}                       -> 200 OK (or 404 Not Found)
 * - POST   /api/v1/users                            -> 201 Created with Location header
 * - PUT    /api/v1/users/{id}                       -> 200 OK (Full Update / Idempotent)
 * - PATCH  /api/v1/users/{id}                       -> 200 OK (Partial Update)
 * - DELETE /api/v1/users/{id}                       -> 204 No Content (Idempotent)
 */
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Paginated user list.
     * Prevents OutOfMemoryError in production when user table has millions of rows.
     */
    @GetMapping
    public ResponseEntity<Page<User>> getAllUsers(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<User> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User created = userService.createUser(user);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();

        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        User updated = userService.updateUser(id, user);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<User> patchUser(@PathVariable Long id, @RequestBody User partialUser) {
        User patched = userService.patchUser(id, partialUser);
        return ResponseEntity.ok(patched);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
