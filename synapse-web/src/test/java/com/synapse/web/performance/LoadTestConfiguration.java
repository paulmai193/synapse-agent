package com.synapse.web.performance;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@TestConfiguration
@Profile("load-test")
public class LoadTestConfiguration {

    @Bean
    public ExecutorService loadTestExecutor() {
        return Executors.newFixedThreadPool(100);
    }

    @Bean
    public LoadTestMetrics loadTestMetrics() {
        return new LoadTestMetrics();
    }
}