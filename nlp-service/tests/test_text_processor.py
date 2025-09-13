import pytest
from app.services.text_processor import TextProcessor
from app.models.text_processing import Language


class TestTextProcessor:
    
    def setup_method(self):
        self.processor = TextProcessor()
    
    def test_clean_text_basic(self):
        text = "This is a test   with   extra spaces."
        cleaned = self.processor.clean_text(text)
        assert cleaned == "This is a test with extra spaces."
    
    def test_clean_text_with_urls(self):
        text = "Check this link https://example.com for more info."
        cleaned = self.processor.clean_text(text)
        assert "https://example.com" not in cleaned
        assert "Check this link" in cleaned
    
    def test_clean_text_with_emails(self):
        text = "Contact us at test@example.com for support."
        cleaned = self.processor.clean_text(text)
        assert "test@example.com" not in cleaned
        assert "Contact us at" in cleaned
    
    def test_chunk_text_basic(self):
        text = "This is a long text that needs to be chunked into smaller pieces for processing."
        chunks = self.processor.chunk_text(text, chunk_size=30, overlap=10)
        assert len(chunks) > 1
        assert all(chunk.chunk_id >= 0 for chunk in chunks)
        assert all(len(chunk.text) <= 30 for chunk in chunks)
    
    def test_chunk_text_empty(self):
        chunks = self.processor.chunk_text("")
        assert len(chunks) == 0
    
    def test_chunk_text_short(self):
        text = "Short text"
        chunks = self.processor.chunk_text(text, chunk_size=100)
        assert len(chunks) == 1
        assert chunks[0].text == text