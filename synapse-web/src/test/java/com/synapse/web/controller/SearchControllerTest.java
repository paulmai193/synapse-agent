package com.synapse.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.synapse.core.dto.*;
import com.synapse.core.service.SearchService;
import com.synapse.core.service.QAService;
import com.synapse.core.service.AuditService;
import com.synapse.security.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SearchController.class)
class SearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SearchService searchService;

    @MockBean
    private QAService qaService;

    @MockBean
    private AuditService auditService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Mock SecurityUtils
        mockStatic(SecurityUtils.class);
        when(SecurityUtils.getCurrentUserId()).thenReturn(1L);
    }

    @Test
    @WithMockUser(roles = "USER")
    void testSearch() throws Exception {
        SearchRequest request = new SearchRequest();
        request.setQuery("test query");
        request.setLimit(10);

        SearchResponse response = new SearchResponse("test query", Collections.emptyList(), 0, 100);
        when(searchService.search(any(SearchRequest.class), anyLong())).thenReturn(response);

        mockMvc.perform(post("/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.query").value("test query"))
                .andExpect(jsonPath("$.totalResults").value(0));

        verify(searchService).search(any(SearchRequest.class), eq(1L));
        verify(auditService).logActivity(eq(1L), eq("SEARCH"), anyString(), anyString());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testSearchSuggestions() throws Exception {
        mockMvc.perform(get("/search/suggestions")
                .param("query", "test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testSubmitFeedback() throws Exception {
        SearchFeedbackRequest request = new SearchFeedbackRequest();
        request.setQuery("test query");
        request.setResultId("result-1");
        request.setRating(5);
        request.setHelpful(true);

        mockMvc.perform(post("/search/feedback")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(auditService).logActivity(eq(1L), eq("SEARCH_FEEDBACK"), anyString(), anyString());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testAskQuestion() throws Exception {
        QARequest request = new QARequest();
        request.setQuestion("What is this about?");

        QAResponse response = new QAResponse();
        response.setQuestion("What is this about?");
        response.setAnswer("This is a test answer");
        response.setConfidence(0.8f);

        when(qaService.answerQuestion(any(QARequest.class), anyLong())).thenReturn(response);

        mockMvc.perform(post("/search/qa")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.question").value("What is this about?"))
                .andExpect(jsonPath("$.answer").value("This is a test answer"))
                .andExpect(jsonPath("$.confidence").value(0.8));

        verify(qaService).answerQuestion(any(QARequest.class), eq(1L));
        verify(auditService).logActivity(eq(1L), eq("QA_QUERY"), anyString(), anyString());
    }
}