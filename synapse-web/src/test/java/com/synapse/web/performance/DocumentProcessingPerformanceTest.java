package com.synapse.web.performance;

import com.synapse.web.SynapseWebApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = SynapseWebApplication.class)
@AutoConfigureWebMvc
@ActiveProfiles("load-test")
public class DocumentProcessingPerformanceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ExecutorService loadTestExecutor;

    @Autowired
    private LoadTestMetrics loadTestMetrics;

    @Test
    public void testConcurrentDocumentUploadPerformance() throws Exception {
        int concurrentUploads = 20;
        
        CompletableFuture<Void>[] futures = IntStream.range(0, concurrentUploads)
            .mapToObj(uploadId -> CompletableFuture.runAsync(() -> {
                try {
                    performDocumentUpload(uploadId);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, loadTestExecutor))
            .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures).join();

        LoadTestMetrics.LoadTestResults results = loadTestMetrics.getResults();
        
        assertTrue(results.averageResponseTime < 30000, 
            "Average document processing time should be under 30 seconds: " + results.averageResponseTime + "ms");
        assertTrue(results.successRate > 90, 
            "Success rate should be above 90%: " + results.successRate + "%");
        
        System.out.println("Document Processing Performance Results:");
        System.out.println("Total Uploads: " + results.totalRequests);
        System.out.println("Successful Uploads: " + results.successfulRequests);
        System.out.println("Failed Uploads: " + results.failedRequests);
        System.out.println("Average Processing Time: " + results.averageResponseTime + "ms");
        System.out.println("Max Processing Time: " + results.maxResponseTime + "ms");
        System.out.println("Success Rate: " + results.successRate + "%");
    }

    private void performDocumentUpload(int uploadId) throws Exception {
        // Create test document content (1MB)
        byte[] content = generateTestContent(1024 * 1024);
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "test-document-" + uploadId + ".txt", 
            "text/plain", 
            content
        );

        long startTime = System.currentTimeMillis();
        try {
            mockMvc.perform(multipart("/api/documents/upload")
                    .file(file)
                    .param("title", "Performance Test Document " + uploadId)
                    .param("visibility", "PROJECT"))
                    .andExpect(status().isOk());
            
            long processingTime = System.currentTimeMillis() - startTime;
            loadTestMetrics.recordRequest("/api/documents/upload", processingTime, true);
            
        } catch (Exception e) {
            long processingTime = System.currentTimeMillis() - startTime;
            loadTestMetrics.recordRequest("/api/documents/upload", processingTime, false);
            throw e;
        }
    }

    private byte[] generateTestContent(int sizeBytes) {
        StringBuilder content = new StringBuilder();
        String paragraph = "This is a test paragraph for performance testing. It contains multiple sentences to simulate real document content. ";
        
        while (content.length() < sizeBytes) {
            content.append(paragraph);
        }
        
        return content.substring(0, sizeBytes).getBytes();
    }
}