from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
import logging
from config.logging import setup_logging
from config.settings import settings
from app.api.health import router as health_router
from app.api.text_processing import router as text_processing_router
from app.api.translation import router as translation_router
from app.api.cache import router as cache_router

# Setup logging
setup_logging()
logger = logging.getLogger(__name__)

app = FastAPI(
    title="Synapse NLP Microservice",
    description="Natural Language Processing service for multilingual text processing",
    version="1.0.0"
)

# CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Include routers
app.include_router(health_router, prefix="/health", tags=["health"])
app.include_router(text_processing_router, prefix="/api/v1/text", tags=["text-processing"])
app.include_router(translation_router, prefix="/api/v1/translation", tags=["translation"])
app.include_router(cache_router, prefix="/api/v1/cache", tags=["cache"])

@app.on_event("startup")
async def startup_event():
    logger.info("Starting NLP Microservice...")
    logger.info(f"Using embedding model: {settings.embedding_model}")

@app.on_event("shutdown")
async def shutdown_event():
    logger.info("Shutting down NLP Microservice...")

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8001)