from pydantic import BaseModel
from typing import List, Optional
from enum import Enum


class Language(str, Enum):
    ENGLISH = "en"
    VIETNAMESE = "vi"
    CHINESE = "zh"
    JAPANESE = "ja"
    KOREAN = "ko"
    FRENCH = "fr"
    GERMAN = "de"
    SPANISH = "es"
    UNKNOWN = "unknown"


class TextProcessingRequest(BaseModel):
    text: str
    language: Optional[Language] = None
    chunk_size: int = 1000
    chunk_overlap: int = 200


class TextChunk(BaseModel):
    text: str
    start_index: int
    end_index: int
    chunk_id: int


class TextProcessingResponse(BaseModel):
    original_text: str
    detected_language: Language
    cleaned_text: str
    chunks: List[TextChunk]
    total_chunks: int


class LanguageDetectionRequest(BaseModel):
    text: str


class LanguageDetectionResponse(BaseModel):
    detected_language: Language
    confidence: float