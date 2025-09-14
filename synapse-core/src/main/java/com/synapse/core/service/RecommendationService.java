package com.synapse.core.service;

import com.synapse.core.dto.SearchResult;
import com.synapse.data.entity.SearchAnalytics;
import com.synapse.data.repository.jpa.SearchAnalyticsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for adaptive ranking and recommendations based on user behavior.
 * Task 13.2: Build adaptive ranking and recommendation system
 */
@Service
public class RecommendationService {

    private static final Logger logger = LoggerFactory.getLogger(RecommendationService.class);

    @Autowired
    private SearchAnalyticsRepository searchAnalyticsRepository;

    /**
     * Rerank search results based on user behavior patterns.
     */
    public List<SearchResult> rerankResults(List<SearchResult> originalResults, UUID userId, String query) {
        try {
            Map<String, Double> documentScores = calculateDocumentScores(userId);
            Map<String, Double> queryPatternScores = calculateQueryPatternScores(query);
            
            return originalResults.stream()
                .peek(result -> {
                    double behaviorScore = documentScores.getOrDefault(result.getDocumentId(), 0.0);
                    double patternScore = queryPatternScores.getOrDefault(result.getDocumentId(), 0.0);
                    double personalizedScore = result.getRelevanceScore() + (behaviorScore * 0.2f) + (patternScore * 0.1f);
                    result.setRelevanceScore(Math.min(1.0f, (float) personalizedScore));
                })
                .sorted((a, b) -> Float.compare(b.getRelevanceScore(), a.getRelevanceScore()))
                .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Failed to rerank results: {}", e.getMessage());
            return originalResults;
        }
    }

    /**
     * Get personalized search suggestions based on user history.
     */
    public List<String> getPersonalizedSuggestions(UUID userId, String partialQuery, int limit) {
        try {
            LocalDateTime since = LocalDateTime.now().minusDays(30);
            List<SearchAnalytics> userSearches = searchAnalyticsRepository
                .findByUserIdOrderByTimestampDesc(userId, org.springframework.data.domain.PageRequest.of(0, 100))
                .getContent();

            Map<String, Long> queryFrequency = userSearches.stream()
                .filter(sa -> sa.getQueryText().toLowerCase().contains(partialQuery.toLowerCase()))
                .collect(Collectors.groupingBy(SearchAnalytics::getQueryText, Collectors.counting()));

            return queryFrequency.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Failed to get personalized suggestions: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Calculate document scores based on user interaction history.
     */
    private Map<String, Double> calculateDocumentScores(UUID userId) {
        LocalDateTime since = LocalDateTime.now().minusDays(30);
        List<SearchAnalytics> userSearches = searchAnalyticsRepository
            .findByUserIdOrderByTimestampDesc(userId, org.springframework.data.domain.PageRequest.of(0, 200))
            .getContent();

        Map<String, Double> scores = new HashMap<>();
        
        for (SearchAnalytics search : userSearches) {
            if (search.getClickedResults() != null && !search.getClickedResults().equals("[]")) {
                String[] clickedDocs = search.getClickedResults().replace("[", "").replace("]", "").split(",");
                for (String docId : clickedDocs) {
                    docId = docId.trim();
                    scores.merge(docId, 0.5, Double::sum);
                }
            }
            
            if (search.getFeedbackHelpful() != null && search.getFeedbackHelpful()) {
                // Boost documents from helpful searches
                if (search.getClickedResults() != null) {
                    String[] clickedDocs = search.getClickedResults().replace("[", "").replace("]", "").split(",");
                    for (String docId : clickedDocs) {
                        docId = docId.trim();
                        scores.merge(docId, 0.3, Double::sum);
                    }
                }
            }
        }
        
        return scores;
    }

    /**
     * Calculate query pattern scores for similar queries.
     */
    private Map<String, Double> calculateQueryPatternScores(String query) {
        LocalDateTime since = LocalDateTime.now().minusDays(7);
        List<Object[]> popularQueries = searchAnalyticsRepository.findPopularQueries(
            since, org.springframework.data.domain.PageRequest.of(0, 50));

        Map<String, Double> scores = new HashMap<>();
        
        for (Object[] queryData : popularQueries) {
            String popularQuery = (String) queryData[0];
            Long count = (Long) queryData[1];
            
            if (calculateQuerySimilarity(query, popularQuery) > 0.6) {
                // This is a simplified approach - in practice, you'd track which documents
                // were successful for similar queries
                scores.put("similar_pattern_boost", count * 0.1);
            }
        }
        
        return scores;
    }

    /**
     * Simple query similarity calculation.
     */
    private double calculateQuerySimilarity(String query1, String query2) {
        Set<String> words1 = new HashSet<>(Arrays.asList(query1.toLowerCase().split("\\s+")));
        Set<String> words2 = new HashSet<>(Arrays.asList(query2.toLowerCase().split("\\s+")));
        
        Set<String> intersection = new HashSet<>(words1);
        intersection.retainAll(words2);
        
        Set<String> union = new HashSet<>(words1);
        union.addAll(words2);
        
        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }
}