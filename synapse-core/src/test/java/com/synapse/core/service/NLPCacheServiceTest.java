package com.synapse.core.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.HashMap;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
class NLPCacheServiceTest {
    
    @Autowired
    private NLPCacheService nlpCacheService;
    
    @MockBean
    private CacheService cacheService;
    
    @MockBean
    private RestTemplate restTemplate;
    
    @BeforeEach
    void setUp() {
        // Reset mocks before each test
        reset(cacheService, restTemplate);
    }
    
    @Test
    void shouldReturnCachedEmbeddingWhenAvailable() {
        // Given
        String text = "test text";
        float[] expectedEmbedding = {1.0f, 2.0f, 3.0f};
        when(cacheService.getEmbedding(anyString())).thenReturn(expectedEmbedding);
        
        // When
        float[] result = nlpCacheService.getEmbeddingWithCache(text);
        
        // Then
        assertArrayEquals(expectedEmbedding, result);
        verify(cacheService).getEmbedding(anyString());
        verifyNoInteractions(restTemplate);
    }
    
    @Test
    void shouldGenerateAndCacheEmbeddingWhenNotCached() {
        // Given
        String text = "test text";
        when(cacheService.getEmbedding(anyString())).thenReturn(null);
        
        // When
        float[] result = nlpCacheService.getEmbeddingWithCache(text);
        
        // Then
        assertNotNull(result);
        verify(cacheService).getEmbedding(anyString());
        verify(cacheService).putInCache(eq(CacheService.EMBEDDING_CACHE), anyString(), any());
    }
    
    @Test
    void shouldEvictEmbeddingCache() {
        // Given
        String text = "test text";
        
        // When
        nlpCacheService.evictEmbeddingCache(text);
        
        // Then
        verify(cacheService).evictEmbedding(anyString());
    }
}