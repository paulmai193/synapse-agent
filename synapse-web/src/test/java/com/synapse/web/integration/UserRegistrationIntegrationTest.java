package com.synapse.web.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.synapse.web.dto.UserDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for user registration and authentication flows.
 * Task 14.1: Create comprehensive integration test suite
 */
@SpringBootTest
@AutoConfigureWebMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class UserRegistrationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should register new user successfully")
    void testUserRegistration() throws Exception {
        UserDto.RegisterRequest request = new UserDto.RegisterRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpected(status().isCreated())
                .andExpected(jsonPath("$.username").value("testuser"));
    }

    @Test
    @DisplayName("Should login with valid credentials")
    void testUserLogin() throws Exception {
        String loginJson = "{\"username\":\"admin\",\"password\":\"admin123\"}";
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpected(status().isOk())
                .andExpected(jsonPath("$.token").exists());
    }
}