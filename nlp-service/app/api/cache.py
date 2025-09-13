from fastapi import APIRouter, HTTPException
import logging
from app.services.cache_service import CacheService

logger = logging.getLogger(__name__)
router = APIRouter()

# Initialize service
cache_service = CacheService()


@router.delete("/clear")
async def clear_cache(pattern: str = "*"):
    """Clear cache entries matching pattern."""
    try:
        cache_service.clear_cache(pattern)
        return {"message": f"Cache cleared for pattern: {pattern}"}
    except Exception as e:
        logger.error(f"Cache clear failed: {e}")
        raise HTTPException(status_code=500, detail="Cache clear failed")


@router.get("/status")
async def cache_status():
    """Get cache service status."""
    try:
        is_connected = cache_service.redis_client is not None
        if is_connected:
            cache_service.redis_client.ping()
        
        return {
            "redis_connected": is_connected,
            "status": "healthy" if is_connected else "disconnected"
        }
    except Exception as e:
        logger.error(f"Cache status check failed: {e}")
        return {
            "redis_connected": False,
            "status": "error",
            "error": str(e)
        }