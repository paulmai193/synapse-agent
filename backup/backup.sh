#!/bin/bash

set -e

# Configuration
BACKUP_DIR="/backup"
DATE=$(date +%Y%m%d_%H%M%S)
RETENTION_DAYS=30

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${GREEN}Starting Synapse backup process...${NC}"

# Create backup directory
mkdir -p $BACKUP_DIR/$DATE

# PostgreSQL Backup
echo -e "${YELLOW}Backing up PostgreSQL database...${NC}"
docker exec synapse-postgres pg_dump -U synapse synapse > $BACKUP_DIR/$DATE/postgres_backup.sql
if [ $? -eq 0 ]; then
    echo -e "${GREEN}PostgreSQL backup completed${NC}"
else
    echo -e "${RED}PostgreSQL backup failed${NC}"
    exit 1
fi

# MongoDB Backup
echo -e "${YELLOW}Backing up MongoDB database...${NC}"
docker exec synapse-mongodb mongodump --username synapse --password synapse_password --authenticationDatabase admin --db synapse --out /tmp/mongodb_backup
docker cp synapse-mongodb:/tmp/mongodb_backup $BACKUP_DIR/$DATE/
if [ $? -eq 0 ]; then
    echo -e "${GREEN}MongoDB backup completed${NC}"
else
    echo -e "${RED}MongoDB backup failed${NC}"
    exit 1
fi

# Redis Backup
echo -e "${YELLOW}Backing up Redis data...${NC}"
docker exec synapse-redis redis-cli --rdb /tmp/redis_backup.rdb
docker cp synapse-redis:/tmp/redis_backup.rdb $BACKUP_DIR/$DATE/
if [ $? -eq 0 ]; then
    echo -e "${GREEN}Redis backup completed${NC}"
else
    echo -e "${RED}Redis backup failed${NC}"
    exit 1
fi

# Qdrant Backup
echo -e "${YELLOW}Backing up Qdrant vector database...${NC}"
docker exec synapse-qdrant tar -czf /tmp/qdrant_backup.tar.gz /qdrant/storage
docker cp synapse-qdrant:/tmp/qdrant_backup.tar.gz $BACKUP_DIR/$DATE/
if [ $? -eq 0 ]; then
    echo -e "${GREEN}Qdrant backup completed${NC}"
else
    echo -e "${RED}Qdrant backup failed${NC}"
    exit 1
fi

# Application Configuration Backup
echo -e "${YELLOW}Backing up application configuration...${NC}"
cp -r ./k8s $BACKUP_DIR/$DATE/
cp -r ./monitoring $BACKUP_DIR/$DATE/
cp docker-compose*.yml $BACKUP_DIR/$DATE/
cp .env* $BACKUP_DIR/$DATE/ 2>/dev/null || true

# Compress backup
echo -e "${YELLOW}Compressing backup...${NC}"
cd $BACKUP_DIR
tar -czf synapse_backup_$DATE.tar.gz $DATE/
rm -rf $DATE/

# Calculate backup size
BACKUP_SIZE=$(du -h synapse_backup_$DATE.tar.gz | cut -f1)
echo -e "${GREEN}Backup completed: synapse_backup_$DATE.tar.gz ($BACKUP_SIZE)${NC}"

# Cleanup old backups
echo -e "${YELLOW}Cleaning up old backups (older than $RETENTION_DAYS days)...${NC}"
find $BACKUP_DIR -name "synapse_backup_*.tar.gz" -mtime +$RETENTION_DAYS -delete
REMAINING_BACKUPS=$(ls -1 $BACKUP_DIR/synapse_backup_*.tar.gz 2>/dev/null | wc -l)
echo -e "${GREEN}Cleanup completed. $REMAINING_BACKUPS backups remaining.${NC}"

# Upload to cloud storage (optional)
if [ ! -z "$AWS_S3_BUCKET" ]; then
    echo -e "${YELLOW}Uploading backup to S3...${NC}"
    aws s3 cp $BACKUP_DIR/synapse_backup_$DATE.tar.gz s3://$AWS_S3_BUCKET/backups/
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}Backup uploaded to S3${NC}"
    else
        echo -e "${RED}Failed to upload backup to S3${NC}"
    fi
fi

echo -e "${GREEN}Backup process completed successfully!${NC}"