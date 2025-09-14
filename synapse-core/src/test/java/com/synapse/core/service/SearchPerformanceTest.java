package com.synapse.core.service;

import com.synapse.core.dto.SearchRequest;
import com.synapse.core.dto.SearchResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * Performance tests for SearchService to validate <3 second response time requirement.
 * Task 12.3: Optimize search and Q&A response times
 */
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
class SearchPerformanceTest {

    private static final Logger logger = LoggerFactory.getLogger(SearchPerformanceTest.class);
    
    @Autowired
    private SearchService searchService;
    
    @MockBean
    private AccessControlService accessControlService;
    
    private static final long TEST_USER_ID = 1L;
    private static final int PERFORMANCE_TARGET_MS = 3000; // 3 seconds
    private static final int CONCURRENT_USERS = 50;
    
    @BeforeEach
    void setUp() {
        // Mock access control for test user
        when(accessControlService.getAccessibleProjectIds(anyLong()))
            .thenReturn(java.util.Set.of(1L, 2L, 3L));
        when(accessControlService.getAccessibleDepartmentIds(anyLong()))
            .thenReturn(java.util.Set.of(1L, 2L));
    }
    
    @Test
    @DisplayName("Single search query should complete within 3 seconds")
    void testSingleSearchPerformance() {
        // Given
        SearchRequest request = createSearchRequest("test query", 10);
        
        // When
        long startTime = System.currentTimeMillis();
        SearchResponse response = searchService.search(request, TEST_USER_ID);
        long duration = System.currentTimeMillis() - startTime;
        
        // Then
        assertNotNull(response);
        assertTrue(duration < PERFORMANCE_TARGET_MS, 
            String.format("Search took %dms, expected <%dms", duration, PERFORMANCE_TARGET_MS));
        
        logger.info("Single search performance: {}ms", duration);
    }
    
    @Test
    @DisplayName("Cached search query should complete within 100ms")
    void testCachedSearchPerformance() {
        // Given
        SearchRequest request = createSearchRequest("cached query test", 10);
        
        // First call to populate cache
        searchService.search(request, TEST_USER_ID);
        
        // When - Second call should hit cache
        long startTime = System.currentTimeMillis();
        SearchResponse response = searchService.search(request, TEST_USER_ID);
        long duration = System.currentTimeMillis() - startTime;
        
        // Then
        assertNotNull(response);
        assertTrue(duration < 100, 
            String.format("Cached search took %dms, expected <100ms", duration));
        
        logger.info("Cached search performance: {}ms", duration);
    }
    
    @Test
    @DisplayName("Concurrent searches should maintain performance under load")
    void testConcurrentSearchPerformance() throws InterruptedException {
        // Given
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_USERS);
        List<CompletableFuture<Long>> futures = new ArrayList<>();
        
        // When - Execute concurrent searches
        long testStartTime = System.currentTimeMillis();
        
        for (int i = 0; i < CONCURRENT_USERS; i++) {
            final int queryId = i;
            CompletableFuture<Long> future = CompletableFuture.supplyAsync(() -> {
                SearchRequest request = createSearchRequest("concurrent query " + queryId, 10);
                long startTime = System.currentTimeMillis();
                searchService.search(request, TEST_USER_ID);
                return System.currentTimeMillis() - startTime;
            }, executor);
            futures.add(future);
        }
        
        // Wait for all searches to complete
        List<Long> durations = new ArrayList<>();
        for (CompletableFuture<Long> future : futures) {
            durations.add(future.get());
        }
        
        long totalTestTime = System.currentTimeMillis() - testStartTime;
        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);
        
        // Then - Analyze performance
        double avgDuration = durations.stream().mapToLong(Long::longValue).average().orElse(0.0);
        long maxDuration = durations.stream().mapToLong(Long::longValue).max().orElse(0L);
        long minDuration = durations.stream().mapToLong(Long::longValue).min().orElse(0L);
        
        // 95th percentile
        durations.sort(Long::compareTo);
        long p95Duration = durations.get((int) (durations.size() * 0.95));
        
        logger.info("Concurrent search performance stats:");
        logger.info("  Total users: {}", CONCURRENT_USERS);
        logger.info("  Total test time: {}ms", totalTestTime);
        logger.info("  Average duration: {:.2f}ms", avgDuration);
        logger.info("  Min duration: {}ms", minDuration);
        logger.info("  Max duration: {}ms", maxDuration);
        logger.info("  95th percentile: {}ms", p95Duration);
        
        // Assertions
        assertTrue(avgDuration < PERFORMANCE_TARGET_MS, 
            String.format("Average search time %.2fms exceeds target %dms", avgDuration, PERFORMANCE_TARGET_MS));
        assertTrue(p95Duration < PERFORMANCE_TARGET_MS * 1.5, 
            String.format("95th percentile %dms exceeds acceptable threshold", p95Duration));
    }
    
    @Test
    @DisplayName("Large result set search should complete within performance target")
    void testLargeResultSetPerformance() {
        // Given
        SearchRequest request = createSearchRequest("comprehensive search query", 100);
        
        // When
        long startTime = System.currentTimeMillis();
        SearchResponse response = searchService.search(request, TEST_USER_ID);
        long duration = System.currentTimeMillis() - startTime;
        
        // Then
        assertNotNull(response);
        assertTrue(duration < PERFORMANCE_TARGET_MS, 
            String.format("Large result search took %dms, expected <%dms", duration, PERFORMANCE_TARGET_MS));
        
        logger.info("Large result set search performance: {}ms", duration);
    }
    
    @Test
    @DisplayName("Search with metrics should provide performance monitoring")
    void testSearchWithMetrics() {
        // Given
        SearchRequest request = createSearchRequest("metrics test query", 20);
        
        // When
        long startTime = System.currentTimeMillis();
        SearchResponse response = searchService.searchWithMetrics(request, TEST_USER_ID);
        long duration = System.currentTimeMillis() - startTime;
        
        // Then
        assertNotNull(response);
        assertTrue(duration < PERFORMANCE_TARGET_MS, 
            String.format("Search with metrics took %dms, expected <%dms", duration, PERFORMANCE_TARGET_MS));
        
        // Verify response contains timing information
        assertTrue(response.getSearchTimeMs() > 0, "Response should contain search time");
        
        logger.info("Search with metrics performance: {}ms (reported: {}ms)", 
            duration, response.getSearchTimeMs());
    }
    
    @Test
    @DisplayName("Cursor-based pagination should maintain performance")
    void testCursorPaginationPerformance() {
        // Given
        SearchRequest firstRequest = createSearchRequest("pagination test", 20);
        
        // When - First page
        long startTime = System.currentTimeMillis();
        SearchResponse firstResponse = searchService.search(firstRequest, TEST_USER_ID);
        long firstPageDuration = System.currentTimeMillis() - startTime;
        
        // Second page with cursor
        if (firstResponse.getCursor() != null) {
            SearchRequest secondRequest = createSearchRequest("pagination test", 20);
            secondRequest.setCursor(firstResponse.getCursor());
            
            startTime = System.currentTimeMillis();
            SearchResponse secondResponse = searchService.search(secondRequest, TEST_USER_ID);
            long secondPageDuration = System.currentTimeMillis() - startTime;
            
            // Then
            assertTrue(firstPageDuration < PERFORMANCE_TARGET_MS, 
                String.format("First page took %dms, expected <%dms", firstPageDuration, PERFORMANCE_TARGET_MS));
            assertTrue(secondPageDuration < PERFORMANCE_TARGET_MS, 
                String.format("Second page took %dms, expected <%dms", secondPageDuration, PERFORMANCE_TARGET_MS));
            
            logger.info("Pagination performance - First: {}ms, Second: {}ms", 
                firstPageDuration, secondPageDuration);
        }
    }
    
    private SearchRequest createSearchRequest(String query, int limit) {
        SearchRequest request = new SearchRequest();
        request.setQuery(query);
        request.setLimit(limit);
        request.setLanguage("en");
        return request;
    }
}