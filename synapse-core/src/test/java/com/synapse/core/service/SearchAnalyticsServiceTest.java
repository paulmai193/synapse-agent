package com.synapse.core.service;

import com.synapse.core.dto.SearchRequest;
import com.synapse.core.dto.SearchResponse;
import com.synapse.core.dto.SearchFeedbackRequest;
import com.synapse.core.dto.SearchResult;
import com.synapse.data.entity.SearchAnalytics;
import com.synapse.data.repository.jpa.SearchAnalyticsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SearchAnalyticsService.
 * Task 13.1: Implement search analytics and feedback collection
 */
class SearchAnalyticsServiceTest {

    @Mock
    private SearchAnalyticsRepository searchAnalyticsRepository;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private SearchAnalyticsService searchAnalyticsService;

    private UUID testUserId;
    private SearchRequest testSearchRequest;
    private SearchResponse testSearchResponse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        testUserId = UUID.randomUUID();
        
        testSearchRequest = new SearchRequest();
        testSearchRequest.setQuery("test query");
        testSearchRequest.setLanguage("en");
        testSearchRequest.setLimit(10);
        
        testSearchResponse = new SearchResponse();
        testSearchResponse.setQuery("test query");
        testSearchResponse.setResults(Arrays.asList(new SearchResult(), new SearchResult()));
        testSearchResponse.setSearchTimeMs(150L);
        
        when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(httpServletRequest.getHeader("User-Agent")).thenReturn("Test Browser");
    }

    @Test
    @DisplayName("Should record search interaction successfully")
    void testRecordSearchInteraction() {
        // Given
        String sessionId = "test-session-123";
        
        // When
        searchAnalyticsService.recordSearchInteraction(testSearchRequest, testSearchResponse, testUserId, sessionId);
        
        // Then
        verify(searchAnalyticsRepository).save(argThat(analytics -> 
            analytics.getUserId().equals(testUserId) &&
            analytics.getQueryText().equals("test query") &&
            analytics.getQueryLanguage().equals("en") &&
            analytics.getResultsCount().equals(2) &&
            analytics.getResponseTimeMs().equals(150L) &&
            analytics.getSearchType().equals("optimized") &&
            analytics.getSessionId().equals(sessionId)
        ));
    }

    @Test
    @DisplayName("Should record search feedback successfully")
    void testRecordSearchFeedback() {
        // Given
        SearchFeedbackRequest feedback = new SearchFeedbackRequest();
        feedback.setQuery("test query");
        feedback.setRating(4);
        feedback.setHelpful(true);
        feedback.setFeedback("Very helpful results");
        feedback.setResponseTimeMs(150L);
        
        SearchAnalytics existingAnalytics = new SearchAnalytics();
        existingAnalytics.setUserId(testUserId);
        existingAnalytics.setQueryText("test query");
        existingAnalytics.setTimestamp(LocalDateTime.now().minusMinutes(5));
        
        Page<SearchAnalytics> searchPage = new PageImpl<>(Arrays.asList(existingAnalytics));
        when(searchAnalyticsRepository.findByUserIdOrderByTimestampDesc(eq(testUserId), any(PageRequest.class)))
            .thenReturn(searchPage);
        
        // When
        searchAnalyticsService.recordSearchFeedback(feedback, testUserId);
        
        // Then
        verify(searchAnalyticsRepository).save(argThat(analytics ->
            analytics.getFeedbackRating().equals(4) &&
            analytics.getFeedbackHelpful().equals(true) &&
            analytics.getFeedbackComment().equals("Very helpful results") &&
            analytics.getResponseTimeMs().equals(150L)
        ));
    }

    @Test
    @DisplayName("Should record clicked results successfully")
    void testRecordClickedResults() {
        // Given
        String query = "test query";
        List<String> clickedDocIds = Arrays.asList("doc1", "doc2", "doc3");
        
        SearchAnalytics existingAnalytics = new SearchAnalytics();
        existingAnalytics.setUserId(testUserId);
        existingAnalytics.setQueryText(query);
        existingAnalytics.setTimestamp(LocalDateTime.now().minusMinutes(2));
        
        Page<SearchAnalytics> searchPage = new PageImpl<>(Arrays.asList(existingAnalytics));
        when(searchAnalyticsRepository.findByUserIdOrderByTimestampDesc(eq(testUserId), any(PageRequest.class)))
            .thenReturn(searchPage);
        
        // When
        searchAnalyticsService.recordClickedResults(query, clickedDocIds, testUserId);
        
        // Then
        verify(searchAnalyticsRepository).save(argThat(analytics ->
            analytics.getClickedResults().equals("[doc1,doc2,doc3]")
        ));
    }

    @Test
    @DisplayName("Should generate analytics dashboard data")
    void testGetAnalyticsDashboardData() {
        // Given
        int days = 30;
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        
        when(searchAnalyticsRepository.count()).thenReturn(1000L);
        when(searchAnalyticsRepository.getAverageResponseTime(any(LocalDateTime.class))).thenReturn(250.5);
        when(searchAnalyticsRepository.findPopularQueries(any(LocalDateTime.class), any(PageRequest.class)))
            .thenReturn(Arrays.asList(
                new Object[]{"popular query 1", 50L},
                new Object[]{"popular query 2", 30L}
            ));
        when(searchAnalyticsRepository.getClickThroughStats(any(LocalDateTime.class)))
            .thenReturn(new Object[]{800L, 320L});
        when(searchAnalyticsRepository.getPerformanceBySearchType(any(LocalDateTime.class)))
            .thenReturn(Arrays.asList(
                new Object[]{"cached", 50.0, 200L},
                new Object[]{"optimized", 150.0, 400L},
                new Object[]{"fresh", 800.0, 200L}
            ));
        when(searchAnalyticsRepository.findSlowSearches(eq(3000L), any(LocalDateTime.class)))
            .thenReturn(Arrays.asList(new SearchAnalytics(), new SearchAnalytics()));
        
        // When
        Map<String, Object> dashboardData = searchAnalyticsService.getAnalyticsDashboardData(days);
        
        // Then
        assertNotNull(dashboardData);
        assertEquals(1000L, dashboardData.get("totalSearches"));
        assertEquals(250.5, dashboardData.get("averageResponseTime"));
        assertEquals(40.0, dashboardData.get("clickThroughRate")); // 320/800 * 100
        assertEquals(2, dashboardData.get("slowSearchesCount"));
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> popularQueries = (List<Map<String, Object>>) dashboardData.get("popularQueries");
        assertEquals(2, popularQueries.size());
        assertEquals("popular query 1", popularQueries.get(0).get("query"));
        assertEquals(50L, popularQueries.get(0).get("count"));
    }

    @Test
    @DisplayName("Should generate user search patterns")
    void testGetUserSearchPatterns() {
        // Given
        int days = 30;
        
        when(searchAnalyticsRepository.countByUserIdAndTimestampBetween(
            eq(testUserId), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(25L);
        
        List<SearchAnalytics> userSearches = Arrays.asList(
            createSearchAnalytics("query 1", 100L, 5),
            createSearchAnalytics("query 2", 200L, 3),
            createSearchAnalytics("query 3", 150L, null)
        );
        Page<SearchAnalytics> searchPage = new PageImpl<>(userSearches);
        when(searchAnalyticsRepository.findByUserIdOrderByTimestampDesc(eq(testUserId), any(PageRequest.class)))
            .thenReturn(searchPage);
        
        // When
        Map<String, Object> patterns = searchAnalyticsService.getUserSearchPatterns(testUserId, days);
        
        // Then
        assertNotNull(patterns);
        assertEquals(25L, patterns.get("searchCount"));
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> recentSearches = (List<Map<String, Object>>) patterns.get("recentSearches");
        assertEquals(3, recentSearches.size());
        assertEquals("query 1", recentSearches.get(0).get("query"));
        assertEquals(100L, recentSearches.get(0).get("responseTime"));
        assertEquals(5, recentSearches.get(0).get("rating"));
    }

    @Test
    @DisplayName("Should generate search quality metrics")
    void testGetSearchQualityMetrics() {
        // Given
        List<SearchAnalytics> searchesWithFeedback = Arrays.asList(
            createSearchAnalyticsWithFeedback(4, true),
            createSearchAnalyticsWithFeedback(5, true),
            createSearchAnalyticsWithFeedback(2, false),
            createSearchAnalyticsWithFeedback(3, null)
        );
        Page<SearchAnalytics> feedbackPage = new PageImpl<>(searchesWithFeedback);
        when(searchAnalyticsRepository.findSearchesWithFeedback(any(PageRequest.class)))
            .thenReturn(feedbackPage);
        
        List<SearchAnalytics> lowSatisfactionSearches = Arrays.asList(
            createSearchAnalytics("bad query 1", 1000L, 1),
            createSearchAnalytics("bad query 2", 2000L, 2)
        );
        when(searchAnalyticsRepository.findLowSatisfactionSearches())
            .thenReturn(lowSatisfactionSearches);
        
        // When
        Map<String, Object> metrics = searchAnalyticsService.getSearchQualityMetrics();
        
        // Then
        assertNotNull(metrics);
        assertEquals(3.5, (Double) metrics.get("averageRating"), 0.1); // (4+5+2+3)/4
        assertEquals(66.67, (Double) metrics.get("helpfulPercentage"), 0.1); // 2/3 * 100 (excluding null)
        assertEquals(2, metrics.get("lowSatisfactionCount"));
        
        @SuppressWarnings("unchecked")
        List<String> lowSatisfactionQueries = (List<String>) metrics.get("lowSatisfactionQueries");
        assertTrue(lowSatisfactionQueries.contains("bad query 1"));
        assertTrue(lowSatisfactionQueries.contains("bad query 2"));
    }

    private SearchAnalytics createSearchAnalytics(String query, Long responseTime, Integer rating) {
        SearchAnalytics analytics = new SearchAnalytics();
        analytics.setUserId(testUserId);
        analytics.setQueryText(query);
        analytics.setResponseTimeMs(responseTime);
        analytics.setResultsCount(5);
        analytics.setFeedbackRating(rating);
        analytics.setTimestamp(LocalDateTime.now().minusHours(1));
        return analytics;
    }

    private SearchAnalytics createSearchAnalyticsWithFeedback(Integer rating, Boolean helpful) {
        SearchAnalytics analytics = new SearchAnalytics();
        analytics.setUserId(testUserId);
        analytics.setQueryText("test query");
        analytics.setFeedbackRating(rating);
        analytics.setFeedbackHelpful(helpful);
        analytics.setTimestamp(LocalDateTime.now().minusHours(1));
        return analytics;
    }
}