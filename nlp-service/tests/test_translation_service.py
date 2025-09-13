import pytest
from app.services.translation_service import TranslationService
from app.models.text_processing import Language


class TestTranslationService:
    
    def setup_method(self):
        self.service = TranslationService()
    
    def test_translate_same_language(self):
        result = self.service.translate_text(
            text="Hello world",
            target_language=Language.ENGLISH,
            source_language=Language.ENGLISH
        )
        assert result.original_text == "Hello world"
        assert result.translated_text == "Hello world"
        assert result.source_language == Language.ENGLISH
        assert result.target_language == Language.ENGLISH
        assert result.confidence == 1.0
    
    def test_translate_empty_text(self):
        result = self.service.translate_text(
            text="",
            target_language=Language.VIETNAMESE
        )
        assert result.original_text == ""
        assert result.translated_text == ""
        assert result.source_language == Language.UNKNOWN
    
    def test_translate_batch_empty(self):
        results = self.service.translate_batch(
            texts=[],
            target_language=Language.ENGLISH
        )
        assert len(results) == 0
    
    def test_translate_batch_basic(self):
        texts = ["Hello", "World"]
        results = self.service.translate_batch(
            texts=texts,
            target_language=Language.VIETNAMESE,
            source_language=Language.ENGLISH
        )
        assert len(results) == 2
        assert all(r.source_language == Language.ENGLISH for r in results)
        assert all(r.target_language == Language.VIETNAMESE for r in results)