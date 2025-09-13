package com.synapse.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for Synapse AI Agent.
 * Configures component scanning and enables necessary Spring features.
 */
@SpringBootApplication(scanBasePackages = "com.synapse")
@EntityScan(basePackages = "com.synapse.data.entity")
@EnableJpaRepositories(basePackages = "com.synapse.data.repository.jpa")
@EnableMongoRepositories(basePackages = "com.synapse.data.repository.mongo")
@EnableAsync
@EnableScheduling
public class SynapseApplication {

    public static void main(String[] args) {
        SpringApplication.run(SynapseApplication.class, args);
    }
}