package com.synapse.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.synapse.core.service.UserService;
import com.synapse.data.entity.User;
import com.synapse.web.dto.UserDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for UserController.
 */
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "test@example.com", "hashedpassword");
        testUser.setUserId(UUID.randomUUID());
    }

    @Test
    void shouldRegisterUser() throws Exception {
        UserDto.RegisterRequest request = new UserDto.RegisterRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("password123");

        when(userService.registerUser(anyString(), anyString(), anyString())).thenReturn(testUser);

        mockMvc.perform(post("/users/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @WithMockUser(roles = "SYSTEM_ADMIN")
    void shouldGetAllUsers() throws Exception {
        when(userService.findAllActiveUsers()).thenReturn(Arrays.asList(testUser));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("testuser"));
    }

    @Test
    @WithMockUser(roles = "SYSTEM_ADMIN")
    void shouldAssignRole() throws Exception {
        UserDto.RoleAssignmentRequest request = new UserDto.RoleAssignmentRequest();
        request.setRoleName("ADMIN");

        mockMvc.perform(post("/users/{userId}/roles", testUser.getUserId())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SYSTEM_ADMIN")
    void shouldUpdateUserStatus() throws Exception {
        UserDto.StatusUpdateRequest request = new UserDto.StatusUpdateRequest();
        request.setStatus(User.UserStatus.INACTIVE);

        mockMvc.perform(put("/users/{userId}/status", testUser.getUserId())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SYSTEM_ADMIN")
    void shouldSoftDeleteUser() throws Exception {
        mockMvc.perform(delete("/users/{userId}", testUser.getUserId())
                .with(csrf()))
                .andExpected(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldDenyAccessToUnauthorizedUser() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isForbidden());
    }
}