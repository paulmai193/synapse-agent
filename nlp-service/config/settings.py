import os
from pydantic import BaseSettings
from typing import Optional


class Settings(BaseSettings):
    redis_host: str = "localhost"
    redis_port: int = 6379
    redis_password: Optional[str] = None
    log_level: str = "INFO"
    embedding_model: str = "sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2"
    translation_service: str = "google"
    google_translate_api_key: Optional[str] = None
    
    class Config:
        env_file = ".env"


settings = Settings()