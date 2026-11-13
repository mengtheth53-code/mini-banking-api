package com.minibanking.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minibanking.api.exception.DuplicateResourceException;
import com.minibanking.api.exception.ResourceNotFoundException;
import com.minibanking.api.model.User;
import com.minibanking.api.security.CustomUserDetailsService;
import com.minibanking.api.security.jwt.JwtService;
import com.minibanking.api.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser(roles = "ADMIN")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @Test
    @DisplayName("GET /api/v1/users should return HTTP 200 OK with paginated users")
    void getAllUsers_Returns200() throws Exception {
        User user = new User(1L, "Alice Smith", "alice@bank.com", "hash", "ROLE_USER", Instant.now());
        Page<User> page = new PageImpl<>(List.of(user), PageRequest.of(0, 10), 1);
        when(userService.getAllUsers(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/users?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].fullName").value("Alice Smith"))
                .andExpect(jsonPath("$.content[0].email").value("alice@bank.com"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/users/{id} should return HTTP 200 OK when found")
    void getUserById_Found_Returns200() throws Exception {
        User user = new User(1L, "Alice Smith", "alice@bank.com", "hash", "ROLE_USER", Instant.now());
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
    @DisplayName("DELETE /api/v1/users/{id} should return HTTP 204 No Content")
    void deleteUser_Returns204() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/api/v1/users/1"))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUser(1L);
    }
}
