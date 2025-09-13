package com.synapse.core.service;

import com.synapse.core.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class QAService {

    private static final Logger logger = LoggerFactory.getLogger(QAService.class);

    @Autowired
    private SearchService searchService;

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

            // Generate answer based on search results
            String answer = generateAnswer(request.getQuestion(), searchResponse);
            Float confidence = calculateConfidence(searchResponse);

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
            return createErrorResponse(request, startTime);
        }
    }

    private String generateAnswer(String question, SearchResponse searchResponse) {
        if (searchResponse.getResults().isEmpty()) {
            return "I couldn't find relevant information to answer your question. Please try rephrasing your question or check if you have access to the relevant documents.";
        }

        // Simple answer generation - in a real implementation, this would use LLM
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