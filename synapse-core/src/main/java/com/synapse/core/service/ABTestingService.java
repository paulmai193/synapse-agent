package com.synapse.core.service;

import com.synapse.core.dto.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A/B Testing service for comparing different ranking algorithms.
 * Task 13.2: Build adaptive ranking and recommendation system
 */
@Service
public class ABTestingService {

    private static final Logger logger = LoggerFactory.getLogger(ABTestingService.class);

    @Autowired
    private MLRankingService mlRankingService;

    @Autowired
    private RecommendationService recommendationService;

    private final Map<String, TestGroup> activeTests = new ConcurrentHashMap<>();
    private final Map<UUID, String> userTestAssignments = new ConcurrentHashMap<>();

    public enum RankingAlgorithm {
        BASELINE,
        ML_RANKING,
        PERSONALIZED,
        HYBRID
    }

    /**
     * Get ranking algorithm for user based on A/B test assignment.
     */
    public RankingAlgorithm getUserRankingAlgorithm(UUID userId) {
        String testGroup = userTestAssignments.computeIfAbsent(userId, this::assignUserToTestGroup);
        
        TestGroup group = activeTests.get(testGroup);
        return group != null ? group.algorithm : RankingAlgorithm.BASELINE;
    }

    /**
     * Apply ranking based on user's test group assignment.
     */
    public List<SearchResult> applyTestRanking(List<SearchResult> results, UUID userId, String query) {
        RankingAlgorithm algorithm = getUserRankingAlgorithm(userId);
        
        try {
            switch (algorithm) {
                case ML_RANKING:
                    return mlRankingService.applyMLRanking(results, userId, query);
                case PERSONALIZED:
                    return recommendationService.rerankResults(results, userId, query);
                case HYBRID:
                    List<SearchResult> mlResults = mlRankingService.applyMLRanking(results, userId, query);
                    return recommendationService.rerankResults(mlResults, userId, query);
                case BASELINE:
                default:
                    return results; // Keep original ranking
            }
        } catch (Exception e) {
            logger.error("Failed to apply test ranking for algorithm {}: {}", algorithm, e.getMessage());
            return results;
        }
    }

    /**
     * Record test result for analysis.
     */
    public void recordTestResult(UUID userId, String query, List<SearchResult> results, 
                               boolean userSatisfied, double responseTime) {
        String testGroup = userTestAssignments.get(userId);
        if (testGroup != null) {
            TestGroup group = activeTests.get(testGroup);
            if (group != null) {
                synchronized (group) {
                    group.totalQueries++;
                    group.totalResponseTime += responseTime;
                    if (userSatisfied) {
                        group.satisfiedQueries++;
                    }
                }
                
                logger.debug("Recorded test result for user {} in group {}: satisfied={}, responseTime={}ms", 
                    userId, testGroup, userSatisfied, responseTime);
            }
        }
    }

    /**
     * Initialize A/B test with different ranking algorithms.
     */
    public void initializeABTest(String testName, double trafficSplit) {
        if (trafficSplit < 0.0 || trafficSplit > 1.0) {
            throw new IllegalArgumentException("Traffic split must be between 0.0 and 1.0");
        }

        // Create test groups
        activeTests.put(testName + "_baseline", new TestGroup(RankingAlgorithm.BASELINE, trafficSplit / 4));
        activeTests.put(testName + "_ml", new TestGroup(RankingAlgorithm.ML_RANKING, trafficSplit / 4));
        activeTests.put(testName + "_personalized", new TestGroup(RankingAlgorithm.PERSONALIZED, trafficSplit / 4));
        activeTests.put(testName + "_hybrid", new TestGroup(RankingAlgorithm.HYBRID, trafficSplit / 4));

        logger.info("Initialized A/B test '{}' with traffic split {}", testName, trafficSplit);
    }

    /**
     * Get A/B test results for analysis.
     */
    public Map<String, Object> getTestResults() {
        Map<String, Object> results = new HashMap<>();
        
        for (Map.Entry<String, TestGroup> entry : activeTests.entrySet()) {
            TestGroup group = entry.getValue();
            Map<String, Object> groupStats = new HashMap<>();
            
            synchronized (group) {
                groupStats.put("algorithm", group.algorithm.name());
                groupStats.put("totalQueries", group.totalQueries);
                groupStats.put("satisfiedQueries", group.satisfiedQueries);
                groupStats.put("satisfactionRate", group.totalQueries > 0 ? 
                    (double) group.satisfiedQueries / group.totalQueries : 0.0);
                groupStats.put("avgResponseTime", group.totalQueries > 0 ? 
                    group.totalResponseTime / group.totalQueries : 0.0);
                groupStats.put("trafficAllocation", group.trafficAllocation);
            }
            
            results.put(entry.getKey(), groupStats);
        }
        
        return results;
    }

    /**
     * Assign user to test group based on user ID hash.
     */
    private String assignUserToTestGroup(UUID userId) {
        if (activeTests.isEmpty()) {
            initializeABTest("default", 1.0);
        }

        int hash = Math.abs(userId.hashCode());
        double random = (hash % 10000) / 10000.0;
        
        double cumulative = 0.0;
        for (Map.Entry<String, TestGroup> entry : activeTests.entrySet()) {
            cumulative += entry.getValue().trafficAllocation;
            if (random < cumulative) {
                logger.debug("Assigned user {} to test group {}", userId, entry.getKey());
                return entry.getKey();
            }
        }
        
        // Fallback to first group
        String fallbackGroup = activeTests.keySet().iterator().next();
        logger.debug("Assigned user {} to fallback group {}", userId, fallbackGroup);
        return fallbackGroup;
    }

    /**
     * Test group configuration.
     */
    private static class TestGroup {
        final RankingAlgorithm algorithm;
        final double trafficAllocation;
        long totalQueries = 0;
        long satisfiedQueries = 0;
        double totalResponseTime = 0.0;

        TestGroup(RankingAlgorithm algorithm, double trafficAllocation) {
            this.algorithm = algorithm;
            this.trafficAllocation = trafficAllocation;
        }
    }
}