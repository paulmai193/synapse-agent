package com.synapse.core.service;

import com.synapse.core.dto.*;
import com.synapse.data.llm.LLMService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MultilingualQAServiceTest {

    @Mock
    private SearchService searchService;

    @Mock
    private LLMService llmService;

    @Mock
    private RestTemplate restTemplate;

    private MultilingualQAService multilingualQAService;

    @BeforeEach
    void setUp() {
        multilingualQAService = new MultilingualQAService();
        // Use reflection to inject mocked dependencies
        setField(multilingualQAService, "searchService", searchService);
        setField(multilingualQAService, "llmService", llmService);
        setField(multilingualQAService, "restTemplate", restTemplate);
    }

    @Test
    void testProcessQuestion_EnglishQuery_Success() throws Exception {
        // Arrange
        QARequest request = new QARequest();
        request.setQuestion("What is the company vacation policy?");
        request.setLanguage("en");
        Long userId = 1L;

        // Mock language detection
        Map<String, Object> languageResponse = Map.of("language", "en");
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
            .thenReturn(languageResponse);

        // Mock search results
        SearchResponse searchResponse = createMockSearchResponse();
        when(searchService.search(any(SearchRequest.class), eq(userId)))
            .thenReturn(searchResponse);

        // Mock LLM response
        LLMService.QAResult qaResult = new LLMService.QAResult(
            "Based on company policy, employees are entitled to 20 days of vacation per year.",
            createMockContextChunks(),
            0.85,
            "en"
        );
        when(llmService.generateQAResponse(anyString(), anyList(), eq("en")))
            .thenReturn(CompletableFuture.completedFuture(qaResult));

        // Act
        QAResponse response = multilingualQAService.processQuestion(request, userId);

        // Assert
        assertNotNull(response);
        assertEquals("What is the company vacation policy?", response.getQuestion());
        assertTrue(response.getAnswer().contains("vacation"));
        assertEquals("en", response.getLanguage());
        assertTrue(response.getConfidence() > 0.8f);
        assertNotNull(response.getConversationId());
        assertTrue(response.getResponseTimeMs() > 0);
    }

    @Test
    void testProcessQuestion_VietnameseQuery_Success() throws Exception {
        // Arrange
        QARequest request = new QARequest();
        request.setQuestion("Chính sách nghỉ phép của công ty là gì?");
        Long userId = 1L;

        // Mock language detection
        Map<String, Object> languageResponse = Map.of("language", "vi");
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
            .thenReturn(languageResponse);

        // Mock search results
        SearchResponse searchResponse = createMockSearchResponse();
        when(searchService.search(any(SearchRequest.class), eq(userId)))
            .thenReturn(searchResponse);

        // Mock LLM response in Vietnamese
        LLMService.QAResult qaResult = new LLMService.QAResult(
            "Theo chính sách công ty, nhân viên được hưởng 20 ngày nghỉ phép mỗi năm.",
            createMockContextChunks(),
            0.82,
            "vi"
        );
        when(llmService.generateQAResponse(anyString(), anyList(), eq("vi")))
            .thenReturn(CompletableFuture.completedFuture(qaResult));

        // Act
        QAResponse response = multilingualQAService.processQuestion(request, userId);

        // Assert
        assertNotNull(response);
        assertEquals("Chính sách nghỉ phép của công ty là gì?", response.getQuestion());
        assertTrue(response.getAnswer().contains("nghỉ phép"));
        assertEquals("vi", response.getLanguage());
        assertTrue(response.getConfidence() > 0.8f);
    }

    @Test
    void testProcessQuestion_LowConfidence_UncertaintyHandling() throws Exception {
        // Arrange
        QARequest request = new QARequest();
        request.setQuestion("What is the specific procedure for XYZ?");
        request.setLanguage("en");
        Long userId = 1L;

        // Mock language detection
        Map<String, Object> languageResponse = Map.of("language", "en");
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
            .thenReturn(languageResponse);

        // Mock limited search results
        SearchResponse searchResponse = createLimitedSearchResponse();
        when(searchService.search(any(SearchRequest.class), eq(userId)))
            .thenReturn(searchResponse);

        // Mock low-confidence LLM response
        LLMService.QAResult qaResult = new LLMService.QAResult(
            "I cannot provide a complete answer based on available information.",
            createLimitedContextChunks(),
            0.4, // Low confidence
            "en"
        );
        when(llmService.generateQAResponse(anyString(), anyList(), eq("en")))
            .thenReturn(CompletableFuture.completedFuture(qaResult));

        // Act
        QAResponse response = multilingualQAService.processQuestion(request, userId);

        // Assert
        assertNotNull(response);
        assertEquals("What is the specific procedure for XYZ?", response.getQuestion());
        assertTrue(response.getAnswer().contains("information") || 
                  response.getAnswer().contains("Suggestions"));
        assertEquals("en", response.getLanguage());
        assertTrue(response.getConfidence() < 0.6f); // Should trigger uncertainty handling
    }

    @Test
    void testProcessQuestion_NoSearchResults_UncertaintyHandling() throws Exception {
        // Arrange
        QARequest request = new QARequest();
        request.setQuestion("What is the policy for something that doesn't exist?");
        request.setLanguage("en");
        Long userId = 1L;

        // Mock language detection
        Map<String, Object> languageResponse = Map.of("language", "en");
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
            .thenReturn(languageResponse);

        // Mock empty search results
        SearchResponse emptySearchResponse = new SearchResponse("test", Collections.emptyList(), 0, 100L);
        when(searchService.search(any(SearchRequest.class), eq(userId)))
            .thenReturn(emptySearchResponse);

        // Mock LLM response for no context
        LLMService.QAResult qaResult = new LLMService.QAResult(
            "I couldn't find relevant information to answer your question.",
            Collections.emptyList(),
            0.1,
            "en"
        );
        when(llmService.generateQAResponse(anyString(), anyList(), eq("en")))
            .thenReturn(CompletableFuture.completedFuture(qaResult));

        // Act
        QAResponse response = multilingualQAService.processQuestion(request, userId);

        // Assert
        assertNotNull(response);
        assertTrue(response.getAnswer().contains("couldn't find") || 
                  response.getAnswer().contains("relevant information"));
        assertTrue(response.getConfidence() < 0.6f);
        assertTrue(response.getSources().isEmpty());
    }

    @Test
    void testProcessQuestion_LanguageDetectionFails_UsesDefault() throws Exception {
        // Arrange
        QARequest request = new QARequest();
        request.setQuestion("What is the policy?");
        request.setLanguage("en");
        Long userId = 1L;

        // Mock language detection failure
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
            .thenThrow(new RuntimeException("Language detection service unavailable"));

        // Mock search results
        SearchResponse searchResponse = createMockSearchResponse();
        when(searchService.search(any(SearchRequest.class), eq(userId)))
            .thenReturn(searchResponse);

        // Mock LLM response
        LLMService.QAResult qaResult = new LLMService.QAResult(
            "Based on available information...",
            createMockContextChunks(),
            0.75,
            "en"
        );
        when(llmService.generateQAResponse(anyString(), anyList(), eq("en")))
            .thenReturn(CompletableFuture.completedFuture(qaResult));

        // Act
        QAResponse response = multilingualQAService.processQuestion(request, userId);

        // Assert
        assertNotNull(response);
        assertEquals("en", response.getLanguage()); // Should fall back to request language
        assertTrue(response.getConfidence() > 0.6f);
    }

    private SearchResponse createMockSearchResponse() {
        List<SearchResult> results = Arrays.asList(
            createSearchResult("doc1", "Company Policy", "Vacation policy details...", 0.9f),
            createSearchResult("doc2", "HR Handbook", "Employee benefits information...", 0.8f)
        );
        return new SearchResponse("vacation policy", results, results.size(), 150L);
    }

    private SearchResponse createLimitedSearchResponse() {
        List<SearchResult> results = Arrays.asList(
            createSearchResult("doc1", "General Info", "Some general information...", 0.5f)
        );
        return new SearchResponse("XYZ procedure", results, results.size(), 120L);
    }

    private SearchResult createSearchResult(String docId, String title, String content, float score) {
        SearchResult result = new SearchResult();
        result.setDocumentId(docId);
        result.setTitle(title);
        result.setContent(content);
        result.setRelevanceScore(score);
        result.setSource("upload");
        result.setProjectId(1L);
        return result;
    }

    private List<Map<String, Object>> createMockContextChunks() {
        List<Map<String, Object>> chunks = new ArrayList<>();
        
        Map<String, Object> chunk1 = new HashMap<>();
        chunk1.put("content", "Vacation policy details...");
        chunk1.put("documentTitle", "Company Policy");
        chunk1.put("confidence", 0.9);
        chunks.add(chunk1);
        
        Map<String, Object> chunk2 = new HashMap<>();
        chunk2.put("content", "Employee benefits information...");
        chunk2.put("documentTitle", "HR Handbook");
        chunk2.put("confidence", 0.8);
        chunks.add(chunk2);
        
        return chunks;
    }

    private List<Map<String, Object>> createLimitedContextChunks() {
        List<Map<String, Object>> chunks = new ArrayList<>();
        
        Map<String, Object> chunk1 = new HashMap<>();
        chunk1.put("content", "Some general information...");
        chunk1.put("documentTitle", "General Info");
        chunk1.put("confidence", 0.5);
        chunks.add(chunk1);
        
        return chunks;
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }
}