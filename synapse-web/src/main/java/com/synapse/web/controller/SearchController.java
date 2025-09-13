package com.synapse.web.controller;

import com.synapse.core.dto.SearchRequest;
import com.synapse.core.dto.SearchResponse;
import com.synapse.core.dto.SearchFeedbackRequest;
import com.synapse.core.dto.QARequest;
import com.synapse.core.dto.QAResponse;
import com.synapse.core.service.QAService;
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
    private AuditService auditService;

    @PostMapping
    @RequireRole("USER")
    public ResponseEntity<SearchResponse> search(@Valid @RequestBody SearchRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        
        try {
            logger.info("Search request from user {}: query='{}'", userId, request.getQuery());
            
            SearchResponse response = searchService.search(request, userId);
            
            // Log search activity
            auditService.logActivity(
                userId,
                "SEARCH",
                "Performed search query: " + request.getQuery(),
                "SearchController.search"
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Search failed for user {}: {}", userId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/suggestions")
    @RequireRole("USER")
    public ResponseEntity<String[]> getSearchSuggestions(@RequestParam String query) {
        try {
            String[] suggestions = generateSuggestions(query);
            return ResponseEntity.ok(suggestions);
        } catch (Exception e) {
            logger.error("Failed to generate suggestions: {}", e.getMessage());
            return ResponseEntity.ok(new String[0]);
        }
    }

    @PostMapping("/feedback")
    @RequireRole("USER")
    public ResponseEntity<Void> submitFeedback(@Valid @RequestBody SearchFeedbackRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        
        try {
            logger.info("Search feedback from user {}: query='{}', rating={}", 
                userId, request.getQuery(), request.getRating());
            
            // Log feedback activity
            auditService.logActivity(
                userId,
                "SEARCH_FEEDBACK",
                String.format("Submitted feedback for query '%s': rating=%d, helpful=%s", 
                    request.getQuery(), request.getRating(), request.isHelpful()),
                "SearchController.submitFeedback"
            );
            
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
            logger.info("Q&A request from user {}: question='{}'", userId, request.getQuestion());
            
            QAResponse response = qaService.answerQuestion(request, userId);
            
            // Log Q&A activity
            auditService.logActivity(
                userId,
                "QA_QUERY",
                "Asked question: " + request.getQuestion(),
                "SearchController.askQuestion"
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Q&A failed for user {}: {}", userId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    private String[] generateSuggestions(String query) {
        // Basic suggestion logic - can be enhanced with search history, popular queries, etc.
        if (query.length() < 2) {
            return new String[0];
        }
        
        // Return some basic suggestions based on common patterns
        return new String[]{
            query + " documentation",
            query + " guide",
            query + " tutorial",
            "how to " + query,
            query + " best practices"
        };
    }
}