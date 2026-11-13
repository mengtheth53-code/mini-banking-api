package com.minibanking.api.controller;

import com.minibanking.api.dto.response.UserResponse;
import com.minibanking.api.model.User;
import com.minibanking.api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User Management", description = "User management endpoints with Role-Based Access Control")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get paginated user directory (ADMIN role only)")
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<User> users = userService.getAllUsers(pageable);
        Page<UserResponse> responses = users.map(u -> new UserResponse(
                u.getId(), u.getFullName(), u.getEmail(), u.getRole(), u.getCreatedAt()
        ));
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    @Operation(summary = "Get user details by ID (Admin or account owner)")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        User u = userService.getUserById(id);
        return ResponseEntity.ok(new UserResponse(
                u.getId(), u.getFullName(), u.getEmail(), u.getRole(), u.getCreatedAt()
        ));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Soft delete a user by ID (ADMIN role only)")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
