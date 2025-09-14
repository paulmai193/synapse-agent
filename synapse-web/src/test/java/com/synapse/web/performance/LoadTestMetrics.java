package com.synapse.web.performance;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

public class LoadTestMetrics {
    private final LongAdder totalRequests = new LongAdder();
    private final LongAdder successfulRequests = new LongAdder();
    private final LongAdder failedRequests = new LongAdder();
    private final AtomicLong totalResponseTime = new AtomicLong(0);
    private final AtomicLong maxResponseTime = new AtomicLong(0);
    private final AtomicLong minResponseTime = new AtomicLong(Long.MAX_VALUE);
    private final ConcurrentHashMap<String, LongAdder> endpointMetrics = new ConcurrentHashMap<>();

    public void recordRequest(String endpoint, long responseTime, boolean success) {
        totalRequests.increment();
        if (success) {
            successfulRequests.increment();
        } else {
            failedRequests.increment();
        }
        
        totalResponseTime.addAndGet(responseTime);
        maxResponseTime.updateAndGet(current -> Math.max(current, responseTime));
        minResponseTime.updateAndGet(current -> Math.min(current, responseTime));
        
        endpointMetrics.computeIfAbsent(endpoint, k -> new LongAdder()).increment();
    }

    public double getAverageResponseTime() {
        long total = totalRequests.sum();
        return total > 0 ? (double) totalResponseTime.get() / total : 0;
    }

    public double getSuccessRate() {
        long total = totalRequests.sum();
        return total > 0 ? (double) successfulRequests.sum() / total * 100 : 0;
    }

    public LoadTestResults getResults() {
        return new LoadTestResults(
            totalRequests.sum(),
            successfulRequests.sum(),
            failedRequests.sum(),
            getAverageResponseTime(),
            maxResponseTime.get(),
            minResponseTime.get() == Long.MAX_VALUE ? 0 : minResponseTime.get(),
            getSuccessRate()
        );
    }

    public static class LoadTestResults {
        public final long totalRequests;
        public final long successfulRequests;
        public final long failedRequests;
        public final double averageResponseTime;
        public final long maxResponseTime;
        public final long minResponseTime;
        public final double successRate;

        public LoadTestResults(long totalRequests, long successfulRequests, long failedRequests,
                             double averageResponseTime, long maxResponseTime, long minResponseTime,
                             double successRate) {
            this.totalRequests = totalRequests;
            this.successfulRequests = successfulRequests;
            this.failedRequests = failedRequests;
            this.averageResponseTime = averageResponseTime;
            this.maxResponseTime = maxResponseTime;
            this.minResponseTime = minResponseTime;
            this.successRate = successRate;
        }
    }
}