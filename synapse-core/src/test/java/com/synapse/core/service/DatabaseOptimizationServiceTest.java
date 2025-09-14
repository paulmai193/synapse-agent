package com.synapse.core.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
class DatabaseOptimizationServiceTest {
    
    @Autowired
    private DatabaseOptimizationService databaseOptimizationService;
    
    @MockBean
    private CacheService cacheService;
    
    @BeforeEach
    void setUp() {
        reset(cacheService);
    }
    
    @Test
    void shouldReturnCachedPagedResults() {
        // Given
        String queryKey = "testQuery";
        Pageable pageable = PageRequest.of(0, 10);
        List<String> content = List.of("item1", "item2");
        Page<String> expectedPage = new PageImpl<>(content, pageable, 2);
        
        when(cacheService.getCachedQueryResult(anyString())).thenReturn(expectedPage);
        
        // When
        Page<String> result = databaseOptimizationService.getCachedPagedResults(
            queryKey, pageable, (p) -> expectedPage);
        
        // Then
        assertEquals(expectedPage, result);
        verify(cacheService).getCachedQueryResult(anyString());
    }
    
    @Test
    void shouldExecuteAndCacheQueryWhenNotCached() {
        // Given
        String queryKey = "testQuery";
        Pageable pageable = PageRequest.of(0, 10);
        List<String> content = List.of("item1", "item2");
        Page<String> expectedPage = new PageImpl<>(content, pageable, 2);
        
        when(cacheService.getCachedQueryResult(anyString())).thenReturn(null);
        
        // When
        Page<String> result = databaseOptimizationService.getCachedPagedResults(
            queryKey, pageable, (p) -> expectedPage);
        
        // Then
        assertEquals(expectedPage, result);
        verify(cacheService).getCachedQueryResult(anyString());
        verify(cacheService).optimizeQueryCache(anyString(), eq(expectedPage));
    }
    
    @Test
    void shouldReturnCachedListResults() {
        // Given
        String queryKey = "testListQuery";
        List<String> expectedList = List.of("item1", "item2", "item3");
        
        when(cacheService.getCachedQueryResult(queryKey)).thenReturn(expectedList);
        
        // When
        List<String> result = databaseOptimizationService.getCachedListResults(
            queryKey, () -> expectedList);
        
        // Then
        assertEquals(expectedList, result);
        verify(cacheService).getCachedQueryResult(queryKey);
    }
    
    @Test
    void shouldReturnOptimizationMetrics() {
        // Given
        Map<String, Object> cacheStats = Map.of("totalKeys", 100);
        when(cacheService.getCacheStatistics()).thenReturn(cacheStats);
        
        // When
        Map<String, Object> metrics = databaseOptimizationService.getOptimizationMetrics();
        
        // Then
        assertNotNull(metrics);
        assertTrue(metrics.containsKey("cacheStatistics"));
        assertTrue(metrics.containsKey("optimizedQueries"));
        assertTrue(metrics.containsKey("cacheHitRate"));
        verify(cacheService).getCacheStatistics();
    }
}