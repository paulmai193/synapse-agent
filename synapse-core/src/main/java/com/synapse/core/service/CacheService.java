package com.synapse.core.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@Service
public class CacheService {
    
    private static final Logger logger = LoggerFactory.getLogger(CacheService.class);
    
    @Autowired
    private CacheManager cacheManager;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    // Cache names
    public static final String USER_PERMISSIONS_CACHE = "userPermissions";
    public static final String ROLE_DATA_CACHE = "roleData";
    public static final String DOCUMENT_METADATA_CACHE = "documentMetadata";
    public static final String SEARCH_RESULTS_CACHE = "searchResults";
    public static final String USER_PROFILE_CACHE = "userProfile";
    public static final String USER_AUTH_CACHE = "userAuth";
    public static final String PERMISSION_LOOKUP_CACHE = "permissionLookup";
    public static final String EMBEDDING_CACHE = "embeddings";
    
    // User authentication data caching
    @Cacheable(value = USER_AUTH_CACHE, key = "#userId")
    public Map<String, Object> getUserAuthData(String userId) {
        logger.debug("Cache miss for user auth data: {}", userId);
        return null; // Will be populated by calling service
    }
    
    @Cacheable(value = USER_PERMISSIONS_CACHE, key = "#userId")
    public Set<String> getUserPermissions(String userId) {
        logger.debug("Cache miss for user permissions: {}", userId);
        return null; // Will be populated by calling service
    }
    
    @Cacheable(value = ROLE_DATA_CACHE, key = "#roleId")
    public Object getRoleData(String roleId) {
        logger.debug("Cache miss for role data: {}", roleId);
        return null; // Will be populated by calling service
    }
    
    @Cacheable(value = DOCUMENT_METADATA_CACHE, key = "#documentId")
    public Object getDocumentMetadata(String documentId) {
        logger.debug("Cache miss for document metadata: {}", documentId);
        return null; // Will be populated by calling service
    }
    
    // Search result caching with TTL
    @Cacheable(value = SEARCH_RESULTS_CACHE, key = "#query + '_' + #userId")
    public Map<String, Object> getSearchResults(String query, String userId) {
        logger.debug("Cache miss for search results: query={}, user={}", query, userId);
        return null; // Will be populated by calling service
    }
    
    // Permission lookup caching
    @Cacheable(value = PERMISSION_LOOKUP_CACHE, key = "#userId + '_' + #resourceId")
    public Boolean hasPermission(String userId, String resourceId) {
        logger.debug("Cache miss for permission lookup: user={}, resource={}", userId, resourceId);
        return null; // Will be populated by calling service
    }
    
    // Embedding caching for NLP operations
    @Cacheable(value = EMBEDDING_CACHE, key = "#textHash")
    public float[] getEmbedding(String textHash) {
        logger.debug("Cache miss for embedding: {}", textHash);
        return null; // Will be populated by calling service
    }
    
    // Cache eviction methods
    @CacheEvict(value = USER_AUTH_CACHE, key = "#userId")
    public void evictUserAuthData(String userId) {
        logger.info("Evicted user auth data cache for user: {}", userId);
    }
    
    @CacheEvict(value = USER_PERMISSIONS_CACHE, key = "#userId")
    public void evictUserPermissions(String userId) {
        logger.info("Evicted user permissions cache for user: {}", userId);
    }
    
    @CacheEvict(value = ROLE_DATA_CACHE, key = "#roleId")
    public void evictRoleData(String roleId) {
        logger.info("Evicted role data cache for role: {}", roleId);
    }
    
    @CacheEvict(value = DOCUMENT_METADATA_CACHE, key = "#documentId")
    public void evictDocumentMetadata(String documentId) {
        logger.info("Evicted document metadata cache for document: {}", documentId);
    }
    
    @CacheEvict(value = SEARCH_RESULTS_CACHE, key = "#query + '_' + #userId")
    public void evictSearchResults(String query, String userId) {
        logger.info("Evicted search results cache for query: {} and user: {}", query, userId);
    }
    
    @CacheEvict(value = PERMISSION_LOOKUP_CACHE, key = "#userId + '_' + #resourceId")
    public void evictPermissionLookup(String userId, String resourceId) {
        logger.info("Evicted permission lookup cache for user: {} and resource: {}", userId, resourceId);
    }
    
    @CacheEvict(value = EMBEDDING_CACHE, key = "#textHash")
    public void evictEmbedding(String textHash) {
        logger.info("Evicted embedding cache for text hash: {}", textHash);
    }
    
    @CacheEvict(value = {USER_AUTH_CACHE, USER_PERMISSIONS_CACHE, ROLE_DATA_CACHE, DOCUMENT_METADATA_CACHE, 
                        SEARCH_RESULTS_CACHE, PERMISSION_LOOKUP_CACHE, EMBEDDING_CACHE}, allEntries = true)
    public void evictAllCaches() {
        logger.info("Evicted all caches");
    }
    
    // Cache warming strategies
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void warmFrequentlyAccessedData() {
        logger.debug("Starting cache warming for frequently accessed data");
        try {
            warmRoleDataCache();
            warmPermissionCache();
            logger.debug("Cache warming completed successfully");
        } catch (Exception e) {
            logger.error("Error during cache warming: {}", e.getMessage(), e);
        }
    }
    
    public void warmUpCache() {
        logger.info("Starting manual cache warm-up process");
        warmFrequentlyAccessedData();
    }
    
    private void warmRoleDataCache() {
        String[] commonRoles = {"USER", "PROJECT_ADMIN", "DEPARTMENT_ADMIN", "SYSTEM_ADMIN"};
        for (String role : commonRoles) {
            putInCache(ROLE_DATA_CACHE, role, "warmed_role_" + role);
        }
        logger.debug("Warmed role data cache with {} entries", commonRoles.length);
    }
    
    private void warmPermissionCache() {
        logger.debug("Warming permission cache for active users");
        // This would typically query for most active users and pre-load their permissions
    }
    
    public Cache getCache(String cacheName) {
        return cacheManager.getCache(cacheName);
    }
    
    public void putInCache(String cacheName, Object key, Object value) {
        Cache cache = getCache(cacheName);
        if (cache != null) {
            cache.put(key, value);
            logger.debug("Added to cache {}: key={}", cacheName, key);
        }
    }
    
    public Object getFromCache(String cacheName, Object key) {
        Cache cache = getCache(cacheName);
        if (cache != null) {
            Cache.ValueWrapper wrapper = cache.get(key);
            if (wrapper != null) {
                logger.debug("Cache hit for {}: key={}", cacheName, key);
                return wrapper.get();
            }
        }
        logger.debug("Cache miss for {}: key={}", cacheName, key);
        return null;
    }
    
    public void evictFromCache(String cacheName, Object key) {
        Cache cache = getCache(cacheName);
        if (cache != null) {
            cache.evict(key);
            logger.info("Evicted from cache {}: key={}", cacheName, key);
        }
    }
    
    // Cache statistics and monitoring
    public Map<String, Object> getCacheStatistics() {
        Map<String, Object> stats = new HashMap<>();
        try {
            Set<String> keys = redisTemplate.keys("*");
            stats.put("totalKeys", keys != null ? keys.size() : 0);
            stats.put("timestamp", System.currentTimeMillis());
            
            // Get cache-specific statistics
            stats.put("userAuthKeys", getKeyCount("userAuth*"));
            stats.put("userPermissionKeys", getKeyCount("userPermissions*"));
            stats.put("roleDataKeys", getKeyCount("roleData*"));
            stats.put("searchResultKeys", getKeyCount("searchResults*"));
            stats.put("embeddingKeys", getKeyCount("embeddings*"));
            
        } catch (Exception e) {
            logger.error("Error getting cache statistics: {}", e.getMessage(), e);
            stats.put("error", e.getMessage());
        }
        return stats;
    }
    
    private long getKeyCount(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            return keys != null ? keys.size() : 0;
        } catch (Exception e) {
            logger.warn("Error counting keys for pattern {}: {}", pattern, e.getMessage());
            return 0;
        }
    }
    
    // Database query optimization helpers
    public void optimizeQueryCache(String queryKey, Object result) {
        putInCache("queryResults", queryKey, result);
    }
    
    public Object getCachedQueryResult(String queryKey) {
        return getFromCache("queryResults", queryKey);
    }
}