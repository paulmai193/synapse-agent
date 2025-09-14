package com.synapse.web.performance;

import com.synapse.web.SynapseWebApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = SynapseWebApplication.class)
@ActiveProfiles("load-test")
public class EmbeddingGenerationPerformanceTest {

    @Autowired
    private ExecutorService loadTestExecutor;

    @Autowired
    private LoadTestMetrics loadTestMetrics;

    @Test
    public void testConcurrentEmbeddingGeneration() throws Exception {
        int concurrentRequests = 50;
        int requestsPerThread = 5;
        
        CompletableFuture<Void>[] futures = IntStream.range(0, concurrentRequests)
            .mapToObj(threadId -> CompletableFuture.runAsync(() -> {
                try {
                    performEmbeddingGeneration(threadId, requestsPerThread);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, loadTestExecutor))
            .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures).join();

        LoadTestMetrics.LoadTestResults results = loadTestMetrics.getResults();
        
        assertTrue(results.averageResponseTime < 5000, 
            "Average embedding generation time should be under 5 seconds: " + results.averageResponseTime + "ms");
        assertTrue(results.successRate > 95, 
            "Success rate should be above 95%: " + results.successRate + "%");
        
        System.out.println("Embedding Generation Performance Results:");
        System.out.println("Total Requests: " + results.totalRequests);
        System.out.println("Successful Requests: " + results.successfulRequests);
        System.out.println("Failed Requests: " + results.failedRequests);
        System.out.println("Average Generation Time: " + results.averageResponseTime + "ms");
        System.out.println("Max Generation Time: " + results.maxResponseTime + "ms");
        System.out.println("Success Rate: " + results.successRate + "%");
    }

    private void performEmbeddingGeneration(int threadId, int requestCount) throws Exception {
        for (int i = 0; i < requestCount; i++) {
            String text = generateTestText(threadId, i);
            
            long startTime = System.currentTimeMillis();
            try {
                // Simulate embedding generation
                simulateEmbeddingGeneration(text);
                
                long generationTime = System.currentTimeMillis() - startTime;
                loadTestMetrics.recordRequest("embedding-generation", generationTime, true);
                
            } catch (Exception e) {
                long generationTime = System.currentTimeMillis() - startTime;
                loadTestMetrics.recordRequest("embedding-generation", generationTime, false);
                throw e;
            }
        }
    }

    private String generateTestText(int threadId, int requestId) {
        return "This is test text for embedding generation performance testing. " +
               "Thread ID: " + threadId + ", Request ID: " + requestId + ". " +
               "This text simulates real document content that would be processed " +
               "by the NLP microservice for vector embedding generation.";
    }

    private void simulateEmbeddingGeneration(String text) throws InterruptedException {
        // Simulate processing time based on text length
        int processingTime = Math.min(text.length() / 10, 2000);
        Thread.sleep(processingTime);
    }
}