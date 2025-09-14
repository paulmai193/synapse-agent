package com.synapse.core.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CacheServiceTest {

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

    @Mock
    private Cache.ValueWrapper valueWrapper;

    private CacheService cacheService;

    @BeforeEach
    void setUp() {
        cacheService = new CacheService();
        cacheService.cacheManager = cacheManager;
    }

    @Test
    void testPutInCache() {
        // Given
        String cacheName = "testCache";
        String key = "testKey";
        String value = "testValue";
        
        when(cacheManager.getCache(cacheName)).thenReturn(cache);

        // When
        cacheService.putInCache(cacheName, key, value);

        // Then
        verify(cache).put(key, value);
    }

    @Test
    void testGetFromCache_Hit() {
        // Given
        String cacheName = "testCache";
        String key = "testKey";
        String expectedValue = "testValue";
        
        when(cacheManager.getCache(cacheName)).thenReturn(cache);
        when(cache.get(key)).thenReturn(valueWrapper);
        when(valueWrapper.get()).thenReturn(expectedValue);

        // When
        Object result = cacheService.getFromCache(cacheName, key);

        // Then
        assertEquals(expectedValue, result);
    }

    @Test
    void testGetFromCache_Miss() {
        // Given
        String cacheName = "testCache";
        String key = "testKey";
        
        when(cacheManager.getCache(cacheName)).thenReturn(cache);
        when(cache.get(key)).thenReturn(null);

        // When
        Object result = cacheService.getFromCache(cacheName, key);

        // Then
        assertNull(result);
    }

    @Test
    void testEvictFromCache() {
        // Given
        String cacheName = "testCache";
        String key = "testKey";
        
        when(cacheManager.getCache(cacheName)).thenReturn(cache);

        // When
        cacheService.evictFromCache(cacheName, key);

        // Then
        verify(cache).evict(key);
    }

    @Test
    void testEvictUserPermissions() {
        // Given
        String userId = "user123";
        when(cacheManager.getCache(CacheService.USER_PERMISSIONS_CACHE)).thenReturn(cache);

        // When
        cacheService.evictUserPermissions(userId);

        // Then
        verify(cache).evict(userId);
    }

    @Test
    void testGetCache() {
        // Given
        String cacheName = "testCache";
        when(cacheManager.getCache(cacheName)).thenReturn(cache);

        // When
        Cache result = cacheService.getCache(cacheName);

        // Then
        assertEquals(cache, result);
        verify(cacheManager).getCache(cacheName);
    }
}