package com.synapse.data.config;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Qdrant vector database.
 */
@Configuration
public class VectorDatabaseConfig {

    @Value("${synapse.vector.database.url}")
    private String qdrantUrl;

    @Bean
    public QdrantClient qdrantClient() {
        // Extract host and port from URL
        String host = extractHost(qdrantUrl);
        int port = extractPort(qdrantUrl);
        
        return new QdrantClient(
            QdrantGrpcClient.newBuilder(host, port, false).build()
        );
    }

    private String extractHost(String url) {
        // Remove protocol if present
        String cleanUrl = url.replaceFirst("^https?://", "");
        // Extract host part
        return cleanUrl.split(":")[0];
    }

    private int extractPort(String url) {
        // Remove protocol if present
        String cleanUrl = url.replaceFirst("^https?://", "");
        // Extract port or default to 6333
        String[] parts = cleanUrl.split(":");
        return parts.length > 1 ? Integer.parseInt(parts[1]) : 6333;
    }
}