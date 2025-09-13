import logging
from langdetect import detect, detect_langs
from langdetect.lang_detect_exception import LangDetectException
from app.models.text_processing import Language

logger = logging.getLogger(__name__)


class LanguageDetectionService:
    """Service for detecting language of text."""
    
    def __init__(self):
        self.language_mapping = {
            'en': Language.ENGLISH,
            'vi': Language.VIETNAMESE,
            'zh-cn': Language.CHINESE,
            'zh': Language.CHINESE,
            'ja': Language.JAPANESE,
            'ko': Language.KOREAN,
            'fr': Language.FRENCH,
            'de': Language.GERMAN,
            'es': Language.SPANISH
        }
    
    def detect_language(self, text: str) -> tuple[Language, float]:
        """
        Detect language of the given text.
        
        Args:
            text: Input text to analyze
            
        Returns:
            Tuple of (detected_language, confidence_score)
        """
        if not text or len(text.strip()) < 10:
            return Language.UNKNOWN, 0.0
        
        try:
            # Get language with confidence
            lang_probs = detect_langs(text)
            if not lang_probs:
                return Language.UNKNOWN, 0.0
            
            detected_lang = lang_probs[0].lang
            confidence = lang_probs[0].prob
            
            # Map to our Language enum
            mapped_lang = self.language_mapping.get(detected_lang, Language.UNKNOWN)
            
            logger.info(f"Detected language: {mapped_lang} with confidence: {confidence:.2f}")
            return mapped_lang, confidence
            
        except LangDetectException as e:
            logger.warning(f"Language detection failed: {e}")
            return Language.UNKNOWN, 0.0
        except Exception as e:
            logger.error(f"Unexpected error in language detection: {e}")
            return Language.UNKNOWN, 0.0