#!/bin/bash

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
ENVIRONMENT=${1:-dev}
NAMESPACE="synapse"

echo -e "${GREEN}Starting Synapse deployment for environment: ${ENVIRONMENT}${NC}"

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Check prerequisites
echo -e "${YELLOW}Checking prerequisites...${NC}"
if ! command_exists docker; then
    echo -e "${RED}Docker is not installed${NC}"
    exit 1
fi

if ! command_exists docker-compose; then
    echo -e "${RED}Docker Compose is not installed${NC}"
    exit 1
fi

# Build images
echo -e "${YELLOW}Building Docker images...${NC}"
docker-compose build --parallel

# Deploy based on environment
case $ENVIRONMENT in
    "dev")
        echo -e "${YELLOW}Deploying development environment...${NC}"
        docker-compose -f docker-compose.yml -f docker-compose.dev.yml up -d
        ;;
    "prod")
        echo -e "${YELLOW}Deploying production environment...${NC}"
        docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
        ;;
    "k8s")
        echo -e "${YELLOW}Deploying to Kubernetes...${NC}"
        if ! command_exists kubectl; then
            echo -e "${RED}kubectl is not installed${NC}"
            exit 1
        fi
        
        # Create namespace
        kubectl create namespace $NAMESPACE --dry-run=client -o yaml | kubectl apply -f -
        
        # Apply configurations
        kubectl apply -f k8s/configmap.yaml
        kubectl apply -f k8s/secrets.yaml
        kubectl apply -f k8s/postgres.yaml
        kubectl apply -f k8s/backend.yaml
        kubectl apply -f k8s/frontend.yaml
        
        # Wait for deployments
        kubectl rollout status deployment/synapse-backend -n $NAMESPACE
        kubectl rollout status deployment/synapse-frontend -n $NAMESPACE
        ;;
    *)
        echo -e "${RED}Unknown environment: $ENVIRONMENT${NC}"
        echo "Usage: $0 [dev|prod|k8s]"
        exit 1
        ;;
esac

# Health check
echo -e "${YELLOW}Performing health checks...${NC}"
sleep 30

if [ "$ENVIRONMENT" = "k8s" ]; then
    kubectl get pods -n $NAMESPACE
else
    docker-compose ps
fi

echo -e "${GREEN}Deployment completed successfully!${NC}"

# Display access information
case $ENVIRONMENT in
    "dev"|"prod")
        echo -e "${GREEN}Application is available at:${NC}"
        echo "  Frontend: http://localhost"
        echo "  Backend API: http://localhost:8080"
        echo "  NLP Service: http://localhost:8001"
        ;;
    "k8s")
        echo -e "${GREEN}Application is deployed to Kubernetes${NC}"
        echo "Use 'kubectl get ingress -n $NAMESPACE' to get access URLs"
        ;;
esac