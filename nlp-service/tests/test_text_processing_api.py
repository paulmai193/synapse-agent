import pytest
from fastapi.testclient import TestClient
from app.main import app

client = TestClient(app)


class TestTextProcessingAPI:
    
    def test_detect_language_endpoint(self):
        response = client.post(
            "/api/v1/text/detect-language",
            json={"text": "This is an English text for testing."}
        )
        assert response.status_code == 200
        data = response.json()
        assert "detected_language" in data
        assert "confidence" in data
    
    def test_process_text_endpoint(self):
        response = client.post(
            "/api/v1/text/process-text",
            json={
                "text": "This is a sample text for processing and chunking.",
                "chunk_size": 20,
                "chunk_overlap": 5
            }
        )
        assert response.status_code == 200
        data = response.json()
        assert "original_text" in data
        assert "detected_language" in data
        assert "cleaned_text" in data
        assert "chunks" in data
        assert "total_chunks" in data
    
    def test_generate_embedding_endpoint(self):
        response = client.post(
            "/api/v1/text/generate-embedding",
            json={"text": "Sample text for embedding generation"}
        )
        assert response.status_code == 200
        data = response.json()
        assert "text" in data
        assert "embedding" in data
        assert "dimension" in data
        assert isinstance(data["embedding"], list)
        assert len(data["embedding"]) > 0
    
    def test_generate_embeddings_batch_endpoint(self):
        response = client.post(
            "/api/v1/text/generate-embeddings-batch",
            json={"texts": ["First text", "Second text", "Third text"]}
        )
        assert response.status_code == 200
        data = response.json()
        assert "texts" in data
        assert "embeddings" in data
        assert "count" in data
        assert "dimension" in data
        assert len(data["embeddings"]) == 3