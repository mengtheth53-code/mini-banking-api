package com.minibanking.api.controller;

import com.minibanking.api.model.User;
import com.minibanking.api.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * REST Controller adhering strictly to RESTful & HTTP standards:
 * - GET    /api/v1/users        -> 200 OK
 * - GET    /api/v1/users/{id}   -> 200 OK (or 404 Not Found)
 * - POST   /api/v1/users        -> 201 Created with Location header
 * - PUT    /api/v1/users/{id}   -> 200 OK (Full Update / Idempotent)
 * - PATCH  /api/v1/users/{id}   -> 200 OK (Partial Update)
 * - DELETE /api/v1/users/{id}   -> 204 No Content (Idempotent)
 */
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User created = userService.createUser(user);

        // Standard REST pattern: Include 'Location' header pointing to new resource URI
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
