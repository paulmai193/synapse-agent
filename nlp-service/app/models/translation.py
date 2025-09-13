from pydantic import BaseModel
from typing import List, Optional
from app.models.text_processing import Language


class TranslationRequest(BaseModel):
    text: str
    source_language: Optional[Language] = None
    target_language: Language
    auto_detect: bool = True


class TranslationResponse(BaseModel):
    original_text: str
    translated_text: str
    source_language: Language
    target_language: Language
    confidence: Optional[float] = None


class BatchTranslationRequest(BaseModel):
    texts: List[str]
    source_language: Optional[Language] = None
    target_language: Language
    auto_detect: bool = True


class BatchTranslationResponse(BaseModel):
    translations: List[TranslationResponse]
    total_count: int