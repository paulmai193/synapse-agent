package com.synapse.web.performance;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PerformanceMonitoringService {

    private final MeterRegistry meterRegistry;
    private final ConcurrentHashMap<String, Timer> timers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Counter> counters = new ConcurrentHashMap<>();

    @Autowired
    public PerformanceMonitoringService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public Timer.Sample startTimer(String operationName) {
        Timer timer = timers.computeIfAbsent(operationName, 
            name -> Timer.builder("synapse.operation.duration")
                .tag("operation", name)
                .register(meterRegistry));
        return Timer.start(meterRegistry);
    }

    public void stopTimer(Timer.Sample sample, String operationName) {
        Timer timer = timers.get(operationName);
        if (timer != null) {
            sample.stop(timer);
        }
    }

    public void incrementCounter(String counterName, String... tags) {
        String key = counterName + String.join("", tags);
        Counter counter = counters.computeIfAbsent(key,
            k -> Counter.builder("synapse.operation.count")
                .tag("operation", counterName)
                .tags(tags)
                .register(meterRegistry));
        counter.increment();
    }

    public void recordSearchPerformance(long responseTimeMs, boolean success) {
        Timer.builder("synapse.search.response.time")
            .tag("success", String.valueOf(success))
            .register(meterRegistry)
            .record(Duration.ofMillis(responseTimeMs));
        
        incrementCounter("search.requests", "success", String.valueOf(success));
    }

    public void recordDocumentProcessingPerformance(long processingTimeMs, boolean success) {
        Timer.builder("synapse.document.processing.time")
            .tag("success", String.valueOf(success))
            .register(meterRegistry)
            .record(Duration.ofMillis(processingTimeMs));
        
        incrementCounter("document.processing", "success", String.valueOf(success));
    }

    public void recordEmbeddingGenerationPerformance(long generationTimeMs, boolean success) {
        Timer.builder("synapse.embedding.generation.time")
            .tag("success", String.valueOf(success))
            .register(meterRegistry)
            .record(Duration.ofMillis(generationTimeMs));
        
        incrementCounter("embedding.generation", "success", String.valueOf(success));
    }

    public PerformanceReport generateReport() {
        return new PerformanceReport(
            getAverageResponseTime("synapse.search.response.time"),
            getAverageResponseTime("synapse.document.processing.time"),
            getAverageResponseTime("synapse.embedding.generation.time"),
            getSuccessRate("search.requests"),
            getSuccessRate("document.processing"),
            getSuccessRate("embedding.generation")
        );
    }

    private double getAverageResponseTime(String timerName) {
        return meterRegistry.find(timerName)
            .timer()
            .map(timer -> timer.mean(java.util.concurrent.TimeUnit.MILLISECONDS))
            .orElse(0.0);
    }

    private double getSuccessRate(String counterName) {
        double successCount = meterRegistry.find("synapse.operation.count")
            .tag("operation", counterName)
            .tag("success", "true")
            .counter()
            .map(Counter::count)
            .orElse(0.0);
        
        double totalCount = meterRegistry.find("synapse.operation.count")
            .tag("operation", counterName)
            .counters()
            .stream()
            .mapToDouble(Counter::count)
            .sum();
        
        return totalCount > 0 ? (successCount / totalCount) * 100 : 0;
    }

    public static class PerformanceReport {
        public final double averageSearchTime;
        public final double averageDocumentProcessingTime;
        public final double averageEmbeddingGenerationTime;
        public final double searchSuccessRate;
        public final double documentProcessingSuccessRate;
        public final double embeddingGenerationSuccessRate;

        public PerformanceReport(double averageSearchTime, double averageDocumentProcessingTime,
                               double averageEmbeddingGenerationTime, double searchSuccessRate,
                               double documentProcessingSuccessRate, double embeddingGenerationSuccessRate) {
            this.averageSearchTime = averageSearchTime;
            this.averageDocumentProcessingTime = averageDocumentProcessingTime;
            this.averageEmbeddingGenerationTime = averageEmbeddingGenerationTime;
            this.searchSuccessRate = searchSuccessRate;
            this.documentProcessingSuccessRate = documentProcessingSuccessRate;
            this.embeddingGenerationSuccessRate = embeddingGenerationSuccessRate;
        }
    }
}