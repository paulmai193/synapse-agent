package com.synapse.data.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.synapse.data.config.LLMConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.retry.Retry;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@Service
public class LLMService {
    
    private static final Logger logger = LoggerFactory.getLogger(LLMService.class);
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    private final LLMConfig llmConfig;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final CircuitBreaker circuitBreaker;
    private final Retry retry;
    private final RateLimiter rateLimiter;
    
    @Autowired
    public LLMService(LLMConfig llmConfig, 
                     OkHttpClient httpClient,
                     CircuitBreaker circuitBreaker,
                     Retry retry,
                     RateLimiter rateLimiter) {
        this.llmConfig = llmConfig;
        this.httpClient = httpClient;
        this.objectMapper = new ObjectMapper();
        this.circuitBreaker = circuitBreaker;
        this.retry = retry;
        this.rateLimiter = rateLimiter;
    }
    
    public CompletableFuture<String> generateAnswer(String question, List<String> contextChunks, String language) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String prompt = PromptTemplate.buildQAPrompt(question, contextChunks, language);
                return generateResponse(prompt);
            } catch (Exception e) {
                logger.error("Error generating answer for question: {}", question, e);
                return PromptTemplate.buildUncertaintyResponse(contextChunks);
            }
        });
    }
    
    public CompletableFuture<String> enhanceSearchQuery(String query, String language) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String prompt = PromptTemplate.buildSearchEnhancementPrompt(query, language);
                return generateResponse(prompt);
            } catch (Exception e) {
                logger.error("Error enhancing search query: {}", query, e);
                return query; // Return original query if enhancement fails
            }
        });
    }
    
    public String generateResponse(String prompt) {
        Supplier<String> decoratedSupplier = CircuitBreaker
                .decorateSupplier(circuitBreaker, () -> 
                    RateLimiter.decorateSupplier(rateLimiter, () ->
                        Retry.decorateSupplier(retry, () -> callOllamaAPI(prompt))
                    ).get()
                );
        
        return decoratedSupplier.get();
    }
    
    private String callOllamaAPI(String prompt) {
        try {
            LLMRequest request = new LLMRequest(llmConfig.getModel(), prompt, null);
            request.getOptions().setTemperature(llmConfig.getTemperature());
            request.getOptions().setNumPredict(llmConfig.getMaxTokens());
            
            String jsonRequest = objectMapper.writeValueAsString(request);
            
            RequestBody body = RequestBody.create(jsonRequest, JSON);
            Request httpRequest = new Request.Builder()
                    .url(llmConfig.getUrl() + "/api/generate")
                    .post(body)
                    .build();
            
            logger.debug("Calling Ollama API with prompt length: {}", prompt.length());
            
            try (Response response = httpClient.newCall(httpRequest).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected response code: " + response.code());
                }
                
                String responseBody = response.body().string();
                LLMResponse llmResponse = objectMapper.readValue(responseBody, LLMResponse.class);
                
                logger.debug("Ollama API response received, eval_count: {}, total_duration: {}ms", 
                           llmResponse.getEvalCount(), llmResponse.getTotalDuration() / 1_000_000);
                
                return llmResponse.getResponse();
            }
            
        } catch (IOException e) {
            logger.error("Error calling Ollama API", e);
            throw new RuntimeException("Failed to generate LLM response", e);
        }
    }
    
    public CompletableFuture<QAResult> generateQAResponse(String question, 
                                                         List<Map<String, Object>> contextChunks, 
                                                         String language) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String contextWithCitations = PromptTemplate.buildContextWithCitations(contextChunks);
                String prompt = PromptTemplate.buildQAPrompt(question, 
                                                           List.of(contextWithCitations), 
                                                           language);
                
                String response = generateResponse(prompt);
                
                // Calculate confidence based on context quality and response length
                double confidence = calculateConfidence(contextChunks, response);
                
                return new QAResult(response, contextChunks, confidence, language);
                
            } catch (Exception e) {
                logger.error("Error generating Q&A response for question: {}", question, e);
                
                // Return uncertainty response with low confidence
                String uncertaintyResponse = PromptTemplate.buildUncertaintyResponse(
                    contextChunks.stream()
                        .map(chunk -> (String) chunk.get("content"))
                        .toList()
                );
                
                return new QAResult(uncertaintyResponse, contextChunks, 0.1, language);
            }
        });
    }
    
    private double calculateConfidence(List<Map<String, Object>> contextChunks, String response) {
        if (contextChunks.isEmpty()) {
            return 0.1;
        }
        
        // Base confidence on average context chunk confidence
        double avgContextConfidence = contextChunks.stream()
                .mapToDouble(chunk -> (Double) chunk.getOrDefault("confidence", 0.5))
                .average()
                .orElse(0.5);
        
        // Adjust based on response characteristics
        double responseQualityFactor = 1.0;
        
        if (response.contains("I cannot") || response.contains("not enough information")) {
            responseQualityFactor = 0.3;
        } else if (response.length() < 50) {
            responseQualityFactor = 0.6;
        } else if (response.length() > 200) {
            responseQualityFactor = 1.0;
        }
        
        return Math.min(0.95, avgContextConfidence * responseQualityFactor);
    }
    
    public boolean isHealthy() {
        try {
            Request request = new Request.Builder()
                    .url(llmConfig.getUrl() + "/api/tags")
                    .get()
                    .build();
            
            try (Response response = httpClient.newCall(request).execute()) {
                return response.isSuccessful();
            }
        } catch (Exception e) {
            logger.warn("Ollama health check failed", e);
            return false;
        }
    }
    
    public static class QAResult {
        private final String answer;
        private final List<Map<String, Object>> sources;
        private final double confidence;
        private final String language;
        
        public QAResult(String answer, List<Map<String, Object>> sources, double confidence, String language) {
            this.answer = answer;
            this.sources = sources;
            this.confidence = confidence;
            this.language = language;
        }
        
        public String getAnswer() {
            return answer;
        }
        
        public List<Map<String, Object>> getSources() {
            return sources;
        }
        
        public double getConfidence() {
            return confidence;
        }
        
        public String getLanguage() {
            return language;
        }
    }
}