from fastapi import APIRouter, HTTPException
import logging
from app.models.translation import (
    TranslationRequest, TranslationResponse,
    BatchTranslationRequest, BatchTranslationResponse
)
from app.services.translation_service import TranslationService

logger = logging.getLogger(__name__)
router = APIRouter()

# Initialize service
translation_service = TranslationService()


@router.post("/translate", response_model=TranslationResponse)
async def translate_text(request: TranslationRequest):
    """Translate text to target language."""
    try:
        result = translation_service.translate_text(
            text=request.text,
            target_language=request.target_language,
            source_language=request.source_language,
            auto_detect=request.auto_detect
        )
        return result
    except Exception as e:
        logger.error(f"Translation failed: {e}")
        raise HTTPException(status_code=500, detail="Translation failed")


@router.post("/translate-batch", response_model=BatchTranslationResponse)
async def translate_batch(request: BatchTranslationRequest):
    """Translate multiple texts to target language."""
    try:
        results = translation_service.translate_batch(
            texts=request.texts,
            target_language=request.target_language,
            source_language=request.source_language,
            auto_detect=request.auto_detect
        )
        
        return BatchTranslationResponse(
            translations=results,
            total_count=len(results)
        )
    except Exception as e:
        logger.error(f"Batch translation failed: {e}")
        raise HTTPException(status_code=500, detail="Batch translation failed")