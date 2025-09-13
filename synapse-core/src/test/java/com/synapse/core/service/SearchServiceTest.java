package com.synapse.core.service;

import com.synapse.core.dto.SearchRequest;
import com.synapse.core.dto.SearchResponse;
import com.synapse.data.entity.mongo.Document;
import com.synapse.data.entity.mongo.Chunk;
import com.synapse.data.repository.mongo.DocumentRepository;
import com.synapse.data.repository.mongo.ChunkRepository;
import com.synapse.data.vector.VectorDatabaseService;
import com.synapse.data.vector.VectorSearchResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock
    private VectorDatabaseService vectorDatabaseService;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private ChunkRepository chunkRepository;

    @Mock
    private AccessControlService accessControlService;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private SearchService searchService;

    @BeforeEach
    void setUp() {
        // Setup common mocks
        when(accessControlService.getAccessibleProjectIds(anyLong()))
            .thenReturn(Set.of(1L, 2L));
        when(accessControlService.getAccessibleDepartmentIds(anyLong()))
            .thenReturn(Set.of(1L));
    }

    @Test
    void testSearchWithValidQuery() {
        // Arrange
        SearchRequest request = new SearchRequest();
        request.setQuery("test query");
        request.setLimit(10);

        Map<String, Object> embeddingResponse = new HashMap<>();
        embeddingResponse.put("embedding", Arrays.asList(0.1, 0.2, 0.3));
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
            .thenReturn(embeddingResponse);

        VectorSearchResult vectorResult = new VectorSearchResult("chunk-1", 0.9f, new HashMap<>());
        when(vectorDatabaseService.searchSimilar(anyList(), anyInt(), anyMap()))
            .thenReturn(Arrays.asList(vectorResult));

        Chunk chunk = new Chunk();
        chunk.setId("chunk-1");
        chunk.setDocumentId("doc-1");
        chunk.setContent("Test content");
        when(chunkRepository.findById("chunk-1")).thenReturn(Optional.of(chunk));

        Document document = new Document();
        document.setId("doc-1");
        document.setTitle("Test Document");
        document.setProjectId(1L);
        document.setLastModified(LocalDateTime.now());
        when(documentRepository.findById("doc-1")).thenReturn(Optional.of(document));

        // Act
        SearchResponse response = searchService.search(request, 1L);

        // Assert
        assertNotNull(response);
        assertEquals("test query", response.getQuery());
        assertEquals(1, response.getTotalResults());
        assertFalse(response.getResults().isEmpty());
        assertTrue(response.getSearchTimeMs() >= 0);
    }

    @Test
    void testSearchWithEmptyEmbedding() {
        // Arrange
        SearchRequest request = new SearchRequest();
        request.setQuery("test query");

        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
            .thenReturn(null);

        // Act
        SearchResponse response = searchService.search(request, 1L);

        // Assert
        assertNotNull(response);
        assertEquals("test query", response.getQuery());
        assertEquals(0, response.getTotalResults());
        assertTrue(response.getResults().isEmpty());
    }

    @Test
    void testSearchWithNoResults() {
        // Arrange
        SearchRequest request = new SearchRequest();
        request.setQuery("test query");

        Map<String, Object> embeddingResponse = new HashMap<>();
        embeddingResponse.put("embedding", Arrays.asList(0.1, 0.2, 0.3));
        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
            .thenReturn(embeddingResponse);

        when(vectorDatabaseService.searchSimilar(anyList(), anyInt(), anyMap()))
            .thenReturn(Collections.emptyList());

        // Act
        SearchResponse response = searchService.search(request, 1L);

        // Assert
        assertNotNull(response);
        assertEquals("test query", response.getQuery());
        assertEquals(0, response.getTotalResults());
        assertTrue(response.getResults().isEmpty());
    }

    @Test
    void testSearchHandlesException() {
        // Arrange
        SearchRequest request = new SearchRequest();
        request.setQuery("test query");

        when(restTemplate.postForObject(anyString(), any(), eq(Map.class)))
            .thenThrow(new RuntimeException("NLP service unavailable"));

        // Act
        SearchResponse response = searchService.search(request, 1L);

        // Assert
        assertNotNull(response);
        assertEquals("test query", response.getQuery());
        assertEquals(0, response.getTotalResults());
        assertTrue(response.getResults().isEmpty());
    }
}