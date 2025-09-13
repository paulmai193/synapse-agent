import pytest
from app.services.cache_service import CacheService


class TestCacheService:
    
    def setup_method(self):
        self.service = CacheService()
    
    def test_generate_key(self):
        key1 = self.service._generate_key("test", "hello world")
        key2 = self.service._generate_key("test", "hello world")
        key3 = self.service._generate_key("test", "different text")
        
        assert key1 == key2
        assert key1 != key3
        assert key1.startswith("test:")
    
    def test_embedding_cache_operations(self):
        text = "test text"
        embedding = [0.1, 0.2, 0.3]
        
        # Test cache miss
        cached = self.service.get_embedding(text)
        assert cached is None
        
        # Test cache set and get
        self.service.set_embedding(text, embedding)
        cached = self.service.get_embedding(text)
        
        # Only assert if Redis is available
        if self.service.redis_client:
            assert cached == embedding
    
    def test_translation_cache_operations(self):
        text = "hello"
        source_lang = "en"
        target_lang = "vi"
        translation = "xin chào"
        
        # Test cache miss
        cached = self.service.get_translation(text, source_lang, target_lang)
        assert cached is None
        
        # Test cache set and get
        self.service.set_translation(text, source_lang, target_lang, translation)
        cached = self.service.get_translation(text, source_lang, target_lang)
        
        # Only assert if Redis is available
        if self.service.redis_client:
            assert cached == translation