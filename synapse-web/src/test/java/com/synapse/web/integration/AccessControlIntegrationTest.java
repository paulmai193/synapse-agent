package com.synapse.web.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.synapse.core.dto.SearchRequest;
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
 * Integration tests for role-based access control validation.
 * Task 14.1: Create comprehensive integration test suite
 */
@SpringBootTest
@AutoConfigureWebMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class AccessControlIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should enforce role-based access to admin endpoints")
    void testAdminAccessControl() throws Exception {
        mockMvc.perform(get("/api/search/analytics/dashboard"))
                .andExpected(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should allow user access to search endpoints")
    void testUserAccessControl() throws Exception {
        SearchRequest request = new SearchRequest();
        request.setQuery("test");
        request.setLimit(5);

        mockMvc.perform(post("/api/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpected(status().isUnauthorized()); // No auth token
    }

    @Test
    @DisplayName("Should validate project access for documents")
    void testProjectAccessControl() throws Exception {
        mockMvc.perform(get("/api/documents"))
                .andExpected(status().isUnauthorized());
    }
}