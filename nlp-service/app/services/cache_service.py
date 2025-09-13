import json
import hashlib
import logging
from typing import List, Optional, Any
import redis
from config.settings import settings

logger = logging.getLogger(__name__)


class CacheService:
    """Redis-based caching service for embeddings and translations."""
    
    def __init__(self):
        self.redis_client = None
        self._connect()
    
    def _connect(self):
        """Connect to Redis."""
        try:
            self.redis_client = redis.Redis(
                host=settings.redis_host,
                port=settings.redis_port,
                password=settings.redis_password,
                decode_responses=True,
                socket_connect_timeout=5,
                socket_timeout=5
            )
            # Test connection
            self.redis_client.ping()
            logger.info("Connected to Redis successfully")
        except Exception as e:
            logger.warning(f"Redis connection failed: {e}")
            self.redis_client = None
    
    def _generate_key(self, prefix: str, data: str) -> str:
        """Generate cache key from data."""
        hash_obj = hashlib.md5(data.encode('utf-8'))
        return f"{prefix}:{hash_obj.hexdigest()}"
    
    def get_embedding(self, text: str) -> Optional[List[float]]:
        """Get cached embedding for text."""
        if not self.redis_client:
            return None
        
        try:
            key = self._generate_key("embedding", text)
            cached_data = self.redis_client.get(key)
            if cached_data:
                return json.loads(cached_data)
        except Exception as e:
            logger.error(f"Failed to get cached embedding: {e}")
        
        return None
    
    def set_embedding(self, text: str, embedding: List[float], ttl: int = 3600):
        """Cache embedding for text."""
        if not self.redis_client:
            return
        
        try:
            key = self._generate_key("embedding", text)
            self.redis_client.setex(key, ttl, json.dumps(embedding))
        except Exception as e:
            logger.error(f"Failed to cache embedding: {e}")
    
    def get_translation(self, text: str, source_lang: str, target_lang: str) -> Optional[str]:
        """Get cached translation."""
        if not self.redis_client:
            return None
        
        try:
            cache_key = f"{text}:{source_lang}:{target_lang}"
            key = self._generate_key("translation", cache_key)
            return self.redis_client.get(key)
        except Exception as e:
            logger.error(f"Failed to get cached translation: {e}")
        
        return None
    
    def set_translation(self, text: str, source_lang: str, target_lang: str, translation: str, ttl: int = 7200):
        """Cache translation."""
        if not self.redis_client:
            return
        
        try:
            cache_key = f"{text}:{source_lang}:{target_lang}"
            key = self._generate_key("translation", cache_key)
            self.redis_client.setex(key, ttl, translation)
        except Exception as e:
            logger.error(f"Failed to cache translation: {e}")
    
    def clear_cache(self, pattern: str = "*"):
        """Clear cache entries matching pattern."""
        if not self.redis_client:
            return
        
        try:
            keys = self.redis_client.keys(pattern)
            if keys:
                self.redis_client.delete(*keys)
                logger.info(f"Cleared {len(keys)} cache entries")
        except Exception as e:
            logger.error(f"Failed to clear cache: {e}")