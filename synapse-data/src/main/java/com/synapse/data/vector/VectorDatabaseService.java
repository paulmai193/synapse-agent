package com.synapse.data.vector;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Collections.*;
import io.qdrant.client.grpc.Points.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Service for vector database operations using Qdrant.
 */
@Service
public class VectorDatabaseService {

    private static final Logger logger = LoggerFactory.getLogger(VectorDatabaseService.class);

    @Autowired
    private QdrantClient qdrantClient;

    @Value("${synapse.vector.database.collection-name}")
    private String collectionName;

    @Value("${synapse.vector.database.vector-size}")
    private int vectorSize;

    /**
     * Initialize collection if it doesn't exist.
     */
    public void initializeCollection() {
        try {
            // Check if collection exists
            var collections = qdrantClient.listCollectionsAsync().get();
            boolean exists = collections.getCollectionsList().stream()
                    .anyMatch(desc -> desc.getName().equals(collectionName));

            if (!exists) {
                createCollection();
            }
        } catch (Exception e) {
            logger.error("Failed to initialize collection: {}", e.getMessage());
            throw new RuntimeException("Vector database initialization failed", e);
        }
    }

    private void createCollection() throws ExecutionException, InterruptedException {
        var vectorParams = VectorParams.newBuilder()
                .setSize(vectorSize)
                .setDistance(Distance.Cosine)
                .build();

        var createCollection = CreateCollection.newBuilder()
                .setCollectionName(collectionName)
                .setVectorsConfig(VectorsConfig.newBuilder().setParams(vectorParams).build())
                .build();

        qdrantClient.createCollectionAsync(createCollection).get();
        logger.info("Created collection: {}", collectionName);
    }

    /**
     * Store vector point in the database.
     */
    public void storeVector(VectorPoint vectorPoint) {
        try {
            var point = PointStruct.newBuilder()
                    .setId(PointId.newBuilder().setUuid(vectorPoint.getId()).build())
                    .setVectors(Vectors.newBuilder().setVector(
                            Vector.newBuilder().addAllData(vectorPoint.getVector()).build()
                    ).build())
                    .putAllPayload(convertPayload(vectorPoint.getPayload()))
                    .build();

            var upsertPoints = UpsertPoints.newBuilder()
                    .setCollectionName(collectionName)
                    .addPoints(point)
                    .build();

            qdrantClient.upsertAsync(upsertPoints).get();
            logger.debug("Stored vector point: {}", vectorPoint.getId());
        } catch (Exception e) {
            logger.error("Failed to store vector: {}", e.getMessage());
            throw new RuntimeException("Vector storage failed", e);
        }
    }

    /**
     * Search for similar vectors.
     */
    public List<VectorSearchResult> searchSimilar(List<Float> queryVector, int limit, Map<String, Object> filter) {
        try {
            var searchPoints = SearchPoints.newBuilder()
                    .setCollectionName(collectionName)
                    .addAllVector(queryVector)
                    .setLimit(limit)
                    .setWithPayload(WithPayloadSelector.newBuilder().setEnable(true).build())
                    .build();

            var searchResult = qdrantClient.searchAsync(searchPoints).get();

            return searchResult.getResultList().stream()
                    .map(this::convertToSearchResult)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Vector search failed: {}", e.getMessage());
            throw new RuntimeException("Vector search failed", e);
        }
    }

    /**
     * Delete vector by ID.
     */
    public void deleteVector(String id) {
        try {
            var deletePoints = DeletePoints.newBuilder()
                    .setCollectionName(collectionName)
                    .setPoints(PointsSelector.newBuilder()
                            .setPoints(PointsIdsList.newBuilder()
                                    .addIds(PointId.newBuilder().setUuid(id).build())
                                    .build())
                            .build())
                    .build();

            qdrantClient.deleteAsync(deletePoints).get();
            logger.debug("Deleted vector: {}", id);
        } catch (Exception e) {
            logger.error("Failed to delete vector: {}", e.getMessage());
            throw new RuntimeException("Vector deletion failed", e);
        }
    }

    private Map<String, io.qdrant.client.grpc.Points.Value> convertPayload(Map<String, Object> payload) {
        Map<String, io.qdrant.client.grpc.Points.Value> result = new HashMap<>();
        if (payload != null) {
            payload.forEach((key, value) -> {
                if (value instanceof String) {
                    result.put(key, io.qdrant.client.grpc.Points.Value.newBuilder()
                            .setStringValue((String) value).build());
                } else if (value instanceof Integer) {
                    result.put(key, io.qdrant.client.grpc.Points.Value.newBuilder()
                            .setIntegerValue((Integer) value).build());
                } else if (value instanceof Long) {
                    result.put(key, io.qdrant.client.grpc.Points.Value.newBuilder()
                            .setIntegerValue((Long) value).build());
                }
            });
        }
        return result;
    }

    private VectorSearchResult convertToSearchResult(ScoredPoint scoredPoint) {
        Map<String, Object> payload = new HashMap<>();
        scoredPoint.getPayloadMap().forEach((key, value) -> {
            if (value.hasStringValue()) {
                payload.put(key, value.getStringValue());
            } else if (value.hasIntegerValue()) {
                payload.put(key, value.getIntegerValue());
            }
        });

        return new VectorSearchResult(
                scoredPoint.getId().getUuid(),
                scoredPoint.getScore(),
                payload
        );
    }
}