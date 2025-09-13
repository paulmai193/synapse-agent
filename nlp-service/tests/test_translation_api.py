import pytest
from fastapi.testclient import TestClient
from app.main import app

client = TestClient(app)


class TestTranslationAPI:
    
    def test_translate_endpoint(self):
        response = client.post(
            "/api/v1/translation/translate",
            json={
                "text": "Hello world",
                "target_language": "vi",
                "source_language": "en",
                "auto_detect": False
            }
        )
        assert response.status_code == 200
        data = response.json()
        assert "original_text" in data
        assert "translated_text" in data
        assert "source_language" in data
        assert "target_language" in data
        assert data["original_text"] == "Hello world"
        assert data["source_language"] == "en"
        assert data["target_language"] == "vi"
    
    def test_translate_batch_endpoint(self):
        response = client.post(
            "/api/v1/translation/translate-batch",
            json={
                "texts": ["Hello", "World"],
                "target_language": "vi",
                "source_language": "en",
                "auto_detect": False
            }
        )
        assert response.status_code == 200
        data = response.json()
        assert "translations" in data
        assert "total_count" in data
        assert data["total_count"] == 2
        assert len(data["translations"]) == 2
    
    def test_cache_status_endpoint(self):
        response = client.get("/api/v1/cache/status")
        assert response.status_code == 200
        data = response.json()
        assert "redis_connected" in data
        assert "status" in data
    
    def test_cache_clear_endpoint(self):
        response = client.delete("/api/v1/cache/clear?pattern=test:*")
        assert response.status_code == 200
        data = response.json()
        assert "message" in data