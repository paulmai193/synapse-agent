package com.synapse.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.synapse.core.dto.QARequest;
import com.synapse.core.dto.QAResponse;
import com.synapse.core.service.MultilingualQAService;
import com.synapse.security.jwt.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(locations = "classpath:application-test.properties")
class MultilingualQAIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private MultilingualQAService multilingualQAService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private String validToken;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        // Create a valid JWT token for testing
        validToken = jwtUtil.generateToken("testuser@example.com");
    }

    @Test
    void testAskQuestion_EnglishQuery_Success() throws Exception {
        // Arrange
        QARequest request = new QARequest();
        request.setQuestion("What is the company vacation policy?");
        request.setLanguage("en");

        QAResponse mockResponse = new QAResponse();
        mockResponse.setQuestion("What is the company vacation policy?");
        mockResponse.setAnswer("Based on company policy, employees are entitled to 20 days of vacation per year.");
        mockResponse.setLanguage("en");
        mockResponse.setConfidence(0.85f);
        mockResponse.setConversationId("conv-123");
        mockResponse.setResponseTimeMs(1500L);

        when(multilingualQAService.processQuestion(any(QARequest.class), anyLong()))
            .thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(post("/api/search/qa")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.question").value("What is the company vacation policy?"))
                .andExpect(jsonPath("$.answer").value("Based on company policy, employees are entitled to 20 days of vacation per year."))
                .andExpect(jsonPath("$.language").value("en"))
                .andExpect(jsonPath("$.confidence").value(0.85))
                .andExpect(jsonPath("$.conversationId").value("conv-123"))
                .andExpect(jsonPath("$.responseTimeMs").value(1500));
    }

    @Test
    void testAskQuestion_VietnameseQuery_Success() throws Exception {
        // Arrange
        QARequest request = new QARequest();
        request.setQuestion("Chính sách nghỉ phép của công ty là gì?");
        request.setLanguage("vi");

        QAResponse mockResponse = new QAResponse();
        mockResponse.setQuestion("Chính sách nghỉ phép của công ty là gì?");
        mockResponse.setAnswer("Theo chính sách công ty, nhân viên được hưởng 20 ngày nghỉ phép mỗi năm.");
        mockResponse.setLanguage("vi");
        mockResponse.setConfidence(0.82f);
        mockResponse.setConversationId("conv-456");
        mockResponse.setResponseTimeMs(1800L);

        when(multilingualQAService.processQuestion(any(QARequest.class), anyLong()))
            .thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(post("/api/search/qa")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.question").value("Chính sách nghỉ phép của công ty là gì?"))
                .andExpect(jsonPath("$.answer").value("Theo chính sách công ty, nhân viên được hưởng 20 ngày nghỉ phép mỗi năm."))
                .andExpect(jsonPath("$.language").value("vi"))
                .andExpect(jsonPath("$.confidence").value(0.82));
    }

    @Test
    void testAskQuestion_LowConfidenceResponse_Success() throws Exception {
        // Arrange
        QARequest request = new QARequest();
        request.setQuestion("What is the specific procedure for XYZ?");
        request.setLanguage("en");

        QAResponse mockResponse = new QAResponse();
        mockResponse.setQuestion("What is the specific procedure for XYZ?");
        mockResponse.setAnswer("Based on the available documents, I found some related information but cannot provide a complete answer. Suggestions: 1. Try rephrasing your question with different keywords");
        mockResponse.setLanguage("en");
        mockResponse.setConfidence(0.3f);
        mockResponse.setConversationId("conv-789");
        mockResponse.setResponseTimeMs(1200L);

        when(multilingualQAService.processQuestion(any(QARequest.class), anyLong()))
            .thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(post("/api/search/qa")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.question").value("What is the specific procedure for XYZ?"))
                .andExpect(jsonPath("$.answer").value(containsString("Suggestions")))
                .andExpect(jsonPath("$.language").value("en"))
                .andExpect(jsonPath("$.confidence").value(0.3));
    }

    @Test
    void testAskQuestion_UnauthorizedAccess_Forbidden() throws Exception {
        // Arrange
        QARequest request = new QARequest();
        request.setQuestion("What is the company policy?");
        request.setLanguage("en");

        // Act & Assert
        mockMvc.perform(post("/api/search/qa")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testAskQuestion_InvalidToken_Unauthorized() throws Exception {
        // Arrange
        QARequest request = new QARequest();
        request.setQuestion("What is the company policy?");
        request.setLanguage("en");

        // Act & Assert
        mockMvc.perform(post("/api/search/qa")
                .header("Authorization", "Bearer invalid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testSubmitQAFeedback_Success() throws Exception {
        // Arrange
        SearchController.QAFeedbackRequest feedbackRequest = new SearchController.QAFeedbackRequest();
        feedbackRequest.setConversationId("conv-123");
        feedbackRequest.setHelpful(true);
        feedbackRequest.setRating(4);
        feedbackRequest.setComment("Very helpful answer");

        // Act & Assert
        mockMvc.perform(post("/api/search/qa/feedback")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(feedbackRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void testSubmitQAFeedback_UnauthorizedAccess_Forbidden() throws Exception {
        // Arrange
        SearchController.QAFeedbackRequest feedbackRequest = new SearchController.QAFeedbackRequest();
        feedbackRequest.setConversationId("conv-123");
        feedbackRequest.setHelpful(false);
        feedbackRequest.setRating(2);

        // Act & Assert
        mockMvc.perform(post("/api/search/qa/feedback")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(feedbackRequest)))
                .andExpect(status().isUnauthorized());
    }
}