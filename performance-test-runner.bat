@echo off
REM Synapse Performance Test Runner for Windows
echo Starting Synapse Performance Tests...

REM Set test environment
set SPRING_PROFILES_ACTIVE=load-test

REM Check required services
echo Checking required services...

REM Check if PostgreSQL is running
pg_isready -h localhost -p 5432 >nul 2>&1
if errorlevel 1 (
    echo Warning: PostgreSQL is not running on localhost:5432
)

REM Check if MongoDB is running
mongosh --eval "db.runCommand('ping')" >nul 2>&1
if errorlevel 1 (
    echo Warning: MongoDB is not running on localhost:27017
)

REM Check if Redis is running
redis-cli ping >nul 2>&1
if errorlevel 1 (
    echo Warning: Redis is not running on localhost:6379
)

REM Run performance tests
echo Running performance tests...

REM Run search performance tests
echo 1. Running search performance tests...
mvn test -Dtest=SearchPerformanceTest -Dspring.profiles.active=load-test

REM Run document processing performance tests
echo 2. Running document processing performance tests...
mvn test -Dtest=DocumentProcessingPerformanceTest -Dspring.profiles.active=load-test

REM Run embedding generation performance tests
echo 3. Running embedding generation performance tests...
mvn test -Dtest=EmbeddingGenerationPerformanceTest -Dspring.profiles.active=load-test

REM Run system health monitoring tests
echo 4. Running system health monitoring tests...
mvn test -Dtest=SystemHealthMonitorTest -Dspring.profiles.active=load-test

REM Generate performance report
echo Generating performance report...
mvn failsafe:integration-test -Pperformance-test

echo Performance tests completed. Check target/failsafe-reports for detailed results.
pause