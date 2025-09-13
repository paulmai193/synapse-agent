import re
import logging
from typing import List
from app.models.text_processing import Language, TextChunk

logger = logging.getLogger(__name__)


class TextProcessor:
    """Service for text preprocessing and cleaning."""
    
    def __init__(self):
        # Common patterns for cleaning
        self.url_pattern = re.compile(r'http[s]?://(?:[a-zA-Z]|[0-9]|[$-_@.&+]|[!*\\(\\),]|(?:%[0-9a-fA-F][0-9a-fA-F]))+')
        self.email_pattern = re.compile(r'\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,}\b')
        self.extra_whitespace = re.compile(r'\s+')
    
    def clean_text(self, text: str, language: Language = Language.ENGLISH) -> str:
        """
        Clean and preprocess text for the given language.
        
        Args:
            text: Input text to clean
            language: Language of the text
            
        Returns:
            Cleaned text
        """
        if not text:
            return ""
        
        # Remove URLs and emails
        text = self.url_pattern.sub(' ', text)
        text = self.email_pattern.sub(' ', text)
        
        # Language-specific cleaning
        if language in [Language.CHINESE, Language.JAPANESE, Language.KOREAN]:
            text = self._clean_cjk_text(text)
        else:
            text = self._clean_latin_text(text)
        
        # Normalize whitespace
        text = self.extra_whitespace.sub(' ', text)
        text = text.strip()
        
        logger.debug(f"Cleaned text length: {len(text)}")
        return text
    
    def _clean_latin_text(self, text: str) -> str:
        """Clean text for Latin-based languages."""
        # Remove special characters but keep basic punctuation
        text = re.sub(r'[^\w\s\.\,\!\?\;\:\-\(\)]', ' ', text)
        return text
    
    def _clean_cjk_text(self, text: str) -> str:
        """Clean text for CJK languages."""
        # Keep CJK characters and basic punctuation
        text = re.sub(r'[^\u4e00-\u9fff\u3040-\u309f\u30a0-\u30ff\uac00-\ud7af\w\s\.\,\!\?\;\:\-\(\)]', ' ', text)
        return text
    
    def chunk_text(self, text: str, chunk_size: int = 1000, overlap: int = 200) -> List[TextChunk]:
        """
        Split text into overlapping chunks.
        
        Args:
            text: Input text to chunk
            chunk_size: Maximum size of each chunk
            overlap: Number of characters to overlap between chunks
            
        Returns:
            List of TextChunk objects
        """
        if not text or chunk_size <= 0:
            return []
        
        chunks = []
        start = 0
        chunk_id = 0
        
        while start < len(text):
            end = min(start + chunk_size, len(text))
            
            # Try to break at sentence boundary if possible
            if end < len(text):
                # Look for sentence endings within the last 100 characters
                sentence_end = self._find_sentence_boundary(text, end - 100, end)
                if sentence_end > start:
                    end = sentence_end
            
            chunk_text = text[start:end].strip()
            if chunk_text:
                chunks.append(TextChunk(
                    text=chunk_text,
                    start_index=start,
                    end_index=end,
                    chunk_id=chunk_id
                ))
                chunk_id += 1
            
            # Move start position with overlap
            start = max(start + 1, end - overlap)
            
            # Prevent infinite loop
            if start >= end:
                break
        
        logger.info(f"Created {len(chunks)} chunks from text of length {len(text)}")
        return chunks
    
    def _find_sentence_boundary(self, text: str, start: int, end: int) -> int:
        """Find the best sentence boundary within the given range."""
        sentence_endings = ['.', '!', '?', '。', '！', '？']
        
        for i in range(end - 1, start - 1, -1):
            if text[i] in sentence_endings:
                # Check if next character is whitespace or end of text
                if i + 1 >= len(text) or text[i + 1].isspace():
                    return i + 1
        
        return end