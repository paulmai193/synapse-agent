#!/bin/bash

set -e

# Configuration
BACKUP_DIR="/backup"
BACKUP_FILE=$1

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

if [ -z "$BACKUP_FILE" ]; then
    echo -e "${RED}Usage: $0 <backup_file.tar.gz>${NC}"
    echo "Available backups:"
    ls -la $BACKUP_DIR/synapse_backup_*.tar.gz 2>/dev/null || echo "No backups found"
    exit 1
fi

if [ ! -f "$BACKUP_FILE" ]; then
    echo -e "${RED}Backup file not found: $BACKUP_FILE${NC}"
    exit 1
fi

echo -e "${GREEN}Starting Synapse restore process...${NC}"
echo -e "${YELLOW}Backup file: $BACKUP_FILE${NC}"

# Extract backup
TEMP_DIR=$(mktemp -d)
echo -e "${YELLOW}Extracting backup...${NC}"
tar -xzf $BACKUP_FILE -C $TEMP_DIR

# Find the backup directory
BACKUP_EXTRACT_DIR=$(find $TEMP_DIR -maxdepth 1 -type d -name "*" | grep -v "^$TEMP_DIR$" | head -1)

if [ -z "$BACKUP_EXTRACT_DIR" ]; then
    echo -e "${RED}Invalid backup file structure${NC}"
    rm -rf $TEMP_DIR
    exit 1
fi

# Stop services
echo -e "${YELLOW}Stopping Synapse services...${NC}"
docker-compose down

# Restore PostgreSQL
echo -e "${YELLOW}Restoring PostgreSQL database...${NC}"
docker-compose up -d postgres
sleep 10
docker exec -i synapse-postgres psql -U synapse -d synapse < $BACKUP_EXTRACT_DIR/postgres_backup.sql
if [ $? -eq 0 ]; then
    echo -e "${GREEN}PostgreSQL restore completed${NC}"
else
    echo -e "${RED}PostgreSQL restore failed${NC}"
    exit 1
fi

# Restore MongoDB
echo -e "${YELLOW}Restoring MongoDB database...${NC}"
docker-compose up -d mongodb
sleep 10
docker cp $BACKUP_EXTRACT_DIR/mongodb_backup synapse-mongodb:/tmp/
docker exec synapse-mongodb mongorestore --username synapse --password synapse_password --authenticationDatabase admin --db synapse /tmp/mongodb_backup/synapse
if [ $? -eq 0 ]; then
    echo -e "${GREEN}MongoDB restore completed${NC}"
else
    echo -e "${RED}MongoDB restore failed${NC}"
    exit 1
fi

# Restore Redis
echo -e "${YELLOW}Restoring Redis data...${NC}"
docker-compose up -d redis
sleep 5
docker cp $BACKUP_EXTRACT_DIR/redis_backup.rdb synapse-redis:/tmp/
docker exec synapse-redis redis-cli --rdb /tmp/redis_backup.rdb
if [ $? -eq 0 ]; then
    echo -e "${GREEN}Redis restore completed${NC}"
else
    echo -e "${RED}Redis restore failed${NC}"
    exit 1
fi

# Restore Qdrant
echo -e "${YELLOW}Restoring Qdrant vector database...${NC}"
docker-compose up -d qdrant
sleep 10
docker cp $BACKUP_EXTRACT_DIR/qdrant_backup.tar.gz synapse-qdrant:/tmp/
docker exec synapse-qdrant sh -c "cd / && tar -xzf /tmp/qdrant_backup.tar.gz"
if [ $? -eq 0 ]; then
    echo -e "${GREEN}Qdrant restore completed${NC}"
else
    echo -e "${RED}Qdrant restore failed${NC}"
    exit 1
fi

# Start all services
echo -e "${YELLOW}Starting all services...${NC}"
docker-compose up -d

# Wait for services to be ready
echo -e "${YELLOW}Waiting for services to be ready...${NC}"
sleep 30

# Health check
echo -e "${YELLOW}Performing health checks...${NC}"
docker-compose ps

# Cleanup
rm -rf $TEMP_DIR

echo -e "${GREEN}Restore process completed successfully!${NC}"
echo -e "${YELLOW}Please verify that all services are working correctly.${NC}"