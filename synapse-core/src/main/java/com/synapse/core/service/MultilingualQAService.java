package com.synapse.core.service;

import com.synapse.core.dto.*;
import com.synapse.data.llm.LLMService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class MultilingualQAService {

    private static final Logger logger = LoggerFactory.getLogger(MultilingualQAService.class);
    private static final String NLP_SERVICE_URL = "http://localhost:8001/api/v1/text";
    private static final double CONFIDENCE_THRESHOLD = 0.6;

    @Autowired
    private SearchService searchService;

    @Autowired
    private LLMService llmService;

    @Autowired
    private RestTemplate restTemplate;

    public QAResponse processQuestion(QARequest request, Long userId) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Step 1: Detect query language
            String detectedLanguage = detectLanguage(request.getQuestion());
            if (detectedLanguage == null) {
                detectedLanguage = request.getLanguage() != null ? request.getLanguage() : "en";
            }
            
            logger.debug("Detected language: {} for question: {}", detectedLanguage, request.getQuestion());

            // Step 2: Enhance query for better search results
            String enhancedQuery = enhanceQuery(request.getQuestion(), detectedLanguage);
            
            // Step 3: Retrieve context with access control filtering
            List<Map<String, Object>> contextChunks = retrieveContextWithAccessControl(
                enhancedQuery, request, userId, detectedLanguage
            );

            // Step 4: Generate multilingual response
            CompletableFuture<LLMService.QAResult> qaResultFuture = llmService.generateQAResponse(
                request.getQuestion(), contextChunks, detectedLanguage
            );

            LLMService.QAResult qaResult = qaResultFuture.get(30, TimeUnit.SECONDS);

            // Step 5: Handle uncertainty and provide fallback
            QAResponse response = buildQAResponse(request, qaResult, contextChunks, detectedLanguage, startTime);
            
            // Step 6: Apply confidence-based uncertainty handling
            if (qaResult.getConfidence() < CONFIDENCE_THRESHOLD) {
                response = handleUncertainty(request, contextChunks, detectedLanguage, startTime);
            }

            logger.info("Multilingual Q&A completed: language={}, confidence={}, sources={}", 
                       detectedLanguage, qaResult.getConfidence(), contextChunks.size());

            return response;

        } catch (Exception e) {
            logger.error("Multilingual Q&A failed for question: {}", request.getQuestion(), e);
            return createErrorResponse(request, startTime);
        }
    }

    private String detectLanguage(String text) {
        try {
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("text", text);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(
                NLP_SERVICE_URL + "/detect-language", entity, Map.class
            );

            if (response != null && response.containsKey("language")) {
                return (String) response.get("language");
            }
        } catch (Exception e) {
            logger.warn("Language detection failed, using default", e);
        }
        return null;
    }

    private String enhanceQuery(String query, String language) {
        try {
            CompletableFuture<String> enhancedQueryFuture = llmService.enhanceSearchQuery(query, language);
            String enhanced = enhancedQueryFuture.get(10, TimeUnit.SECONDS);
            
            // Use enhanced query if it's significantly different and not empty
            if (enhanced != null && !enhanced.trim().isEmpty() && 
                !enhanced.toLowerCase().equals(query.toLowerCase())) {
                logger.debug("Enhanced query from '{}' to '{}'", query, enhanced);
                return enhanced;
            }
        } catch (Exception e) {
            logger.warn("Query enhancement failed, using original query", e);
        }
        return query;
    }

    private List<Map<String, Object>> retrieveContextWithAccessControl(String query, QARequest request, 
                                                                      Long userId, String language) {
        // Create search request for context retrieval
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setQuery(query);
        searchRequest.setLimit(10); // Get more results for better context
        searchRequest.setLanguage(language);
        searchRequest.setProjectId(request.getProjectId());
        searchRequest.setDepartmentId(request.getDepartmentId());

        // Perform search with access control
        SearchResponse searchResponse = searchService.search(searchRequest, userId);

        // Convert search results to context chunks
        List<Map<String, Object>> contextChunks = new ArrayList<>();
        for (SearchResult result : searchResponse.getResults()) {
            Map<String, Object> chunk = new HashMap<>();
            chunk.put("content", result.getContent());
            chunk.put("documentTitle", result.getTitle());
            chunk.put("documentId", result.getDocumentId());
            chunk.put("chunkId", result.getChunkId());
            chunk.put("confidence", (double) result.getRelevanceScore());
            chunk.put("source", result.getSource());
            chunk.put("projectId", result.getProjectId());
            contextChunks.add(chunk);
        }

        logger.debug("Retrieved {} context chunks for Q&A", contextChunks.size());
        return contextChunks;
    }

    private QAResponse buildQAResponse(QARequest request, LLMService.QAResult qaResult, 
                                     List<Map<String, Object>> contextChunks, String language, long startTime) {
        QAResponse response = new QAResponse();
        response.setQuestion(request.getQuestion());
        response.setAnswer(qaResult.getAnswer());
        response.setConversationId(getOrCreateConversationId(request.getConversationId()));
        response.setConfidence((float) qaResult.getConfidence());
        response.setLanguage(language);
        response.setResponseTimeMs(System.currentTimeMillis() - startTime);

        // Convert context chunks to search results for sources
        List<SearchResult> sources = contextChunks.stream()
            .map(this::convertChunkToSearchResult)
            .toList();
        response.setSources(sources);

        return response;
    }

    private QAResponse handleUncertainty(QARequest request, List<Map<String, Object>> contextChunks, 
                                       String language, long startTime) {
        logger.info("Handling uncertainty for low-confidence answer");

        // Generate uncertainty response with suggestions
        String uncertaintyMessage = generateUncertaintyMessage(contextChunks, language);
        
        QAResponse response = new QAResponse();
        response.setQuestion(request.getQuestion());
        response.setAnswer(uncertaintyMessage);
        response.setConversationId(getOrCreateConversationId(request.getConversationId()));
        response.setConfidence(0.3f); // Low confidence for uncertainty responses
        response.setLanguage(language);
        response.setResponseTimeMs(System.currentTimeMillis() - startTime);

        // Include available sources even for uncertain responses
        List<SearchResult> sources = contextChunks.stream()
            .map(this::convertChunkToSearchResult)
            .toList();
        response.setSources(sources);

        return response;
    }

    private String generateUncertaintyMessage(List<Map<String, Object>> contextChunks, String language) {
        if (contextChunks.isEmpty()) {
            return getLocalizedMessage("no_relevant_information", language);
        }

        StringBuilder message = new StringBuilder();
        message.append(getLocalizedMessage("partial_information_available", language)).append("\n\n");

        // Show partial context
        contextChunks.stream()
            .limit(2)
            .forEach(chunk -> {
                String content = (String) chunk.get("content");
                String title = (String) chunk.get("documentTitle");
                message.append("• ").append(title).append(": ")
                       .append(content.substring(0, Math.min(150, content.length())))
                       .append("...\n");
            });

        message.append("\n").append(getLocalizedMessage("suggestions", language));
        return message.toString();
    }

    private String getLocalizedMessage(String key, String language) {
        Map<String, Map<String, String>> messages = Map.of(
            "en", Map.of(
                "no_relevant_information", "I couldn't find relevant information to answer your question. Please try rephrasing your question or check if you have access to the relevant documents.",
                "partial_information_available", "Based on the available documents, I found some related information but cannot provide a complete answer:",
                "suggestions", "Suggestions:\n1. Try rephrasing your question with different keywords\n2. Check if you have access to additional relevant documents\n3. Contact the document authors for more specific information"
            ),
            "vi", Map.of(
                "no_relevant_information", "Tôi không thể tìm thấy thông tin liên quan để trả lời câu hỏi của bạn. Vui lòng thử diễn đạt lại câu hỏi hoặc kiểm tra xem bạn có quyền truy cập vào các tài liệu liên quan không.",
                "partial_information_available", "Dựa trên các tài liệu có sẵn, tôi đã tìm thấy một số thông tin liên quan nhưng không thể đưa ra câu trả lời hoàn chỉnh:",
                "suggestions", "Gợi ý:\n1. Thử diễn đạt lại câu hỏi với các từ khóa khác\n2. Kiểm tra xem bạn có quyền truy cập vào các tài liệu liên quan bổ sung không\n3. Liên hệ với tác giả tài liệu để biết thông tin cụ thể hơn"
            )
        );

        return messages.getOrDefault(language, messages.get("en")).getOrDefault(key, "");
    }

    private SearchResult convertChunkToSearchResult(Map<String, Object> chunk) {
        SearchResult result = new SearchResult();
        result.setDocumentId((String) chunk.get("documentId"));
        result.setChunkId((String) chunk.get("chunkId"));
        result.setTitle((String) chunk.get("documentTitle"));
        result.setContent((String) chunk.get("content"));
        result.setSource((String) chunk.get("source"));
        result.setRelevanceScore(((Double) chunk.get("confidence")).floatValue());
        result.setProjectId((Long) chunk.get("projectId"));
        return result;
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
        response.setLanguage(request.getLanguage() != null ? request.getLanguage() : "en");
        return response;
    }
}