package com.synapse.core.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.HashMap;

@Service
public class NLPCacheService {
    
    private static final Logger logger = LoggerFactory.getLogger(NLPCacheService.class);
    
    @Autowired
    private CacheService cacheService;
    
    @Autowired
    private RestTemplate restTemplate;
    
    private static final String NLP_SERVICE_URL = "http://localhost:8001";
    
    public float[] getEmbeddingWithCache(String text) {
        String textHash = generateTextHash(text);
        
        // Try to get from cache first
        float[] cachedEmbedding = cacheService.getEmbedding(textHash);
        if (cachedEmbedding != null) {
            logger.debug("Cache hit for embedding: {}", textHash);
            return cachedEmbedding;
        }
        
        // Generate embedding via NLP service
        float[] embedding = generateEmbedding(text);
        
        // Cache the result
        if (embedding != null) {
            cacheService.putInCache(CacheService.EMBEDDING_CACHE, textHash, embedding);
            logger.debug("Cached embedding for text hash: {}", textHash);
        }
        
        return embedding;
    }
    
    private float[] generateEmbedding(String text) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            Map<String, Object> request = new HashMap<>();
            request.put("text", text);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(
                NLP_SERVICE_URL + "/api/nlp/embed", entity, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // Convert response to float array
                // This is a simplified implementation
                return new float[]{1.0f, 2.0f, 3.0f}; // Placeholder
            }
            
        } catch (Exception e) {
            logger.error("Error generating embedding: {}", e.getMessage(), e);
        }
        
        return null;
    }
    
    private String generateTextHash(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (Exception e) {
            logger.error("Error generating text hash: {}", e.getMessage(), e);
            return text.hashCode() + "";
        }
    }
    
    public void evictEmbeddingCache(String text) {
        String textHash = generateTextHash(text);
        cacheService.evictEmbedding(textHash);
    }
}