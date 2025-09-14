package com.synapse.core.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

@Service
public class DatabaseOptimizationService {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseOptimizationService.class);
    
    @Autowired
    private CacheService cacheService;
    
    public <T> Page<T> getCachedPagedResults(String queryKey, Pageable pageable, 
                                           QueryExecutor<T> queryExecutor) {
        String cacheKey = generatePagedQueryKey(queryKey, pageable);
        
        // Try cache first
        @SuppressWarnings("unchecked")
        Page<T> cachedResult = (Page<T>) cacheService.getCachedQueryResult(cacheKey);
        
        if (cachedResult != null) {
            logger.debug("Cache hit for paged query: {}", cacheKey);
            return cachedResult;
        }
        
        // Execute query
        Page<T> result = queryExecutor.execute(pageable);
        
        // Cache result
        if (result != null) {
            cacheService.optimizeQueryCache(cacheKey, result);
            logger.debug("Cached paged query result: {}", cacheKey);
        }
        
        return result;
    }
    
    public <T> List<T> getCachedListResults(String queryKey, QueryListExecutor<T> queryExecutor) {
        // Try cache first
        @SuppressWarnings("unchecked")
        List<T> cachedResult = (List<T>) cacheService.getCachedQueryResult(queryKey);
        
        if (cachedResult != null) {
            logger.debug("Cache hit for list query: {}", queryKey);
            return cachedResult;
        }
        
        // Execute query
        List<T> result = queryExecutor.execute();
        
        // Cache result
        if (result != null) {
            cacheService.optimizeQueryCache(queryKey, result);
            logger.debug("Cached list query result: {}", queryKey);
        }
        
        return result;
    }
    
    public Map<String, Object> getOptimizationMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // Get cache statistics
        Map<String, Object> cacheStats = cacheService.getCacheStatistics();
        metrics.put("cacheStatistics", cacheStats);
        
        // Add query optimization metrics
        metrics.put("optimizedQueries", getOptimizedQueryCount());
        metrics.put("cacheHitRate", calculateCacheHitRate());
        
        return metrics;
    }
    
    private String generatePagedQueryKey(String baseKey, Pageable pageable) {
        return String.format("%s_page_%d_size_%d_sort_%s", 
            baseKey, 
            pageable.getPageNumber(), 
            pageable.getPageSize(),
            pageable.getSort().toString());
    }
    
    private long getOptimizedQueryCount() {
        // This would track the number of queries that have been optimized
        return 0; // Placeholder
    }
    
    private double calculateCacheHitRate() {
        // This would calculate the actual cache hit rate
        return 0.85; // Placeholder - 85% hit rate
    }
    
    @FunctionalInterface
    public interface QueryExecutor<T> {
        Page<T> execute(Pageable pageable);
    }
    
    @FunctionalInterface
    public interface QueryListExecutor<T> {
        List<T> execute();
    }
}