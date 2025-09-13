import logging
from typing import List, Optional
from deep_translator import GoogleTranslator
from app.models.text_processing import Language
from app.models.translation import TranslationResponse
from app.services.language_detection import LanguageDetectionService
from app.services.cache_service import CacheService

logger = logging.getLogger(__name__)


class TranslationService:
    """Service for text translation using deep-translator."""
    
    def __init__(self):
        self.language_detector = LanguageDetectionService()
        self.cache_service = CacheService()
        self.language_mapping = {
            Language.ENGLISH: 'en',
            Language.VIETNAMESE: 'vi',
            Language.CHINESE: 'zh',
            Language.JAPANESE: 'ja',
            Language.KOREAN: 'ko',
            Language.FRENCH: 'fr',
            Language.GERMAN: 'de',
            Language.SPANISH: 'es'
        }
        self.reverse_mapping = {v: k for k, v in self.language_mapping.items()}
    
    def translate_text(
        self, 
        text: str, 
        target_language: Language,
        source_language: Optional[Language] = None,
        auto_detect: bool = True
    ) -> TranslationResponse:
        """
        Translate text to target language.
        
        Args:
            text: Text to translate
            target_language: Target language
            source_language: Source language (optional)
            auto_detect: Whether to auto-detect source language
            
        Returns:
            TranslationResponse with translation results
        """
        if not text or not text.strip():
            return TranslationResponse(
                original_text=text,
                translated_text=text,
                source_language=Language.UNKNOWN,
                target_language=target_language
            )
        
        # Detect source language if not provided
        if source_language is None and auto_detect:
            detected_lang, confidence = self.language_detector.detect_language(text)
            source_language = detected_lang
        elif source_language is None:
            source_language = Language.ENGLISH
        
        # Skip translation if source and target are the same
        if source_language == target_language:
            return TranslationResponse(
                original_text=text,
                translated_text=text,
                source_language=source_language,
                target_language=target_language,
                confidence=1.0
            )
        
        try:
            # Check cache first
            source_code = self.language_mapping.get(source_language, 'auto')
            target_code = self.language_mapping.get(target_language, 'en')
            
            cached_translation = self.cache_service.get_translation(text, source_code, target_code)
            if cached_translation:
                logger.debug("Retrieved translation from cache")
                return TranslationResponse(
                    original_text=text,
                    translated_text=cached_translation,
                    source_language=source_language,
                    target_language=target_language,
                    confidence=0.9
                )
            
            # Perform translation
            translator = GoogleTranslator(source=source_code, target=target_code)
            translated_text = translator.translate(text)
            
            # Cache the translation
            self.cache_service.set_translation(text, source_code, target_code, translated_text)
            
            logger.info(f"Translated text from {source_language} to {target_language}")
            
            return TranslationResponse(
                original_text=text,
                translated_text=translated_text,
                source_language=source_language,
                target_language=target_language,
                confidence=0.9  # Default confidence for successful translation
            )
            
        except Exception as e:
            logger.error(f"Translation failed: {e}")
            # Return original text if translation fails
            return TranslationResponse(
                original_text=text,
                translated_text=text,
                source_language=source_language,
                target_language=target_language,
                confidence=0.0
            )
    
    def translate_batch(
        self,
        texts: List[str],
        target_language: Language,
        source_language: Optional[Language] = None,
        auto_detect: bool = True
    ) -> List[TranslationResponse]:
        """
        Translate multiple texts.
        
        Args:
            texts: List of texts to translate
            target_language: Target language
            source_language: Source language (optional)
            auto_detect: Whether to auto-detect source language
            
        Returns:
            List of TranslationResponse objects
        """
        if not texts:
            return []
        
        results = []
        for text in texts:
            result = self.translate_text(
                text=text,
                target_language=target_language,
                source_language=source_language,
                auto_detect=auto_detect
            )
            results.append(result)
        
        return results