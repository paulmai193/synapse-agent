package com.synapse.core.service;

import com.synapse.core.dto.SearchRequest;
import com.synapse.core.dto.SearchResponse;
import com.synapse.core.dto.SearchResult;
import com.synapse.data.entity.mongo.Document;
import com.synapse.data.entity.mongo.Chunk;
import com.synapse.data.repository.mongo.DocumentRepository;
import com.synapse.data.repository.mongo.ChunkRepository;
import com.synapse.data.vector.VectorDatabaseService;
import com.synapse.data.vector.VectorSearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchService {

    private static final Logger logger = LoggerFactory.getLogger(SearchService.class);

    @Autowired
    private VectorDatabaseService vectorDatabaseService;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private ChunkRepository chunkRepository;

    @Autowired
    private AccessControlService accessControlService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ABTestingService abTestingService;

    private static final String NLP_SERVICE_URL = "http://localhost:8001/api/v1/text";

    public SearchResponse search(SearchRequest request, Long userId) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Generate query embedding
            List<Float> queryEmbedding = generateQueryEmbedding(request.getQuery());
            if (queryEmbedding.isEmpty()) {
                return createEmptyResponse(request, startTime);
            }

            // Get user access control context
            Set<Long> accessibleProjectIds = accessControlService.getAccessibleProjectIds(userId);
            Set<Long> accessibleDepartmentIds = accessControlService.getAccessibleDepartmentIds(userId);

            // Search vectors
            List<VectorSearchResult> vectorResults = vectorDatabaseService.searchSimilar(
                queryEmbedding, request.getLimit(), createAccessFilter(accessibleProjectIds, accessibleDepartmentIds)
            );

            // Convert to search results
            List<SearchResult> searchResults = convertToSearchResults(vectorResults, accessibleProjectIds);

            // Apply A/B testing for ranking algorithms
            List<SearchResult> rankedResults = abTestingService.applyTestRanking(searchResults, userId, request.getQuery());
            
            // Filter and rank results
            List<SearchResult> filteredResults = filterAndRankResults(rankedResults, request);

            long searchTime = System.currentTimeMillis() - startTime;
            
            SearchResponse response = new SearchResponse(
                request.getQuery(),
                filteredResults,
                filteredResults.size(),
                searchTime
            );
            response.setLanguage(request.getLanguage());

            // Record A/B test result
            boolean userSatisfied = filteredResults.size() > 0; // Simplified satisfaction metric
            abTestingService.recordTestResult(userId, request.getQuery(), filteredResults, userSatisfied, searchTime);
            
            logger.info("Search completed: query='{}', results={}, time={}ms", 
                request.getQuery(), filteredResults.size(), searchTime);

            return response;

        } catch (Exception e) {
            logger.error("Search failed for query: {}", request.getQuery(), e);
            return createEmptyResponse(request, startTime);
        }
    }

    private List<Float> generateQueryEmbedding(String query) {
        try {
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("text", query);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(
                NLP_SERVICE_URL + "/generate-embedding", entity, Map.class
            );

            if (response != null && response.containsKey("embedding")) {
                @SuppressWarnings("unchecked")
                List<Double> embedding = (List<Double>) response.get("embedding");
                return embedding.stream().map(Double::floatValue).collect(Collectors.toList());
            }
        } catch (Exception e) {
            logger.error("Failed to generate query embedding", e);
        }
        return Collections.emptyList();
    }

    private Map<String, Object> createAccessFilter(Set<Long> projectIds, Set<Long> departmentIds) {
        Map<String, Object> filter = new HashMap<>();
        if (!projectIds.isEmpty()) {
            filter.put("project_ids", projectIds);
        }
        if (!departmentIds.isEmpty()) {
            filter.put("department_ids", departmentIds);
        }
        return filter;
    }

    private List<SearchResult> convertToSearchResults(List<VectorSearchResult> vectorResults, Set<Long> accessibleProjectIds) {
        List<SearchResult> results = new ArrayList<>();

        for (VectorSearchResult vectorResult : vectorResults) {
            try {
                String chunkId = vectorResult.getId();
                Optional<Chunk> chunkOpt = chunkRepository.findById(chunkId);
                
                if (chunkOpt.isPresent()) {
                    Chunk chunk = chunkOpt.get();
                    Optional<Document> documentOpt = documentRepository.findById(chunk.getDocumentId());
                    
                    if (documentOpt.isPresent()) {
                        Document document = documentOpt.get();
                        
                        // Verify access control
                        if (hasAccess(document, accessibleProjectIds)) {
                            SearchResult result = createSearchResult(document, chunk, vectorResult.getScore());
                            results.add(result);
                        }
                    }
                }
            } catch (Exception e) {
                logger.warn("Failed to convert vector result: {}", vectorResult.getId(), e);
            }
        }

        return results;
    }

    private boolean hasAccess(Document document, Set<Long> accessibleProjectIds) {
        return document.getProjectId() != null && accessibleProjectIds.contains(document.getProjectId());
    }

    private SearchResult createSearchResult(Document document, Chunk chunk, Float score) {
        SearchResult result = new SearchResult();
        result.setDocumentId(document.getId());
        result.setChunkId(chunk.getId());
        result.setTitle(document.getTitle());
        result.setContent(chunk.getContent());
        result.setSource(document.getSource());
        result.setRelevanceScore(score);
        result.setLastModified(document.getLastModified());
        result.setProjectId(document.getProjectId());
        return result;
    }

    private List<SearchResult> filterAndRankResults(List<SearchResult> results, SearchRequest request) {
        return results.stream()
            .filter(result -> result.getRelevanceScore() > 0.5f) // Minimum relevance threshold
            .sorted((a, b) -> Float.compare(b.getRelevanceScore(), a.getRelevanceScore()))
            .limit(request.getLimit())
            .collect(Collectors.toList());
    }

    private SearchResponse createEmptyResponse(SearchRequest request, long startTime) {
        long searchTime = System.currentTimeMillis() - startTime;
        SearchResponse response = new SearchResponse(request.getQuery(), Collections.emptyList(), 0, searchTime);
        response.setLanguage(request.getLanguage());
        return response;
    }
    
    private String generateSearchCacheKey(SearchRequest request, Long userId) {
        return String.format("search:%d:%s:%d:%d:%s", 
            userId, 
            request.getQuery().hashCode(), 
            request.getLimit(), 
            request.getOffset(),
            request.getLanguage());
    }
    
    private SearchResponse getCachedSearchResponse(String cacheKey) {
        return (SearchResponse) cacheService.getCachedQueryResult(cacheKey);
    }
    
    private void cacheSearchResponse(String cacheKey, SearchResponse response) {
        cacheService.optimizeQueryCache(cacheKey, response);
    }
    
    private String generateNextCursor(List<SearchResult> results, SearchRequest request) {
        if (results.size() < request.getLimit()) {
            return null; // No more results
        }
        
        // Generate cursor based on last result's score and ID for consistent pagination
        SearchResult lastResult = results.get(results.size() - 1);
        return Base64.getEncoder().encodeToString(
            (lastResult.getRelevanceScore() + ":" + lastResult.getDocumentId()).getBytes()
        );
    }
    
    @Async
    public CompletableFuture<Void> preloadSearchCache(String query, Long userId) {
        logger.debug("Preloading search cache for query: {}", query);
        try {
            SearchRequest request = new SearchRequest();
            request.setQuery(query);
            request.setLimit(20);
            search(request, userId);
        } catch (Exception e) {
            logger.warn("Failed to preload search cache: {}", e.getMessage());
        }
        return CompletableFuture.completedFuture(null);
    }
    
    /**
     * Optimized search with performance monitoring
     */
    public SearchResponse searchWithMetrics(SearchRequest request, Long userId) {
        long startTime = System.nanoTime();
        
        SearchResponse response = search(request, userId);
        
        long endTime = System.nanoTime();
        long durationMs = (endTime - startTime) / 1_000_000;
        
        // Log performance metrics
        logger.info("Search performance: query='{}', results={}, duration={}ms, cached={}", 
            request.getQuery(), 
            response.getResults().size(), 
            durationMs,
            durationMs < 100 ? "likely" : "unlikely");
            
        // Alert if search takes too long
        if (durationMs > 3000) {
            logger.warn("Slow search detected: {}ms for query '{}'", durationMs, request.getQuery());
        }
        
        return response;
    }
}