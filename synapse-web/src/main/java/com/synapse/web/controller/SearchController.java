package com.synapse.web.controller;

import com.synapse.core.dto.SearchRequest;
import com.synapse.core.dto.SearchResponse;
import com.synapse.core.dto.SearchFeedbackRequest;
import com.synapse.core.dto.QARequest;
import com.synapse.core.dto.QAResponse;
import com.synapse.core.service.QAService;
import com.synapse.core.service.MultilingualQAService;
import com.synapse.core.service.SearchService;
import com.synapse.core.service.AuditService;
import com.synapse.security.annotation.RequireRole;
import com.synapse.security.util.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/search")
public class SearchController {

    private static final Logger logger = LoggerFactory.getLogger(SearchController.class);

    @Autowired
    private SearchService searchService;

    @Autowired
    private QAService qaService;
    
    @Autowired
    private MultilingualQAService multilingualQAService;

    @Autowired
    private AuditService auditService;

    @Autowired
    private SearchAnalyticsService searchAnalyticsService;

    @Autowired
    private MLRankingService mlRankingService;

    @Autowired
    private ABTestingService abTestingService;

    @Autowired
    private RecommendationService recommendationService;

    @PostMapping
    @RequireRole("USER")
    public ResponseEntity<SearchResponse> search(@Valid @RequestBody SearchRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        
        try {
            logger.info("Search request from user {}: query='{}', limit={}, offset={}", 
                userId, request.getQuery(), request.getLimit(), request.getOffset());
            
            // Use optimized search with performance monitoring
            SearchResponse response = searchService.searchWithMetrics(request, userId);
            
            // Record search analytics
            String sessionId = request.getSession(false) != null ? 
                request.getSession().getId() : "anonymous";
            searchAnalyticsService.recordSearchInteraction(request, response, userId, sessionId);
            
            // Log search activity with performance metrics
            auditService.logActivity(
                userId,
                "SEARCH",
                String.format("Search query: %s (results: %d, time: %dms)", 
                    request.getQuery(), response.getResults().size(), response.getSearchTimeMs()),
                "SearchController.search"
            );
            
            // Log performance warning if search is slow
            if (response.getSearchTimeMs() > 3000) {
                logger.warn("Slow search detected for user {}: {}ms for query '{}'", 
                    userId, response.getSearchTimeMs(), request.getQuery());
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Search failed for user {}: {}", userId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/optimized")
    @RequireRole("USER")
    public ResponseEntity<SearchResponse> searchOptimized(@Valid @RequestBody SearchRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        
        try {
            logger.info("Optimized search request from user {}: query='{}'", userId, request.getQuery());
            
            // Use the regular optimized search method
            SearchResponse response = searchService.search(request, userId);
            
            // Add performance metadata
            response.setOffset(request.getOffset());
            response.setLimit(request.getLimit());
            response.setHasMore(response.getCursor() != null);
            
            // Log search activity
            auditService.logActivity(
                userId,
                "SEARCH_OPTIMIZED",
                String.format("Optimized search: %s (cached: %s, time: %dms)", 
                    request.getQuery(), 
                    response.getSearchTimeMs() < 100 ? "likely" : "unlikely",
                    response.getSearchTimeMs()),
                "SearchController.searchOptimized"
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Optimized search failed for user {}: {}", userId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/suggestions")
    @RequireRole("USER")
    public ResponseEntity<String[]> getSearchSuggestions(@RequestParam String query) {
        Long userId = SecurityUtils.getCurrentUserId();
        
        try {
            long startTime = System.currentTimeMillis();
            String[] suggestions = generateSuggestions(query);
            long duration = System.currentTimeMillis() - startTime;
            
            logger.debug("Generated {} suggestions for query '{}' in {}ms", 
                suggestions.length, query, duration);
            
            return ResponseEntity.ok(suggestions);
        } catch (Exception e) {
            logger.error("Failed to generate suggestions for user {}: {}", userId, e.getMessage());
            return ResponseEntity.ok(new String[0]);
        }
    }
    
    @PostMapping("/preload")
    @RequireRole("USER")
    public ResponseEntity<Void> preloadSearchCache(@RequestParam String query) {
        Long userId = SecurityUtils.getCurrentUserId();
        
        try {
            logger.debug("Preloading search cache for user {}: query='{}'", userId, query);
            
            // Trigger async cache preloading
            searchService.preloadSearchCache(query, userId);
            
            return ResponseEntity.accepted().build();
            
        } catch (Exception e) {
            logger.error("Failed to preload search cache for user {}: {}", userId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/feedback")
    @RequireRole("USER")
    public ResponseEntity<Void> submitFeedback(@Valid @RequestBody SearchFeedbackRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        
        try {
            logger.info("Search feedback from user {}: query='{}', rating={}, responseTime={}ms", 
                userId, request.getQuery(), request.getRating(), request.getResponseTimeMs());
            
            // Record search feedback in analytics
            searchAnalyticsService.recordSearchFeedback(request, userId);
            
            // Log feedback activity with performance data
            auditService.logActivity(
                userId,
                "SEARCH_FEEDBACK",
                String.format("Feedback: query='%s', rating=%d, helpful=%s, responseTime=%dms", 
                    request.getQuery(), request.getRating(), request.isHelpful(), request.getResponseTimeMs()),
                "SearchController.submitFeedback"
            );
            
            // Alert if user reports slow performance
            if (request.getResponseTimeMs() != null && request.getResponseTimeMs() > 3000) {
                logger.warn("User {} reported slow search: {}ms for query '{}'", 
                    userId, request.getResponseTimeMs(), request.getQuery());
            }
            
            return ResponseEntity.ok().build();
            
        } catch (Exception e) {
            logger.error("Failed to submit feedback for user {}: {}", userId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/qa")
    @RequireRole("USER")
    public ResponseEntity<QAResponse> askQuestion(@Valid @RequestBody QARequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        
        try {
            logger.info("Q&A request from user {}: question='{}', language='{}'", 
                       userId, request.getQuestion(), request.getLanguage());
            
            // Use multilingual Q&A service for enhanced processing
            QAResponse response = multilingualQAService.processQuestion(request, userId);
            
            // Log Q&A activity with language information
            auditService.logActivity(
                userId,
                "QA_QUERY",
                String.format("Asked question in %s: %s (confidence: %.2f)", 
                             response.getLanguage(), request.getQuestion(), response.getConfidence()),
                "SearchController.askQuestion"
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Q&A failed for user {}: {}", userId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/qa/feedback")
    @RequireRole("USER")
    public ResponseEntity<Void> submitQAFeedback(@Valid @RequestBody QAFeedbackRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        
        try {
            logger.info("Q&A feedback from user {}: conversationId='{}', helpful={}", 
                       userId, request.getConversationId(), request.isHelpful());
            
            // Log Q&A feedback activity
            auditService.logActivity(
                userId,
                "QA_FEEDBACK",
                String.format("Submitted Q&A feedback: conversationId=%s, helpful=%s, rating=%d", 
                             request.getConversationId(), request.isHelpful(), request.getRating()),
                "SearchController.submitQAFeedback"
            );
            
            return ResponseEntity.ok().build();
            
        } catch (Exception e) {
            logger.error("Failed to submit Q&A feedback for user {}: {}", userId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    private String[] generateSuggestions(String query) {
        // Enhanced suggestion logic with performance optimization
        if (query.length() < 2) {
            return new String[0];
        }
        
        // Return optimized suggestions based on common patterns and search analytics
        String[] baseSuggestions = {
            query + " documentation",
            query + " guide",
            query + " tutorial",
            "how to " + query,
            query + " best practices",
            query + " troubleshooting",
            query + " configuration",
            query + " API reference"
        };
        
        // Limit to top 5 suggestions for better performance
        return java.util.Arrays.copyOf(baseSuggestions, Math.min(5, baseSuggestions.length));
    }
    
    public static class QAFeedbackRequest {
        private String conversationId;
        private boolean helpful;
        private int rating;
        private String comment;
        
        public String getConversationId() {
            return conversationId;
        }
        
        public void setConversationId(String conversationId) {
            this.conversationId = conversationId;
        }
        
        public boolean isHelpful() {
            return helpful;
        }
        
        public void setHelpful(boolean helpful) {
            this.helpful = helpful;
        }
        
        public int getRating() {
            return rating;
        }
        
        public void setRating(int rating) {
            this.rating = rating;
        }
        
        public String getComment() {
            return comment;
        }
        
        public void setComment(String comment) {
            this.comment = comment;
        }
    }
    
    @PostMapping("/analytics/click")
    @RequireRole("USER")
    public ResponseEntity<Void> recordClickedResults(@RequestBody ClickTrackingRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        
        try {
            logger.debug("Recording clicked results for user {}: query='{}', clicks={}", 
                userId, request.getQuery(), request.getClickedDocumentIds().size());
            
            searchAnalyticsService.recordClickedResults(
                request.getQuery(), 
                request.getClickedDocumentIds(), 
                userId
            );
            
            return ResponseEntity.ok().build();
            
        } catch (Exception e) {
            logger.error("Failed to record clicked results for user {}: {}", userId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/analytics/dashboard")
    @RequireRole("SYSTEM_ADMIN")
    public ResponseEntity<Map<String, Object>> getAnalyticsDashboard(@RequestParam(defaultValue = "30") int days) {
        try {
            Map<String, Object> dashboardData = searchAnalyticsService.getAnalyticsDashboardData(days);
            return ResponseEntity.ok(dashboardData);
        } catch (Exception e) {
            logger.error("Failed to get analytics dashboard: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/analytics/user-patterns")
    @RequireRole("USER")
    public ResponseEntity<Map<String, Object>> getUserSearchPatterns(@RequestParam(defaultValue = "30") int days) {
        Long userId = SecurityUtils.getCurrentUserId();
        
        try {
            Map<String, Object> patterns = searchAnalyticsService.getUserSearchPatterns(userId, days);
            return ResponseEntity.ok(patterns);
        } catch (Exception e) {
            logger.error("Failed to get user search patterns for user {}: {}", userId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/analytics/quality-metrics")
    @RequireRole("SYSTEM_ADMIN")
    public ResponseEntity<Map<String, Object>> getSearchQualityMetrics() {
        try {
            Map<String, Object> metrics = searchAnalyticsService.getSearchQualityMetrics();
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            logger.error("Failed to get search quality metrics: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/ml/model-info")
    @RequireRole("SYSTEM_ADMIN")
    public ResponseEntity<Map<String, Object>> getMLModelInfo() {
        try {
            Map<String, Object> modelInfo = mlRankingService.getModelInfo();
            return ResponseEntity.ok(modelInfo);
        } catch (Exception e) {
            logger.error("Failed to get ML model info: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/ab-test/results")
    @RequireRole("SYSTEM_ADMIN")
    public ResponseEntity<Map<String, Object>> getABTestResults() {
        try {
            Map<String, Object> results = abTestingService.getTestResults();
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            logger.error("Failed to get A/B test results: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/ab-test/initialize")
    @RequireRole("SYSTEM_ADMIN")
    public ResponseEntity<Void> initializeABTest(@RequestParam String testName, 
                                                 @RequestParam double trafficSplit) {
        try {
            abTestingService.initializeABTest(testName, trafficSplit);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Failed to initialize A/B test: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/recommendations/suggestions")
    @RequireRole("USER")
    public ResponseEntity<List<String>> getPersonalizedSuggestions(@RequestParam String query, 
                                                                  @RequestParam(defaultValue = "5") int limit) {
        Long userId = SecurityUtils.getCurrentUserId();
        
        try {
            List<String> suggestions = recommendationService.getPersonalizedSuggestions(userId, query, limit);
            return ResponseEntity.ok(suggestions);
        } catch (Exception e) {
            logger.error("Failed to get personalized suggestions for user {}: {}", userId, e.getMessage());
            return ResponseEntity.ok(Collections.emptyList());
        }
    }
    
    public static class ClickTrackingRequest {
        private String query;
        private List<String> clickedDocumentIds;
        
        public String getQuery() {
            return query;
        }
        
        public void setQuery(String query) {
            this.query = query;
        }
        
        public List<String> getClickedDocumentIds() {
            return clickedDocumentIds;
        }
        
        public void setClickedDocumentIds(List<String> clickedDocumentIds) {
            this.clickedDocumentIds = clickedDocumentIds;
        }
    }
}