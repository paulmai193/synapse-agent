package com.synapse.core.service;

import com.synapse.core.dto.*;
import com.synapse.data.llm.LLMService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class QAService {

    private static final Logger logger = LoggerFactory.getLogger(QAService.class);

    @Autowired
    private SearchService searchService;
    
    @Autowired
    private LLMService llmService;

    public QAResponse answerQuestion(QARequest request, Long userId) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Convert Q&A request to search request
            SearchRequest searchRequest = new SearchRequest();
            searchRequest.setQuery(request.getQuestion());
            searchRequest.setLimit(5); // Get top 5 relevant documents for context
            searchRequest.setLanguage(request.getLanguage());
            searchRequest.setProjectId(request.getProjectId());
            searchRequest.setDepartmentId(request.getDepartmentId());

            // Search for relevant context
            SearchResponse searchResponse = searchService.search(searchRequest, userId);

            // Generate answer using LLM service
            List<Map<String, Object>> contextChunks = prepareContextChunks(searchResponse);
            
            CompletableFuture<LLMService.QAResult> qaResultFuture = llmService.generateQAResponse(
                request.getQuestion(), 
                contextChunks, 
                request.getLanguage()
            );
            
            // Wait for LLM response with timeout
            LLMService.QAResult qaResult = qaResultFuture.get(30, TimeUnit.SECONDS);
            
            String answer = qaResult.getAnswer();
            Float confidence = (float) qaResult.getConfidence();

            // Create response
            QAResponse response = new QAResponse();
            response.setQuestion(request.getQuestion());
            response.setAnswer(answer);
            response.setConversationId(getOrCreateConversationId(request.getConversationId()));
            response.setConfidence(confidence);
            response.setSources(searchResponse.getResults());
            response.setResponseTimeMs(System.currentTimeMillis() - startTime);
            response.setLanguage(request.getLanguage());

            logger.info("Q&A completed: question='{}', confidence={}, sources={}", 
                request.getQuestion(), confidence, searchResponse.getResults().size());

            return response;

        } catch (Exception e) {
            logger.error("Q&A failed for question: {}", request.getQuestion(), e);
            
            // Try fallback answer generation if LLM fails
            try {
                SearchRequest searchRequest = new SearchRequest();
                searchRequest.setQuery(request.getQuestion());
                searchRequest.setLimit(5);
                searchRequest.setLanguage(request.getLanguage());
                searchRequest.setProjectId(request.getProjectId());
                searchRequest.setDepartmentId(request.getDepartmentId());
                
                SearchResponse searchResponse = searchService.search(searchRequest, userId);
                String fallbackAnswer = generateFallbackAnswer(request.getQuestion(), searchResponse);
                Float fallbackConfidence = calculateConfidence(searchResponse);
                
                QAResponse response = new QAResponse();
                response.setQuestion(request.getQuestion());
                response.setAnswer(fallbackAnswer);
                response.setConversationId(getOrCreateConversationId(request.getConversationId()));
                response.setConfidence(fallbackConfidence * 0.5f); // Reduce confidence for fallback
                response.setSources(searchResponse.getResults());
                response.setResponseTimeMs(System.currentTimeMillis() - startTime);
                response.setLanguage(request.getLanguage());
                
                return response;
                
            } catch (Exception fallbackException) {
                logger.error("Fallback Q&A also failed", fallbackException);
                return createErrorResponse(request, startTime);
            }
        }
    }

    private List<Map<String, Object>> prepareContextChunks(SearchResponse searchResponse) {
        List<Map<String, Object>> contextChunks = new ArrayList<>();
        
        for (SearchResult result : searchResponse.getResults()) {
            Map<String, Object> chunk = new HashMap<>();
            chunk.put("content", result.getContent());
            chunk.put("documentTitle", result.getTitle());
            chunk.put("documentId", result.getDocumentId());
            chunk.put("confidence", (double) result.getRelevanceScore());
            chunk.put("source", result.getSource());
            contextChunks.add(chunk);
        }
        
        return contextChunks;
    }
    
    private String generateFallbackAnswer(String question, SearchResponse searchResponse) {
        if (searchResponse.getResults().isEmpty()) {
            return "I couldn't find relevant information to answer your question. Please try rephrasing your question or check if you have access to the relevant documents.";
        }

        // Fallback answer generation when LLM is unavailable
        StringBuilder answer = new StringBuilder();
        answer.append("Based on the available documents, here's what I found:\n\n");
        
        searchResponse.getResults().stream()
            .limit(3) // Use top 3 results
            .forEach(result -> {
                answer.append("• ").append(result.getContent().substring(0, 
                    Math.min(200, result.getContent().length()))).append("...\n");
            });

        answer.append("\nThis information comes from ")
            .append(searchResponse.getResults().size())
            .append(" relevant document(s).");

        return answer.toString();
    }

    private Float calculateConfidence(SearchResponse searchResponse) {
        if (searchResponse.getResults().isEmpty()) {
            return 0.0f;
        }

        // Calculate average relevance score as confidence
        float avgScore = (float) searchResponse.getResults().stream()
            .mapToDouble(result -> result.getRelevanceScore())
            .average()
            .orElse(0.0);

        return Math.min(avgScore, 1.0f);
    }

    private String getOrCreateConversationId(String existingId) {
        return existingId != null ? existingId : UUID.randomUUID().toString();
    }

    private QAResponse createErrorResponse(QARequest request, long startTime) {
        QAResponse response = new QAResponse();
        response.setQuestion(request.getQuestion());
        response.setAnswer("I'm sorry, I encountered an error while processing your question. Please try again later.");
        response.setConversationId(getOrCreateConversationId(request.getConversationId()));
        response.setConfidence(0.0f);
        response.setResponseTimeMs(System.currentTimeMillis() - startTime);
        response.setLanguage(request.getLanguage());
        return response;
    }
}