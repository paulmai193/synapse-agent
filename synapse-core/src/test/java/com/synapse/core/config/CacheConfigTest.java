package com.synapse.core.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = CacheConfig.class)
@TestPropertySource(properties = {
    "spring.redis.host=localhost",
    "spring.redis.port=6379",
    "spring.redis.database=0"
})
class CacheConfigTest {

    @Test
    void testCacheConfigBeans() {
        CacheConfig config = new CacheConfig();
        
        // Test Redis connection factory creation
        RedisConnectionFactory connectionFactory = config.redisConnectionFactory();
        assertNotNull(connectionFactory);
        
        // Test Redis template creation
        RedisTemplate<String, Object> redisTemplate = config.redisTemplate(connectionFactory);
        assertNotNull(redisTemplate);
        assertNotNull(redisTemplate.getKeySerializer());
        assertNotNull(redisTemplate.getValueSerializer());
        
        // Test cache manager creation
        CacheManager cacheManager = config.cacheManager(connectionFactory);
        assertNotNull(cacheManager);
        
        // Verify cache names are configured
        assertNotNull(cacheManager.getCache("userPermissions"));
        assertNotNull(cacheManager.getCache("roleData"));
        assertNotNull(cacheManager.getCache("documentMetadata"));
        assertNotNull(cacheManager.getCache("searchResults"));
        assertNotNull(cacheManager.getCache("userProfile"));
    }
}