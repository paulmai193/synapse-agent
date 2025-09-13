from fastapi import APIRouter, HTTPException
import logging
from app.models.text_processing import (
    TextProcessingRequest, TextProcessingResponse,
    LanguageDetectionRequest, LanguageDetectionResponse
)
from app.services.language_detection import LanguageDetectionService
from app.services.text_processor import TextProcessor
from app.services.embedding_service import EmbeddingService

logger = logging.getLogger(__name__)
router = APIRouter()

# Initialize services
language_detector = LanguageDetectionService()
text_processor = TextProcessor()
embedding_service = EmbeddingService()


@router.post("/detect-language", response_model=LanguageDetectionResponse)
async def detect_language(request: LanguageDetectionRequest):
    """Detect the language of input text."""
    try:
        detected_lang, confidence = language_detector.detect_language(request.text)
        return LanguageDetectionResponse(
            detected_language=detected_lang,
            confidence=confidence
        )
    except Exception as e:
        logger.error(f"Language detection failed: {e}")
        raise HTTPException(status_code=500, detail="Language detection failed")


@router.post("/process-text", response_model=TextProcessingResponse)
async def process_text(request: TextProcessingRequest):
    """Process text: detect language, clean, and chunk."""
    try:
        # Detect language if not provided
        if request.language is None:
            detected_lang, _ = language_detector.detect_language(request.text)
        else:
            detected_lang = request.language
        
        # Clean text
        cleaned_text = text_processor.clean_text(request.text, detected_lang)
        
        # Chunk text
        chunks = text_processor.chunk_text(
            cleaned_text, 
            request.chunk_size, 
            request.chunk_overlap
        )
        
        return TextProcessingResponse(
            original_text=request.text,
            detected_language=detected_lang,
            cleaned_text=cleaned_text,
            chunks=chunks,
            total_chunks=len(chunks)
        )
    except Exception as e:
        logger.error(f"Text processing failed: {e}")
        raise HTTPException(status_code=500, detail="Text processing failed")


@router.post("/generate-embedding")
async def generate_embedding(request: dict):
    """Generate embedding for text."""
    try:
        text = request.get("text", "")
        if not text:
            raise HTTPException(status_code=400, detail="Text is required")
        
        embedding = embedding_service.generate_embedding(text)
        if not embedding:
            raise HTTPException(status_code=500, detail="Failed to generate embedding")
        
        return {
            "text": text,
            "embedding": embedding,
            "dimension": len(embedding)
        }
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Embedding generation failed: {e}")
        raise HTTPException(status_code=500, detail="Embedding generation failed")


@router.post("/generate-embeddings-batch")
async def generate_embeddings_batch(request: dict):
    """Generate embeddings for multiple texts."""
    try:
        texts = request.get("texts", [])
        if not texts or not isinstance(texts, list):
            raise HTTPException(status_code=400, detail="Texts array is required")
        
        embeddings = embedding_service.generate_embeddings_batch(texts)
        if not embeddings:
            raise HTTPException(status_code=500, detail="Failed to generate embeddings")
        
        return {
            "texts": texts,
            "embeddings": embeddings,
            "count": len(embeddings),
            "dimension": len(embeddings[0]) if embeddings else 0
        }
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Batch embedding generation failed: {e}")
        raise HTTPException(status_code=500, detail="Batch embedding generation failed")