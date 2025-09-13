import logging
from typing import List
from sentence_transformers import SentenceTransformer
from config.settings import settings
from app.services.cache_service import CacheService

logger = logging.getLogger(__name__)


class EmbeddingService:
    """Service for generating text embeddings using sentence-transformers."""
    
    def __init__(self):
        self.model = None
        self.cache_service = CacheService()
        self._load_model()
    
    def _load_model(self):
        """Load the embedding model."""
        try:
            logger.info(f"Loading embedding model: {settings.embedding_model}")
            self.model = SentenceTransformer(settings.embedding_model)
            logger.info("Embedding model loaded successfully")
        except Exception as e:
            logger.error(f"Failed to load embedding model: {e}")
            raise
    
    def generate_embedding(self, text: str) -> List[float]:
        """
        Generate embedding for a single text.
        
        Args:
            text: Input text
            
        Returns:
            List of float values representing the embedding
        """
        if not self.model:
            raise RuntimeError("Embedding model not loaded")
        
        if not text or not text.strip():
            return []
        
        # Check cache first
        cached_embedding = self.cache_service.get_embedding(text.strip())
        if cached_embedding:
            logger.debug("Retrieved embedding from cache")
            return cached_embedding
        
        try:
            embedding = self.model.encode(text.strip())
            embedding_list = embedding.tolist()
            
            # Cache the embedding
            self.cache_service.set_embedding(text.strip(), embedding_list)
            
            return embedding_list
        except Exception as e:
            logger.error(f"Failed to generate embedding: {e}")
            return []
    
    def generate_embeddings_batch(self, texts: List[str]) -> List[List[float]]:
        """
        Generate embeddings for multiple texts in batch.
        
        Args:
            texts: List of input texts
            
        Returns:
            List of embeddings (each embedding is a list of floats)
        """
        if not self.model:
            raise RuntimeError("Embedding model not loaded")
        
        if not texts:
            return []
        
        # Filter out empty texts
        valid_texts = [text.strip() for text in texts if text and text.strip()]
        if not valid_texts:
            return []
        
        try:
            embeddings = self.model.encode(valid_texts)
            return [embedding.tolist() for embedding in embeddings]
        except Exception as e:
            logger.error(f"Failed to generate batch embeddings: {e}")
            return []