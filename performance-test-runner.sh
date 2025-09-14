#!/bin/bash

# Synapse Performance Test Runner
echo "Starting Synapse Performance Tests..."

# Set test environment
export SPRING_PROFILES_ACTIVE=load-test

# Start required services (if not already running)
echo "Checking required services..."

# Check if PostgreSQL is running
if ! pg_isready -h localhost -p 5432 > /dev/null 2>&1; then
    echo "Warning: PostgreSQL is not running on localhost:5432"
fi

# Check if MongoDB is running
if ! mongosh --eval "db.runCommand('ping')" > /dev/null 2>&1; then
    echo "Warning: MongoDB is not running on localhost:27017"
fi

# Check if Redis is running
if ! redis-cli ping > /dev/null 2>&1; then
    echo "Warning: Redis is not running on localhost:6379"
fi

# Run performance tests
echo "Running performance tests..."

# Run search performance tests
echo "1. Running search performance tests..."
mvn test -Dtest=SearchPerformanceTest -Dspring.profiles.active=load-test

# Run document processing performance tests
echo "2. Running document processing performance tests..."
mvn test -Dtest=DocumentProcessingPerformanceTest -Dspring.profiles.active=load-test

# Run embedding generation performance tests
echo "3. Running embedding generation performance tests..."
mvn test -Dtest=EmbeddingGenerationPerformanceTest -Dspring.profiles.active=load-test

# Run system health monitoring tests
echo "4. Running system health monitoring tests..."
mvn test -Dtest=SystemHealthMonitorTest -Dspring.profiles.active=load-test

# Generate performance report
echo "Generating performance report..."
mvn failsafe:integration-test -Pperformance-test

echo "Performance tests completed. Check target/failsafe-reports for detailed results."