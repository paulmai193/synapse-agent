package com.synapse.data.vector;

import io.qdrant.client.QdrantClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VectorDatabaseServiceTest {

    @Mock
    private QdrantClient qdrantClient;

    @InjectMocks
    private VectorDatabaseService vectorDatabaseService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(vectorDatabaseService, "collectionName", "test_collection");
        ReflectionTestUtils.setField(vectorDatabaseService, "vectorSize", 384);
    }

    @Test
    void testVectorPointCreation() {
        String id = "test-id";
        List<Float> vector = Arrays.asList(0.1f, 0.2f, 0.3f);
        Map<String, Object> payload = new HashMap<>();
        payload.put("document_id", "doc-123");
        payload.put("project_id", 1L);

        VectorPoint point = new VectorPoint(id, vector, payload);

        assertEquals(id, point.getId());
        assertEquals(vector, point.getVector());
        assertEquals(payload, point.getPayload());
    }

    @Test
    void testVectorSearchResultCreation() {
        String id = "result-id";
        Float score = 0.95f;
        Map<String, Object> payload = new HashMap<>();
        payload.put("text", "sample text");

        VectorSearchResult result = new VectorSearchResult(id, score, payload);

        assertEquals(id, result.getId());
        assertEquals(score, result.getScore());
        assertEquals(payload, result.getPayload());
    }

    @Test
    void testStoreVectorHandlesException() {
        VectorPoint point = new VectorPoint("test", Arrays.asList(0.1f), new HashMap<>());
        
        when(qdrantClient.upsertAsync(any())).thenThrow(new RuntimeException("Connection failed"));

        assertThrows(RuntimeException.class, () -> vectorDatabaseService.storeVector(point));
    }

    @Test
    void testDeleteVectorHandlesException() {
        when(qdrantClient.deleteAsync(any())).thenThrow(new RuntimeException("Connection failed"));

        assertThrows(RuntimeException.class, () -> vectorDatabaseService.deleteVector("test-id"));
    }
}