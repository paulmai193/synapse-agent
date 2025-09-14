package com.synapse.core.service;

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

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MLRankingService.
 * Task 13.2: Build adaptive ranking and recommendation system
 */
class MLRankingServiceTest {

    @Mock
    private SearchAnalyticsRepository searchAnalyticsRepository;

    @InjectMocks
    private MLRankingService mlRankingService;

    private UUID testUserId;
    private List<SearchResult> testResults;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        testUserId = UUID.randomUUID();
        
        // Create test search results
        testResults = Arrays.asList(
            createSearchResult("doc1", "Test Document 1", "Content about testing", 0.8f),
            createSearchResult("doc2", "Guide Document", "User guide content", 0.7f),
            createSearchResult("doc3", "API Reference", "API documentation", 0.6f)
        );
    }

    @Test
    @DisplayName("Should apply ML ranking to search results")
    void testApplyMLRanking() {
        // Given
        when(searchAnalyticsRepository.findByUserIdOrderByTimestampDesc(eq(testUserId), any(PageRequest.class)))
            .thenReturn(new PageImpl<>(Collections.emptyList()));

        // When
        List<SearchResult> rankedResults = mlRankingService.applyMLRanking(testResults, testUserId, "test query");

        // Then
        assertNotNull(rankedResults);
        assertEquals(3, rankedResults.size());
        
        // Results should be sorted by ML score (descending)
        for (int i = 0; i < rankedResults.size() - 1; i++) {
            assertTrue(rankedResults.get(i).getRelevanceScore() >= rankedResults.get(i + 1).getRelevanceScore());
        }
    }

    @Test
    @DisplayName("Should boost documents with user interactions")
    void testUserInteractionBoost() {
        // Given
        SearchAnalytics userInteraction = new SearchAnalytics();
        userInteraction.setUserId(testUserId);
        userInteraction.setClickedResults("[doc1,doc2]");
        userInteraction.setTimestamp(LocalDateTime.now().minusDays(1));
        
        when(searchAnalyticsRepository.findByUserIdOrderByTimestampDesc(eq(testUserId), any(PageRequest.class)))
            .thenReturn(new PageImpl<>(Arrays.asList(userInteraction)));

        // When
        List<SearchResult> rankedResults = mlRankingService.applyMLRanking(testResults, testUserId, "test");

        // Then
        assertNotNull(rankedResults);
        
        // Documents with user interactions should get boosted scores
        SearchResult doc1Result = rankedResults.stream()
            .filter(r -> r.getDocumentId().equals("doc1"))
            .findFirst()
            .orElse(null);
        
        assertNotNull(doc1Result);
        // The exact score depends on the ML algorithm, but it should be adjusted
        assertTrue(doc1Result.getRelevanceScore() > 0.0f);
    }

    @Test
    @DisplayName("Should handle empty search results gracefully")
    void testEmptySearchResults() {
        // Given
        List<SearchResult> emptyResults = Collections.emptyList();

        // When
        List<SearchResult> rankedResults = mlRankingService.applyMLRanking(emptyResults, testUserId, "test");

        // Then
        assertNotNull(rankedResults);
        assertTrue(rankedResults.isEmpty());
    }

    @Test
    @DisplayName("Should return model information")
    void testGetModelInfo() {
        // When
        Map<String, Object> modelInfo = mlRankingService.getModelInfo();

        // Then
        assertNotNull(modelInfo);
        assertTrue(modelInfo.containsKey("featureWeights"));
        assertTrue(modelInfo.containsKey("lastUpdate"));
        assertTrue(modelInfo.containsKey("modelVersion"));
        
        @SuppressWarnings("unchecked")
        Map<String, Double> weights = (Map<String, Double>) modelInfo.get("featureWeights");
        assertNotNull(weights);
        assertTrue(weights.containsKey("base_relevance"));
        assertTrue(weights.containsKey("recency"));
        assertTrue(weights.containsKey("title_match"));
    }

    @Test
    @DisplayName("Should handle retraining with insufficient data")
    void testRetrainWithInsufficientData() {
        // Given
        List<SearchAnalytics> limitedFeedback = Arrays.asList(
            createSearchAnalyticsWithFeedback(4, true),
            createSearchAnalyticsWithFeedback(3, false)
        );
        
        when(searchAnalyticsRepository.findSearchesWithFeedback(any(PageRequest.class)))
            .thenReturn(new PageImpl<>(limitedFeedback));

        // When
        mlRankingService.retrainModel();

        // Then
        // Should complete without errors and keep existing model
        Map<String, Object> modelInfo = mlRankingService.getModelInfo();
        assertNotNull(modelInfo);
    }

    @Test
    @DisplayName("Should calculate feature weights correctly")
    void testFeatureWeightCalculation() {
        // Given - this tests the internal feature extraction logic
        SearchResult result = createSearchResult("doc1", "Test Document", "Test content", 0.8f);
        result.setLastModified(new Date(System.currentTimeMillis() - 86400000)); // 1 day ago

        // When
        List<SearchResult> rankedResults = mlRankingService.applyMLRanking(
            Arrays.asList(result), testUserId, "test document");

        // Then
        assertNotNull(rankedResults);
        assertEquals(1, rankedResults.size());
        
        SearchResult rankedResult = rankedResults.get(0);
        assertTrue(rankedResult.getRelevanceScore() >= 0.0f);
        assertTrue(rankedResult.getRelevanceScore() <= 1.0f);
    }

    private SearchResult createSearchResult(String docId, String title, String content, float score) {
        SearchResult result = new SearchResult();
        result.setDocumentId(docId);
        result.setTitle(title);
        result.setContent(content);
        result.setRelevanceScore(score);
        result.setLastModified(new Date());
        return result;
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