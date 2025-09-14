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
 * Integration tests for search and Q&A functionality.
 * Task 14.1: Create comprehensive integration test suite
 */
@SpringBootTest
@AutoConfigureWebMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class SearchIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should perform search successfully")
    void testSearch() throws Exception {
        SearchRequest request = new SearchRequest();
        request.setQuery("test query");
        request.setLimit(10);

        mockMvc.perform(post("/api/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpected(status().isOk())
                .andExpected(jsonPath("$.query").value("test query"))
                .andExpected(jsonPath("$.results").isArray());
    }

    @Test
    @DisplayName("Should handle multilingual search")
    void testMultilingualSearch() throws Exception {
        SearchRequest request = new SearchRequest();
        request.setQuery("tìm kiếm");
        request.setLanguage("vi");
        request.setLimit(5);

        mockMvc.perform(post("/api/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpected(status().isOk())
                .andExpected(jsonPath("$.language").value("vi"));
    }

    @Test
    @DisplayName("Should get search analytics")
    void testSearchAnalytics() throws Exception {
        mockMvc.perform(get("/api/search/analytics/dashboard")
                .param("days", "7"))
                .andExpected(status().isOk())
                .andExpected(jsonPath("$.totalSearches").exists());
    }
}