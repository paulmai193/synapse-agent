package com.synapse.data.llm;

import com.synapse.data.config.LLMConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.retry.Retry;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LLMServiceTest {

    @Mock
    private LLMConfig llmConfig;

    @Mock
    private CircuitBreaker circuitBreaker;

    @Mock
    private Retry retry;

    @Mock
    private RateLimiter rateLimiter;

    private MockWebServer mockWebServer;
    private LLMService llmService;
    private OkHttpClient httpClient;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        httpClient = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .build();

        when(llmConfig.getUrl()).thenReturn(mockWebServer.url("").toString().replaceAll("/$", ""));
        when(llmConfig.getModel()).thenReturn("llama3.1:8b");
        when(llmConfig.getMaxTokens()).thenReturn(2048);
        when(llmConfig.getTemperature()).thenReturn(0.7);

        llmService = new LLMService(llmConfig, httpClient, circuitBreaker, retry, rateLimiter);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void testGenerateAnswer_Success() throws Exception {
        // Arrange
        String question = "What is the company policy on remote work?";
        List<String> contextChunks = Arrays.asList(
                "Remote work is allowed for all employees with manager approval.",
                "Employees must maintain regular communication during remote work."
        );
        String language = "en";

        String mockResponse = """
                {
                    "model": "llama3.1:8b",
                    "created_at": "2024-01-01T00:00:00Z",
                    "response": "Based on the company policy, remote work is permitted for all employees provided they obtain manager approval and maintain regular communication.",
                    "done": true,
                    "context": [1, 2, 3],
                    "total_duration": 1000000000,
                    "load_duration": 100000000,
                    "prompt_eval_count": 50,
                    "prompt_eval_duration": 200000000,
                    "eval_count": 30,
                    "eval_duration": 300000000
                }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(mockResponse)
                .addHeader("Content-Type", "application/json"));

        // Act
        CompletableFuture<String> result = llmService.generateAnswer(question, contextChunks, language);
        String answer = result.get(10, TimeUnit.SECONDS);

        // Assert
        assertNotNull(answer);
        assertTrue(answer.contains("remote work"));
        assertTrue(answer.contains("manager approval"));
    }

    @Test
    void testGenerateQAResponse_Success() throws Exception {
        // Arrange
        String question = "How do I submit a vacation request?";
        List<Map<String, Object>> contextChunks = Arrays.asList(
                createContextChunk("Vacation requests must be submitted through the HR portal.", "HR Policy", 0.9),
                createContextChunk("All requests require manager approval within 5 business days.", "Employee Handbook", 0.8)
        );
        String language = "en";

        String mockResponse = """
                {
                    "model": "llama3.1:8b",
                    "created_at": "2024-01-01T00:00:00Z",
                    "response": "To submit a vacation request, you need to use the HR portal. After submission, your manager will review and approve the request within 5 business days.",
                    "done": true,
                    "context": [1, 2, 3],
                    "total_duration": 1000000000,
                    "load_duration": 100000000,
                    "prompt_eval_count": 50,
                    "prompt_eval_duration": 200000000,
                    "eval_count": 30,
                    "eval_duration": 300000000
                }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(mockResponse)
                .addHeader("Content-Type", "application/json"));

        // Act
        CompletableFuture<LLMService.QAResult> result = llmService.generateQAResponse(question, contextChunks, language);
        LLMService.QAResult qaResult = result.get(10, TimeUnit.SECONDS);

        // Assert
        assertNotNull(qaResult);
        assertNotNull(qaResult.getAnswer());
        assertTrue(qaResult.getAnswer().contains("HR portal"));
        assertTrue(qaResult.getConfidence() > 0.5);
        assertEquals(language, qaResult.getLanguage());
        assertEquals(2, qaResult.getSources().size());
    }

    @Test
    void testEnhanceSearchQuery_Success() throws Exception {
        // Arrange
        String query = "vacation policy";
        String language = "en";

        String mockResponse = """
                {
                    "model": "llama3.1:8b",
                    "created_at": "2024-01-01T00:00:00Z",
                    "response": "vacation policy time off leave PTO annual leave holiday request",
                    "done": true,
                    "context": [1, 2, 3],
                    "total_duration": 500000000,
                    "load_duration": 50000000,
                    "prompt_eval_count": 20,
                    "prompt_eval_duration": 100000000,
                    "eval_count": 15,
                    "eval_duration": 150000000
                }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(mockResponse)
                .addHeader("Content-Type", "application/json"));

        // Act
        CompletableFuture<String> result = llmService.enhanceSearchQuery(query, language);
        String enhancedQuery = result.get(10, TimeUnit.SECONDS);

        // Assert
        assertNotNull(enhancedQuery);
        assertTrue(enhancedQuery.contains("vacation"));
        assertTrue(enhancedQuery.contains("PTO") || enhancedQuery.contains("leave"));
    }

    @Test
    void testGenerateResponse_APIError() {
        // Arrange
        String prompt = "Test prompt";

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            llmService.generateResponse(prompt);
        });
    }

    @Test
    void testIsHealthy_Success() {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"models\": []}"));

        // Act
        boolean isHealthy = llmService.isHealthy();

        // Assert
        assertTrue(isHealthy);
    }

    @Test
    void testIsHealthy_Failure() {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500));

        // Act
        boolean isHealthy = llmService.isHealthy();

        // Assert
        assertFalse(isHealthy);
    }

    @Test
    void testGenerateAnswer_EmptyContext() throws Exception {
        // Arrange
        String question = "What is the policy?";
        List<String> contextChunks = Arrays.asList();
        String language = "en";

        String mockResponse = """
                {
                    "model": "llama3.1:8b",
                    "created_at": "2024-01-01T00:00:00Z",
                    "response": "I cannot provide a complete answer to your question.",
                    "done": true,
                    "context": [],
                    "total_duration": 500000000,
                    "load_duration": 50000000,
                    "prompt_eval_count": 10,
                    "prompt_eval_duration": 100000000,
                    "eval_count": 10,
                    "eval_duration": 100000000
                }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(mockResponse)
                .addHeader("Content-Type", "application/json"));

        // Act
        CompletableFuture<String> result = llmService.generateAnswer(question, contextChunks, language);
        String answer = result.get(10, TimeUnit.SECONDS);

        // Assert
        assertNotNull(answer);
        assertTrue(answer.contains("cannot") || answer.contains("not enough"));
    }

    private Map<String, Object> createContextChunk(String content, String documentTitle, double confidence) {
        Map<String, Object> chunk = new HashMap<>();
        chunk.put("content", content);
        chunk.put("documentTitle", documentTitle);
        chunk.put("confidence", confidence);
        chunk.put("documentId", "doc-" + System.currentTimeMillis());
        return chunk;
    }
}