import pytest
from app.services.language_detection import LanguageDetectionService
from app.models.text_processing import Language


class TestLanguageDetectionService:
    
    def setup_method(self):
        self.service = LanguageDetectionService()
    
    def test_detect_english(self):
        text = "This is a sample English text for language detection testing."
        language, confidence = self.service.detect_language(text)
        assert language == Language.ENGLISH
        assert confidence > 0.5
    
    def test_detect_vietnamese(self):
        text = "Đây là một văn bản tiếng Việt để kiểm tra việc phát hiện ngôn ngữ."
        language, confidence = self.service.detect_language(text)
        assert language == Language.VIETNAMESE
        assert confidence > 0.5
    
    def test_detect_empty_text(self):
        language, confidence = self.service.detect_language("")
        assert language == Language.UNKNOWN
        assert confidence == 0.0
    
    def test_detect_short_text(self):
        language, confidence = self.service.detect_language("Hi")
        assert language == Language.UNKNOWN
        assert confidence == 0.0