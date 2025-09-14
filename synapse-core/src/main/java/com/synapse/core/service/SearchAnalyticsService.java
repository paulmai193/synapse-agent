package com.synapse.core.service;

import com.synapse.core.dto.SearchRequest;
import com.synapse.core.dto.SearchResponse;
import com.synapse.core.dto.SearchFeedbackRequest;
import com.synapse.data.entity.SearchAnalytics;
import com.synapse.data.repository.jpa.SearchAnalyticsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for search analytics and feedback collection.
 * Task 13.1: Implement search analytics and feedback collection
 */
@Service
public class SearchAnalyticsService {

    private static final Logger logger = LoggerFactory.getLogger(SearchAnalyticsService.class);

    @Autowired
    private SearchAnalyticsRepository searchAnalyticsRepository;

    @Autowired
    private HttpServletRequest httpServletRequest;

    /**
     * Record search interaction for analytics.
     */
    public void recordSearchInteraction(SearchRequest request, SearchResponse response, UUID userId, String sessionId) {
        try {
            SearchAnalytics analytics = new SearchAnalytics();
            analytics.setUserId(userId);
            analytics.setQueryText(request.getQuery());
            analytics.setQueryLanguage(request.getLanguage());
            analytics.setResultsCount(response.getResults().size());
            analytics.setResponseTimeMs(response.getSearchTimeMs());
            analytics.setSearchType(determineSearchType(response.getSearchTimeMs()));
            analytics.setSessionId(sessionId);
            analytics.setIpAddress(getClientIpAddress());
            analytics.setUserAgent(httpServletRequest.getHeader("User-Agent"));

            searchAnalyticsRepository.save(analytics);
            logger.debug("Recorded search analytics for user {} query '{}'", userId, request.getQuery());
        } catch (Exception e) {
            logger.error("Failed to record search analytics: {}", e.getMessage());
        }
    }

    /**
     * Update search analytics with user feedback.
     */
    public void recordSearchFeedback(SearchFeedbackRequest feedback, UUID userId) {
        try {
            // Find recent search analytics for this user and query
            LocalDateTime since = LocalDateTime.now().minusHours(1);
            Page<SearchAnalytics> recentSearches = searchAnalyticsRepository.findByUserIdOrderByTimestampDesc(
                userId, PageRequest.of(0, 10));

            Optional<SearchAnalytics> matchingSearch = recentSearches.getContent().stream()
                .filter(sa -> sa.getQueryText().equals(feedback.getQuery()))
                .findFirst();

            if (matchingSearch.isPresent()) {
                SearchAnalytics analytics = matchingSearch.get();
                analytics.setFeedbackRating(feedback.getRating());
                analytics.setFeedbackHelpful(feedback.isHelpful());
                analytics.setFeedbackComment(feedback.getFeedback());
                
                if (feedback.getResponseTimeMs() != null) {
                    analytics.setResponseTimeMs(feedback.getResponseTimeMs());
                }

                searchAnalyticsRepository.save(analytics);
                logger.info("Updated search feedback for user {} query '{}'", userId, feedback.getQuery());
            } else {
                logger.warn("No matching search found for feedback from user {} query '{}'", userId, feedback.getQuery());
            }
        } catch (Exception e) {
            logger.error("Failed to record search feedback: {}", e.getMessage());
        }
    }

    /**
     * Record clicked search results for click-through rate analysis.
     */
    public void recordClickedResults(String query, List<String> clickedDocumentIds, UUID userId) {
        try {
            // Find recent search for this query
            Page<SearchAnalytics> recentSearches = searchAnalyticsRepository.findByUserIdOrderByTimestampDesc(
                userId, PageRequest.of(0, 5));

            Optional<SearchAnalytics> matchingSearch = recentSearches.getContent().stream()
                .filter(sa -> sa.getQueryText().equals(query))
                .findFirst();

            if (matchingSearch.isPresent()) {
                SearchAnalytics analytics = matchingSearch.get();
                String clickedResultsJson = String.join(",", clickedDocumentIds);
                analytics.setClickedResults("[" + clickedResultsJson + "]");
                
                searchAnalyticsRepository.save(analytics);
                logger.debug("Recorded clicked results for user {} query '{}'", userId, query);
            }
        } catch (Exception e) {
            logger.error("Failed to record clicked results: {}", e.getMessage());
        }
    }

    /**
     * Get search analytics dashboard data.
     */
    public Map<String, Object> getAnalyticsDashboardData(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        Map<String, Object> dashboardData = new HashMap<>();

        try {
            // Total searches
            long totalSearches = searchAnalyticsRepository.count();
            dashboardData.put("totalSearches", totalSearches);

            // Average response time
            Double avgResponseTime = searchAnalyticsRepository.getAverageResponseTime(since);
            dashboardData.put("averageResponseTime", avgResponseTime != null ? avgResponseTime : 0.0);

            // Popular queries
            List<Object[]> popularQueries = searchAnalyticsRepository.findPopularQueries(
                since, PageRequest.of(0, 10));
            dashboardData.put("popularQueries", formatPopularQueries(popularQueries));

            // Click-through rate
            Object[] clickStats = searchAnalyticsRepository.getClickThroughStats(since);
            if (clickStats.length >= 2) {
                Long totalSearchesWithResults = (Long) clickStats[0];
                Long searchesWithClicks = (Long) clickStats[1];
                double clickThroughRate = totalSearchesWithResults > 0 ? 
                    (double) searchesWithClicks / totalSearchesWithResults * 100 : 0.0;
                dashboardData.put("clickThroughRate", clickThroughRate);
            }

            // Performance by search type
            List<Object[]> performanceByType = searchAnalyticsRepository.getPerformanceBySearchType(since);
            dashboardData.put("performanceBySearchType", formatPerformanceByType(performanceByType));

            // Slow searches
            List<SearchAnalytics> slowSearches = searchAnalyticsRepository.findSlowSearches(3000L, since);
            dashboardData.put("slowSearchesCount", slowSearches.size());

            logger.debug("Generated analytics dashboard data for {} days", days);
        } catch (Exception e) {
            logger.error("Failed to generate analytics dashboard data: {}", e.getMessage());
        }

        return dashboardData;
    }

    /**
     * Get user search patterns for personalization.
     */
    public Map<String, Object> getUserSearchPatterns(UUID userId, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        Map<String, Object> patterns = new HashMap<>();

        try {
            // User's search count
            Long searchCount = searchAnalyticsRepository.countByUserIdAndTimestampBetween(
                userId, since, LocalDateTime.now());
            patterns.put("searchCount", searchCount);

            // User's search history
            Page<SearchAnalytics> userSearches = searchAnalyticsRepository.findByUserIdOrderByTimestampDesc(
                userId, PageRequest.of(0, 20));
            patterns.put("recentSearches", formatUserSearches(userSearches.getContent()));

            logger.debug("Generated search patterns for user {} over {} days", userId, days);
        } catch (Exception e) {
            logger.error("Failed to generate user search patterns: {}", e.getMessage());
        }

        return patterns;
    }

    /**
     * Get search quality metrics for improvement insights.
     */
    public Map<String, Object> getSearchQualityMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        try {
            // Searches with feedback
            Page<SearchAnalytics> searchesWithFeedback = searchAnalyticsRepository.findSearchesWithFeedback(
                PageRequest.of(0, 100));
            
            List<SearchAnalytics> feedbackList = searchesWithFeedback.getContent();
            
            // Average rating
            double avgRating = feedbackList.stream()
                .filter(sa -> sa.getFeedbackRating() != null)
                .mapToInt(SearchAnalytics::getFeedbackRating)
                .average()
                .orElse(0.0);
            metrics.put("averageRating", avgRating);

            // Helpful percentage
            long helpfulCount = feedbackList.stream()
                .filter(sa -> sa.getFeedbackHelpful() != null && sa.getFeedbackHelpful())
                .count();
            double helpfulPercentage = feedbackList.size() > 0 ? 
                (double) helpfulCount / feedbackList.size() * 100 : 0.0;
            metrics.put("helpfulPercentage", helpfulPercentage);

            // Low satisfaction searches
            List<SearchAnalytics> lowSatisfactionSearches = searchAnalyticsRepository.findLowSatisfactionSearches();
            metrics.put("lowSatisfactionCount", lowSatisfactionSearches.size());
            metrics.put("lowSatisfactionQueries", lowSatisfactionSearches.stream()
                .map(SearchAnalytics::getQueryText)
                .distinct()
                .limit(10)
                .collect(Collectors.toList()));

            logger.debug("Generated search quality metrics");
        } catch (Exception e) {
            logger.error("Failed to generate search quality metrics: {}", e.getMessage());
        }

        return metrics;
    }

    private String determineSearchType(Long responseTimeMs) {
        if (responseTimeMs == null) return "unknown";
        if (responseTimeMs < 100) return "cached";
        if (responseTimeMs < 1000) return "optimized";
        return "fresh";
    }

    private String getClientIpAddress() {
        String xForwardedFor = httpServletRequest.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return httpServletRequest.getRemoteAddr();
    }

    private List<Map<String, Object>> formatPopularQueries(List<Object[]> popularQueries) {
        return popularQueries.stream()
            .map(row -> {
                Map<String, Object> query = new HashMap<>();
                query.put("query", row[0]);
                query.put("count", row[1]);
                return query;
            })
            .collect(Collectors.toList());
    }

    private List<Map<String, Object>> formatPerformanceByType(List<Object[]> performanceData) {
        return performanceData.stream()
            .map(row -> {
                Map<String, Object> perf = new HashMap<>();
                perf.put("searchType", row[0]);
                perf.put("averageResponseTime", row[1]);
                perf.put("count", row[2]);
                return perf;
            })
            .collect(Collectors.toList());
    }

    private List<Map<String, Object>> formatUserSearches(List<SearchAnalytics> searches) {
        return searches.stream()
            .map(sa -> {
                Map<String, Object> search = new HashMap<>();
                search.put("query", sa.getQueryText());
                search.put("timestamp", sa.getTimestamp());
                search.put("resultsCount", sa.getResultsCount());
                search.put("responseTime", sa.getResponseTimeMs());
                search.put("rating", sa.getFeedbackRating());
                return search;
            })
            .collect(Collectors.toList());
    }
}