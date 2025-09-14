package com.synapse.web.performance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.synapse.core.dto.SearchRequest;
import com.synapse.web.SynapseWebApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = SynapseWebApplication.class)
@AutoConfigureWebMvc
@ActiveProfiles("load-test")
public class SearchPerformanceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ExecutorService loadTestExecutor;

    @Autowired
    private LoadTestMetrics loadTestMetrics;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testConcurrentSearchPerformance() throws Exception {
        int concurrentUsers = 100;
        int requestsPerUser = 10;
        
        CompletableFuture<Void>[] futures = IntStream.range(0, concurrentUsers)
            .mapToObj(userId -> CompletableFuture.runAsync(() -> {
                try {
                    performSearchRequests(userId, requestsPerUser);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, loadTestExecutor))
            .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures).join();

        LoadTestMetrics.LoadTestResults results = loadTestMetrics.getResults();
        
        assertTrue(results.averageResponseTime < 3000, 
            "Average response time should be under 3 seconds: " + results.averageResponseTime + "ms");
        assertTrue(results.successRate > 95, 
            "Success rate should be above 95%: " + results.successRate + "%");
        
        System.out.println("Performance Test Results:");
        System.out.println("Total Requests: " + results.totalRequests);
        System.out.println("Successful Requests: " + results.successfulRequests);
        System.out.println("Failed Requests: " + results.failedRequests);
        System.out.println("Average Response Time: " + results.averageResponseTime + "ms");
        System.out.println("Max Response Time: " + results.maxResponseTime + "ms");
        System.out.println("Min Response Time: " + results.minResponseTime + "ms");
        System.out.println("Success Rate: " + results.successRate + "%");
    }

    private void performSearchRequests(int userId, int requestCount) throws Exception {
        for (int i = 0; i < requestCount; i++) {
            SearchRequest request = new SearchRequest();
            request.setQuery("test query " + userId + " " + i);
            request.setLanguage("en");
            request.setLimit(10);
            request.setOffset(0);

            long startTime = System.currentTimeMillis();
            try {
                MvcResult result = mockMvc.perform(post("/api/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andReturn();
                
                long responseTime = System.currentTimeMillis() - startTime;
                loadTestMetrics.recordRequest("/api/search", responseTime, true);
                
            } catch (Exception e) {
                long responseTime = System.currentTimeMillis() - startTime;
                loadTestMetrics.recordRequest("/api/search", responseTime, false);
                throw e;
            }
        }
    }
}