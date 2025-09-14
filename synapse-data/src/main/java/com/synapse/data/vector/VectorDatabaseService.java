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
            var searchPointsBuilder = SearchPoints.newBuilder()
                    .setCollectionName(collectionName)
                    .addAllVector(queryVector)
                    .setLimit(limit)
                    .setWithPayload(WithPayloadSelector.newBuilder().setEnable(true).build());

            // Add filter if provided
            if (filter != null && !filter.isEmpty()) {
                searchPointsBuilder.setFilter(buildFilter(filter));
            }

            var searchResult = qdrantClient.searchAsync(searchPointsBuilder.build()).get();

            return searchResult.getResultList().stream()
                    .map(this::convertToSearchResult)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Vector search failed: {}", e.getMessage());
            throw new RuntimeException("Vector search failed", e);
        }
    }
    
    /**
     * Optimized search with HNSW parameters for better performance.
     */
    public List<VectorSearchResult> searchSimilarOptimized(List<Float> queryVector, int limit, Map<String, Object> filter) {
        try {
            var searchPointsBuilder = SearchPoints.newBuilder()
                    .setCollectionName(collectionName)
                    .addAllVector(queryVector)
                    .setLimit(limit)
                    .setWithPayload(WithPayloadSelector.newBuilder().setEnable(true).build())
                    .setParams(SearchParams.newBuilder()
                        .setHnswEf(128) // Higher ef for better recall
                        .setExact(false) // Use approximate search for speed
                        .build());

            // Add optimized filter
            if (filter != null && !filter.isEmpty()) {
                searchPointsBuilder.setFilter(buildOptimizedFilter(filter));
            }

            var searchResult = qdrantClient.searchAsync(searchPointsBuilder.build()).get();

            return searchResult.getResultList().stream()
                    .map(this::convertToSearchResult)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Optimized vector search failed: {}", e.getMessage());
            // Fallback to regular search
            return searchSimilar(queryVector, limit, filter);
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

    private Filter buildFilter(Map<String, Object> filterMap) {
        var filterBuilder = Filter.newBuilder();
        
        for (Map.Entry<String, Object> entry : filterMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            if (value instanceof Collection) {
                // Handle collection filters (e.g., project_ids, department_ids)
                @SuppressWarnings("unchecked")
                Collection<Object> values = (Collection<Object>) value;
                if (!values.isEmpty()) {
                    var condition = Condition.newBuilder()
                        .setField(FieldCondition.newBuilder()
                            .setKey(key)
                            .setMatch(Match.newBuilder()
                                .setAnyOf(MatchAny.newBuilder()
                                    .addAllAny(values.stream()
                                        .map(v -> io.qdrant.client.grpc.Points.Value.newBuilder()
                                            .setStringValue(v.toString()).build())
                                        .collect(Collectors.toList()))
                                    .build())
                                .build())
                            .build())
                        .build();
                    filterBuilder.addShould(condition);
                }
            }
        }
        
        return filterBuilder.build();
    }
    
    private Filter buildOptimizedFilter(Map<String, Object> filterMap) {
        // Build more efficient filter with proper indexing hints
        var filterBuilder = Filter.newBuilder();
        
        // Add status filter first (most selective)
        filterBuilder.addMust(Condition.newBuilder()
            .setField(FieldCondition.newBuilder()
                .setKey("status")
                .setMatch(Match.newBuilder()
                    .setValue(io.qdrant.client.grpc.Points.Value.newBuilder()
                        .setStringValue("ACTIVE").build())
                    .build())
                .build())
            .build());
            
        // Add deleted filter
        filterBuilder.addMust(Condition.newBuilder()
            .setField(FieldCondition.newBuilder()
                .setKey("deleted")
                .setMatch(Match.newBuilder()
                    .setValue(io.qdrant.client.grpc.Points.Value.newBuilder()
                        .setBoolValue(false).build())
                    .build())
                .build())
            .build());
        
        // Add access control filters
        for (Map.Entry<String, Object> entry : filterMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            if (value instanceof Collection) {
                @SuppressWarnings("unchecked")
                Collection<Object> values = (Collection<Object>) value;
                if (!values.isEmpty()) {
                    var condition = Condition.newBuilder()
                        .setField(FieldCondition.newBuilder()
                            .setKey(key)
                            .setMatch(Match.newBuilder()
                                .setAnyOf(MatchAny.newBuilder()
                                    .addAllAny(values.stream()
                                        .map(v -> io.qdrant.client.grpc.Points.Value.newBuilder()
                                            .setStringValue(v.toString()).build())
                                        .collect(Collectors.toList()))
                                    .build())
                                .build())
                            .build())
                        .build();
                    filterBuilder.addShould(condition);
                }
            }
        }
        
        return filterBuilder.build();
    }

    private VectorSearchResult convertToSearchResult(ScoredPoint scoredPoint) {
        Map<String, Object> payload = new HashMap<>();
        scoredPoint.getPayloadMap().forEach((key, value) -> {
            if (value.hasStringValue()) {
                payload.put(key, value.getStringValue());
            } else if (value.hasIntegerValue()) {
                payload.put(key, value.getIntegerValue());
            } else if (value.hasBoolValue()) {
                payload.put(key, value.getBoolValue());
            }
        });

        return new VectorSearchResult(
                scoredPoint.getId().getUuid(),
                scoredPoint.getScore(),
                payload
        );
    }
    
    /**
     * Batch upsert vectors for better performance
     */
    public void storeVectorsBatch(List<VectorPoint> vectorPoints) {
        try {
            List<PointStruct> points = vectorPoints.stream()
                .map(vectorPoint -> PointStruct.newBuilder()
                    .setId(PointId.newBuilder().setUuid(vectorPoint.getId()).build())
                    .setVectors(Vectors.newBuilder().setVector(
                        Vector.newBuilder().addAllData(vectorPoint.getVector()).build()
                    ).build())
                    .putAllPayload(convertPayload(vectorPoint.getPayload()))
                    .build())
                .collect(Collectors.toList());

            var upsertPoints = UpsertPoints.newBuilder()
                .setCollectionName(collectionName)
                .addAllPoints(points)
                .build();

            qdrantClient.upsertAsync(upsertPoints).get();
            logger.debug("Stored {} vector points in batch", vectorPoints.size());
        } catch (Exception e) {
            logger.error("Failed to store vector batch: {}", e.getMessage());
            throw new RuntimeException("Vector batch storage failed", e);
        }
    }
    
    /**
     * Get collection info for monitoring
     */
    public Map<String, Object> getCollectionInfo() {
        try {
            var collectionInfo = qdrantClient.getCollectionInfoAsync(collectionName).get();
            Map<String, Object> info = new HashMap<>();
            info.put("vectorsCount", collectionInfo.getResult().getVectorsCount());
            info.put("indexedVectorsCount", collectionInfo.getResult().getIndexedVectorsCount());
            info.put("status", collectionInfo.getResult().getStatus().name());
            return info;
        } catch (Exception e) {
            logger.error("Failed to get collection info: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }
}