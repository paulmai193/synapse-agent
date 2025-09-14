package com.synapse.core.service;

import com.synapse.core.dto.SearchResult;
import com.synapse.data.entity.SearchAnalytics;
import com.synapse.data.repository.jpa.SearchAnalyticsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Machine Learning-based ranking service for search results.
 * Task 13.2: Build adaptive ranking and recommendation system
 */
@Service
public class MLRankingService {

    private static final Logger logger = LoggerFactory.getLogger(MLRankingService.class);

    @Autowired
    private SearchAnalyticsRepository searchAnalyticsRepository;

    private volatile Map<String, Double> featureWeights = new HashMap<>();
    private volatile LocalDateTime lastModelUpdate = LocalDateTime.now();

    /**
     * Apply ML-based ranking to search results.
     */
    public List<SearchResult> applyMLRanking(List<SearchResult> results, UUID userId, String query) {
        try {
            if (featureWeights.isEmpty()) {
                initializeDefaultWeights();
            }

            return results.stream()
                .peek(result -> {
                    double mlScore = calculateMLScore(result, userId, query);
                    result.setRelevanceScore((float) mlScore);
                })
                .sorted((a, b) -> Float.compare(b.getRelevanceScore(), a.getRelevanceScore()))
                .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Failed to apply ML ranking: {}", e.getMessage());
            return results;
        }
    }

    /**
     * Calculate ML-based score for a search result.
     */
    private double calculateMLScore(SearchResult result, UUID userId, String query) {
        Map<String, Double> features = extractFeatures(result, userId, query);
        
        double score = 0.0;
        for (Map.Entry<String, Double> feature : features.entrySet()) {
            Double weight = featureWeights.get(feature.getKey());
            if (weight != null) {
                score += feature.getValue() * weight;
            }
        }
        
        return Math.max(0.0, Math.min(1.0, score));
    }

    /**
     * Extract features for ML ranking.
     */
    private Map<String, Double> extractFeatures(SearchResult result, UUID userId, String query) {
        Map<String, Double> features = new HashMap<>();
        
        // Base relevance score
        features.put("base_relevance", (double) result.getRelevanceScore());
        
        // Document recency
        if (result.getLastModified() != null) {
            long daysSinceModified = (System.currentTimeMillis() - result.getLastModified().getTime()) / (1000 * 60 * 60 * 24);
            features.put("recency", Math.max(0.0, 1.0 - (daysSinceModified / 365.0)));
        } else {
            features.put("recency", 0.5);
        }
        
        // Content length indicator
        if (result.getContent() != null) {
            double lengthScore = Math.min(1.0, result.getContent().length() / 1000.0);
            features.put("content_length", lengthScore);
        } else {
            features.put("content_length", 0.5);
        }
        
        // Query-title match
        if (result.getTitle() != null && query != null) {
            double titleMatch = calculateTextSimilarity(query.toLowerCase(), result.getTitle().toLowerCase());
            features.put("title_match", titleMatch);
        } else {
            features.put("title_match", 0.0);
        }
        
        // User interaction history (simplified)
        features.put("user_interaction", getUserInteractionScore(result.getDocumentId(), userId));
        
        return features;
    }

    /**
     * Get user interaction score for a document.
     */
    private double getUserInteractionScore(String documentId, UUID userId) {
        try {
            LocalDateTime since = LocalDateTime.now().minusDays(30);
            List<SearchAnalytics> userSearches = searchAnalyticsRepository
                .findByUserIdOrderByTimestampDesc(userId, org.springframework.data.domain.PageRequest.of(0, 100))
                .getContent();

            long clickCount = userSearches.stream()
                .filter(sa -> sa.getClickedResults() != null && sa.getClickedResults().contains(documentId))
                .count();

            return Math.min(1.0, clickCount * 0.2);
        } catch (Exception e) {
            logger.warn("Failed to calculate user interaction score: {}", e.getMessage());
            return 0.0;
        }
    }

    /**
     * Simple text similarity calculation.
     */
    private double calculateTextSimilarity(String text1, String text2) {
        Set<String> words1 = new HashSet<>(Arrays.asList(text1.split("\\s+")));
        Set<String> words2 = new HashSet<>(Arrays.asList(text2.split("\\s+")));
        
        Set<String> intersection = new HashSet<>(words1);
        intersection.retainAll(words2);
        
        Set<String> union = new HashSet<>(words1);
        union.addAll(words2);
        
        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }

    /**
     * Initialize default feature weights.
     */
    private void initializeDefaultWeights() {
        featureWeights.put("base_relevance", 0.4);
        featureWeights.put("recency", 0.15);
        featureWeights.put("content_length", 0.1);
        featureWeights.put("title_match", 0.2);
        featureWeights.put("user_interaction", 0.15);
        
        logger.info("Initialized default ML ranking weights");
    }

    /**
     * Retrain the ranking model based on feedback data.
     */
    @Async
    @Scheduled(fixedRate = 86400000) // Daily
    public CompletableFuture<Void> retrainModel() {
        logger.info("Starting ML ranking model retraining");
        
        try {
            LocalDateTime since = LocalDateTime.now().minusDays(7);
            List<SearchAnalytics> feedbackData = searchAnalyticsRepository
                .findSearchesWithFeedback(org.springframework.data.domain.PageRequest.of(0, 1000))
                .getContent();

            if (feedbackData.size() < 50) {
                logger.info("Insufficient feedback data for retraining ({}), keeping current model", feedbackData.size());
                return CompletableFuture.completedFuture(null);
            }

            Map<String, Double> newWeights = calculateOptimalWeights(feedbackData);
            
            if (validateWeights(newWeights)) {
                featureWeights = newWeights;
                lastModelUpdate = LocalDateTime.now();
                logger.info("ML ranking model retrained successfully with {} feedback samples", feedbackData.size());
            } else {
                logger.warn("New weights failed validation, keeping current model");
            }
            
        } catch (Exception e) {
            logger.error("Failed to retrain ML ranking model: {}", e.getMessage());
        }
        
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Calculate optimal weights based on feedback data (simplified approach).
     */
    private Map<String, Double> calculateOptimalWeights(List<SearchAnalytics> feedbackData) {
        Map<String, Double> weights = new HashMap<>(featureWeights);
        
        // Simple weight adjustment based on feedback patterns
        double positiveRatio = feedbackData.stream()
            .filter(sa -> sa.getFeedbackHelpful() != null && sa.getFeedbackHelpful())
            .count() / (double) feedbackData.size();

        double avgRating = feedbackData.stream()
            .filter(sa -> sa.getFeedbackRating() != null)
            .mapToInt(SearchAnalytics::getFeedbackRating)
            .average()
            .orElse(3.0);

        // Adjust weights based on overall satisfaction
        if (avgRating > 4.0) {
            // High satisfaction - slight adjustments
            weights.replaceAll((k, v) -> v * 1.02);
        } else if (avgRating < 3.0) {
            // Low satisfaction - more significant adjustments
            weights.put("user_interaction", weights.get("user_interaction") * 1.1);
            weights.put("title_match", weights.get("title_match") * 1.05);
        }

        // Normalize weights
        double sum = weights.values().stream().mapToDouble(Double::doubleValue).sum();
        weights.replaceAll((k, v) -> v / sum);
        
        return weights;
    }

    /**
     * Validate new weights.
     */
    private boolean validateWeights(Map<String, Double> weights) {
        double sum = weights.values().stream().mapToDouble(Double::doubleValue).sum();
        return Math.abs(sum - 1.0) < 0.01 && weights.values().stream().allMatch(w -> w >= 0.0 && w <= 1.0);
    }

    /**
     * Get current model information.
     */
    public Map<String, Object> getModelInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("featureWeights", new HashMap<>(featureWeights));
        info.put("lastUpdate", lastModelUpdate);
        info.put("modelVersion", "1.0");
        return info;
    }
}