package com.minibanking.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minibanking.api.exception.DuplicateResourceException;
import com.minibanking.api.exception.ResourceNotFoundException;
import com.minibanking.api.model.User;
import com.minibanking.api.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    @DisplayName("GET /api/v1/users should return HTTP 200 OK")
    void getAllUsers_Returns200() throws Exception {
        User user = new User(1L, "Alice Smith", "alice@bank.com", "USER", Instant.now());
        when(userService.getAllUsers()).thenReturn(List.of(user));

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fullName").value("Alice Smith"))
                .andExpect(jsonPath("$[0].email").value("alice@bank.com"));
    }

    @Test
    @DisplayName("GET /api/v1/users/{id} should return HTTP 200 OK when found")
    void getUserById_Found_Returns200() throws Exception {
        User user = new User(1L, "Alice Smith", "alice@bank.com", "USER", Instant.now());
        when(userService.getUserById(1L)).thenReturn(user);

        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.email").value("alice@bank.com"));
    }

    @Test
    @DisplayName("GET /api/v1/users/{id} should return HTTP 404 Not Found when ID does not exist")
    void getUserById_NotFound_Returns404() throws Exception {
        when(userService.getUserById(999L)).thenThrow(new ResourceNotFoundException("User not found"));

        mockMvc.perform(get("/api/v1/users/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/v1/users should return HTTP 201 Created with Location header")
    void createUser_Returns201_WithLocationHeader() throws Exception {
        User requestUser = new User(null, "Bob Jones", "bob@bank.com", "USER", null);
        User createdUser = new User(2L, "Bob Jones", "bob@bank.com", "USER", Instant.now());

        when(userService.createUser(any(User.class))).thenReturn(createdUser);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestUser)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/v1/users/2")))
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.email").value("bob@bank.com"));
    }

    @Test
    @DisplayName("POST /api/v1/users should return HTTP 409 Conflict when email is duplicate")
    void createUser_DuplicateEmail_Returns409() throws Exception {
        User duplicateUser = new User(null, "Existing", "exist@bank.com", "USER", null);
        when(userService.createUser(any(User.class)))
                .thenThrow(new DuplicateResourceException("Email already exists"));

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateUser)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PUT /api/v1/users/{id} should return HTTP 200 OK with updated entity")
    void updateUser_Returns200() throws Exception {
        User updatePayload = new User(null, "Alice Updated", "alice.new@bank.com", "ADMIN", null);
        User updatedUser = new User(1L, "Alice Updated", "alice.new@bank.com", "ADMIN", Instant.now());

        when(userService.updateUser(eq(1L), any(User.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/api/v1/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Alice Updated"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    @DisplayName("PATCH /api/v1/users/{id} should return HTTP 200 OK with patched entity")
    void patchUser_Returns200() throws Exception {
        User patchPayload = new User();
        patchPayload.setFullName("Alice Patched");

        User patchedUser = new User(1L, "Alice Patched", "alice@bank.com", "USER", Instant.now());
        when(userService.patchUser(eq(1L), any(User.class))).thenReturn(patchedUser);

        mockMvc.perform(patch("/api/v1/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Alice Patched"));
    }

    @Test
    @DisplayName("DELETE /api/v1/users/{id} should return HTTP 204 No Content")
    void deleteUser_Returns204() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/api/v1/users/1"))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUser(1L);
    }
}
