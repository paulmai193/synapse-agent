package com.synapse.core.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class CacheService {
    
    private static final Logger logger = LoggerFactory.getLogger(CacheService.class);
    
    @Autowired
    private CacheManager cacheManager;
    
    // Cache names
    public static final String USER_PERMISSIONS_CACHE = "userPermissions";
    public static final String ROLE_DATA_CACHE = "roleData";
    public static final String DOCUMENT_METADATA_CACHE = "documentMetadata";
    public static final String SEARCH_RESULTS_CACHE = "searchResults";
    public static final String USER_PROFILE_CACHE = "userProfile";
    
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
    
    @CacheEvict(value = {USER_PERMISSIONS_CACHE, ROLE_DATA_CACHE, DOCUMENT_METADATA_CACHE}, allEntries = true)
    public void evictAllCaches() {
        logger.info("Evicted all caches");
    }
    
    public void warmUpCache() {
        logger.info("Starting cache warm-up process");
        // Cache warming logic will be implemented by specific services
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
}