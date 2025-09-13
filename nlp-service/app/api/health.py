from fastapi import APIRouter
from pydantic import BaseModel
import redis
from config.settings import settings
import logging

logger = logging.getLogger(__name__)
router = APIRouter()


class HealthResponse(BaseModel):
    status: str
    redis_connected: bool
    embedding_model: str


@router.get("/", response_model=HealthResponse)
async def health_check():
    """Health check endpoint."""
    redis_connected = False
    
    try:
        r = redis.Redis(
            host=settings.redis_host,
            port=settings.redis_port,
            password=settings.redis_password,
            decode_responses=True
        )
        r.ping()
        redis_connected = True
    except Exception as e:
        logger.warning(f"Redis connection failed: {e}")
    
    return HealthResponse(
        status="healthy",
        redis_connected=redis_connected,
        embedding_model=settings.embedding_model
    )