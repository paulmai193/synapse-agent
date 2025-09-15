# Synapse - AI-Powered Knowledge Management Agent

Synapse is a multilingual AI-powered knowledge management system that automates information extraction and synthesis from internal documents and project systems. The system provides intelligent search capabilities and context-aware Q&A functionality with granular role-based access control and comprehensive audit logging.

## 🏗️ Architecture

- **Backend**: Java/Spring Boot with multi-module architecture
- **Frontend**: React with TypeScript and Material-UI
- **NLP Service**: Python/FastAPI with multilingual support
- **Databases**: PostgreSQL, MongoDB, Redis, Qdrant Vector DB
- **Monitoring**: Prometheus, Grafana, ELK Stack

## 🚀 Quick Start

### Prerequisites

- Docker and Docker Compose
- Java 17+ (for development)
- Node.js 18+ (for development)
- Python 3.11+ (for development)

### Development Environment

1. Clone the repository:
```bash
git clone <repository-url>
cd synapse-agent
```

2. Copy environment configuration:
```bash
cp .env.example .env
```

3. Start development environment:
```bash
./deploy.sh dev
```

4. Access the application:
- Frontend: http://localhost
- Backend API: http://localhost:8080
- NLP Service: http://localhost:8001

### Production Deployment

1. Configure production environment:
```bash
cp .env.example .env.prod
# Edit .env.prod with production values
```

2. Deploy to production:
```bash
./deploy.sh prod
```

### Kubernetes Deployment

1. Configure Kubernetes secrets:
```bash
kubectl create secret generic synapse-secrets \
  --from-literal=postgres-password=<password> \
  --from-literal=mongodb-password=<password> \
  --from-literal=redis-password=<password> \
  --from-literal=jwt-secret=<secret>
```

2. Deploy to Kubernetes:
```bash
./deploy.sh k8s
```

## 📊 Monitoring

### Start Monitoring Stack

```bash
docker-compose -f docker-compose.monitoring.yml up -d
```

### Access Monitoring Tools

- Prometheus: http://localhost:9090
- Grafana: http://localhost:3001 (admin/admin)
- Kibana: http://localhost:5601
- Alertmanager: http://localhost:9093

## 🔒 Security Features

- JWT-based authentication
- Role-based access control (RBAC)
- Project/Department isolation
- Comprehensive audit logging
- Input validation and sanitization
- Rate limiting and DDoS protection

## 🌐 Multilingual Support

- Vietnamese, English, Japanese, and more
- Automatic language detection
- Cross-language search capabilities
- Multilingual Q&A responses

## 📚 API Documentation

API documentation is available at:
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI Spec: http://localhost:8080/v3/api-docs

## 🔧 Development

### Backend Development

```bash
cd synapse-web
mvn spring-boot:run
```

### Frontend Development

```bash
cd synapse-frontend
npm install
npm run dev
```

### NLP Service Development

```bash
cd nlp-service
pip install -r requirements.txt
uvicorn app.main:app --reload --port 8001
```

## 🧪 Testing

### Run All Tests

```bash
mvn test  # Backend tests
npm test  # Frontend tests
pytest    # NLP service tests
```

### Performance Testing

```bash
./performance-test-runner.sh
```

## 💾 Backup and Recovery

### Create Backup

```bash
./backup/backup.sh
```

### Restore from Backup

```bash
./backup/restore.sh /backup/synapse_backup_YYYYMMDD_HHMMSS.tar.gz
```

## 📈 Performance Targets

- Search response time: < 3 seconds
- Q&A response time: < 5 seconds
- System availability: 99.9%
- Concurrent users: 500+

## 🛠️ Configuration

### Environment Variables

See `.env.example` for all available configuration options.

### Database Configuration

- PostgreSQL: User management, audit logs
- MongoDB: Document storage, search analytics
- Redis: Caching, session management
- Qdrant: Vector embeddings for semantic search

## 📋 User Roles

- **SYSTEM_ADMIN**: Full system access
- **PROJECT_ADMIN**: Project-level administration
- **DEPARTMENT_ADMIN**: Department-level administration
- **USER**: Basic search and Q&A access

## 🔍 Features

### Document Management
- Multi-format support (PDF, DOCX, TXT)
- Automatic content extraction
- Access control assignment
- Status management

### Search & Q&A
- Semantic search across documents
- Multilingual query processing
- Context-aware answers
- Source citations
- Feedback collection

### Administration
- User management
- Role assignment
- Project/Department management
- Audit log viewing
- Analytics dashboard

## 🐛 Troubleshooting

### Common Issues

1. **Services not starting**: Check Docker logs
```bash
docker-compose logs <service-name>
```

2. **Database connection issues**: Verify environment variables
3. **Search not working**: Check Qdrant vector database status
4. **Authentication issues**: Verify JWT secret configuration

### Health Checks

```bash
# Check all services
docker-compose ps

# Check specific service health
curl http://localhost:8080/actuator/health
curl http://localhost:8001/health/
```

## 📞 Support

For support and questions:
- Check the troubleshooting section
- Review application logs
- Contact the development team

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.