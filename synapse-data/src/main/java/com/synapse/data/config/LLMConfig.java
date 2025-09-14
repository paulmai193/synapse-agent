package com.synapse.data.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import okhttp3.OkHttpClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
@ConfigurationProperties(prefix = "synapse.llm.ollama")
public class LLMConfig {

    private String url = "http://localhost:11434";
    private String model = "llama3.1:8b";
    private int timeout = 30000;
    private int maxTokens = 2048;
    private double temperature = 0.7;
    private CircuitBreakerSettings circuitBreaker = new CircuitBreakerSettings();
    private RetrySettings retry = new RetrySettings();
    private RateLimiterSettings rateLimiter = new RateLimiterSettings();

    @Bean
    public OkHttpClient ollamaHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(timeout, TimeUnit.MILLISECONDS)
                .readTimeout(timeout, TimeUnit.MILLISECONDS)
                .writeTimeout(timeout, TimeUnit.MILLISECONDS)
                .build();
    }

    @Bean
    public CircuitBreaker ollamaCircuitBreaker() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .failureRateThreshold(circuitBreaker.getFailureRateThreshold())
                .waitDurationInOpenState(Duration.ofMillis(circuitBreaker.getWaitDurationInOpenState()))
                .slidingWindowSize(circuitBreaker.getSlidingWindowSize())
                .build();
        
        return CircuitBreaker.of("ollama", config);
    }

    @Bean
    public Retry ollamaRetry() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(retry.getMaxAttempts())
                .waitDuration(Duration.ofMillis(retry.getWaitDuration()))
                .build();
        
        return Retry.of("ollama", config);
    }

    @Bean
    public RateLimiter ollamaRateLimiter() {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(rateLimiter.getLimitForPeriod())
                .limitRefreshPeriod(Duration.ofMillis(rateLimiter.getLimitRefreshPeriod()))
                .build();
        
        return RateLimiter.of("ollama", config);
    }

    // Getters and setters
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public CircuitBreakerSettings getCircuitBreaker() {
        return circuitBreaker;
    }

    public void setCircuitBreaker(CircuitBreakerSettings circuitBreaker) {
        this.circuitBreaker = circuitBreaker;
    }

    public RetrySettings getRetry() {
        return retry;
    }

    public void setRetry(RetrySettings retry) {
        this.retry = retry;
    }

    public RateLimiterSettings getRateLimiter() {
        return rateLimiter;
    }

    public void setRateLimiter(RateLimiterSettings rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    public static class CircuitBreakerSettings {
        private float failureRateThreshold = 50.0f;
        private long waitDurationInOpenState = 30000;
        private int slidingWindowSize = 10;

        public float getFailureRateThreshold() {
            return failureRateThreshold;
        }

        public void setFailureRateThreshold(float failureRateThreshold) {
            this.failureRateThreshold = failureRateThreshold;
        }

        public long getWaitDurationInOpenState() {
            return waitDurationInOpenState;
        }

        public void setWaitDurationInOpenState(long waitDurationInOpenState) {
            this.waitDurationInOpenState = waitDurationInOpenState;
        }

        public int getSlidingWindowSize() {
            return slidingWindowSize;
        }

        public void setSlidingWindowSize(int slidingWindowSize) {
            this.slidingWindowSize = slidingWindowSize;
        }
    }

    public static class RetrySettings {
        private int maxAttempts = 3;
        private long waitDuration = 1000;

        public int getMaxAttempts() {
            return maxAttempts;
        }

        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        public long getWaitDuration() {
            return waitDuration;
        }

        public void setWaitDuration(long waitDuration) {
            this.waitDuration = waitDuration;
        }
    }

    public static class RateLimiterSettings {
        private int limitForPeriod = 10;
        private long limitRefreshPeriod = 60000;

        public int getLimitForPeriod() {
            return limitForPeriod;
        }

        public void setLimitForPeriod(int limitForPeriod) {
            this.limitForPeriod = limitForPeriod;
        }

        public long getLimitRefreshPeriod() {
            return limitRefreshPeriod;
        }

        public void setLimitRefreshPeriod(long limitRefreshPeriod) {
            this.limitRefreshPeriod = limitRefreshPeriod;
        }
    }
}