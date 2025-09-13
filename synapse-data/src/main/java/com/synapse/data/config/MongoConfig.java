package com.synapse.data.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertCallback;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;

/**
 * MongoDB configuration with indexes and event handling.
 */
@Configuration
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Override
    protected String getDatabaseName() {
        return "synapse_docs";
    }

    /**
     * Callback to update timestamps before saving.
     */
    @Bean
    public BeforeConvertCallback<com.synapse.data.entity.mongo.Document> documentBeforeConvertCallback() {
        return (entity, collection) -> {
            entity.setUpdatedAt(LocalDateTime.now());
            return entity;
        };
    }
}