@echo off
setlocal enabledelayedexpansion

set ENVIRONMENT=%1
if "%ENVIRONMENT%"=="" set ENVIRONMENT=dev

echo Starting Synapse deployment for environment: %ENVIRONMENT%

REM Check prerequisites
echo Checking prerequisites...
docker --version >nul 2>&1
if errorlevel 1 (
    echo Docker is not installed
    exit /b 1
)

docker-compose --version >nul 2>&1
if errorlevel 1 (
    echo Docker Compose is not installed
    exit /b 1
)

REM Build images
echo Building Docker images...
docker-compose build --parallel

REM Deploy based on environment
if "%ENVIRONMENT%"=="dev" (
    echo Deploying development environment...
    docker-compose -f docker-compose.yml -f docker-compose.dev.yml up -d
) else if "%ENVIRONMENT%"=="prod" (
    echo Deploying production environment...
    docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
) else if "%ENVIRONMENT%"=="k8s" (
    echo Deploying to Kubernetes...
    kubectl version >nul 2>&1
    if errorlevel 1 (
        echo kubectl is not installed
        exit /b 1
    )
    
    REM Create namespace
    kubectl create namespace synapse --dry-run=client -o yaml | kubectl apply -f -
    
    REM Apply configurations
    kubectl apply -f k8s/configmap.yaml
    kubectl apply -f k8s/secrets.yaml
    kubectl apply -f k8s/postgres.yaml
    kubectl apply -f k8s/backend.yaml
    kubectl apply -f k8s/frontend.yaml
    
    REM Wait for deployments
    kubectl rollout status deployment/synapse-backend -n synapse
    kubectl rollout status deployment/synapse-frontend -n synapse
) else (
    echo Unknown environment: %ENVIRONMENT%
    echo Usage: %0 [dev^|prod^|k8s]
    exit /b 1
)

REM Health check
echo Performing health checks...
timeout /t 30 /nobreak >nul

if "%ENVIRONMENT%"=="k8s" (
    kubectl get pods -n synapse
) else (
    docker-compose ps
)

echo Deployment completed successfully!

REM Display access information
if "%ENVIRONMENT%"=="dev" (
    echo Application is available at:
    echo   Frontend: http://localhost
    echo   Backend API: http://localhost:8080
    echo   NLP Service: http://localhost:8001
) else if "%ENVIRONMENT%"=="prod" (
    echo Application is available at:
    echo   Frontend: http://localhost
    echo   Backend API: http://localhost:8080
    echo   NLP Service: http://localhost:8001
) else if "%ENVIRONMENT%"=="k8s" (
    echo Application is deployed to Kubernetes
    echo Use 'kubectl get ingress -n synapse' to get access URLs
)