# Synapse NLP Microservice

A FastAPI-based microservice for natural language processing operations including multilingual text processing, embedding generation, and translation services.

## Features

- Multilingual text processing and language detection
- Embedding generation using sentence-transformers
- Translation services integration
- Redis caching for performance optimization
- Health check endpoints
- Docker containerization support

## Setup

### Local Development

1. Create virtual environment:
```bash
python -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate
```

2. Install dependencies:
```bash
pip install -r requirements.txt
```

3. Copy environment configuration:
```bash
cp .env.example .env
```

4. Run the application:
```bash
uvicorn app.main:app --reload --port 8001
```

### Docker Deployment

1. Build the image:
```bash
docker build -t synapse-nlp .
```

2. Run the container:
```bash
docker run -p 8001:8001 synapse-nlp
```

## API Documentation

Once running, visit:
- API Documentation: http://localhost:8001/docs
- Health Check: http://localhost:8001/health/

## Testing

Run tests with:
```bash
pytest
```