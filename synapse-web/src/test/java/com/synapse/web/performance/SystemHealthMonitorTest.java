package com.synapse.web.performance;

import com.synapse.web.SynapseWebApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = SynapseWebApplication.class)
@ActiveProfiles("load-test")
public class SystemHealthMonitorTest {

    @Autowired
    private ExecutorService loadTestExecutor;

    @Autowired
    private PerformanceMonitoringService performanceMonitoringService;

    @Test
    public void testSystemHealthUnderLoad() throws Exception {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();

        long initialMemory = memoryBean.getHeapMemoryUsage().getUsed();
        double initialCpuLoad = osBean.getProcessCpuLoad();

        // Simulate high load
        int concurrentTasks = 200;
        CompletableFuture<Void>[] futures = IntStream.range(0, concurrentTasks)
            .mapToObj(taskId -> CompletableFuture.runAsync(() -> {
                simulateWorkload(taskId);
            }, loadTestExecutor))
            .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures).join();

        // Check system health after load
        long finalMemory = memoryBean.getHeapMemoryUsage().getUsed();
        double finalCpuLoad = osBean.getProcessCpuLoad();
        long maxMemory = memoryBean.getHeapMemoryUsage().getMax();

        double memoryUsagePercent = (double) finalMemory / maxMemory * 100;

        assertTrue(memoryUsagePercent < 80, 
            "Memory usage should be under 80%: " + memoryUsagePercent + "%");
        assertTrue(finalCpuLoad < 0.9, 
            "CPU load should be under 90%: " + (finalCpuLoad * 100) + "%");

        PerformanceMonitoringService.PerformanceReport report = 
            performanceMonitoringService.generateReport();

        System.out.println("System Health Report:");
        System.out.println("Initial Memory Usage: " + (initialMemory / 1024 / 1024) + " MB");
        System.out.println("Final Memory Usage: " + (finalMemory / 1024 / 1024) + " MB");
        System.out.println("Memory Usage Percentage: " + memoryUsagePercent + "%");
        System.out.println("Initial CPU Load: " + (initialCpuLoad * 100) + "%");
        System.out.println("Final CPU Load: " + (finalCpuLoad * 100) + "%");
        System.out.println("Average Search Time: " + report.averageSearchTime + "ms");
        System.out.println("Search Success Rate: " + report.searchSuccessRate + "%");
    }

    private void simulateWorkload(int taskId) {
        try {
            // Simulate search operations
            for (int i = 0; i < 5; i++) {
                PerformanceMonitoringService.Timer.Sample sample = 
                    performanceMonitoringService.startTimer("search");
                
                // Simulate search processing
                Thread.sleep(100 + (taskId % 50));
                
                performanceMonitoringService.stopTimer(sample, "search");
                performanceMonitoringService.recordSearchPerformance(100 + (taskId % 50), true);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}