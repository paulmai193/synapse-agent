package com.synapse.data.vector;

import java.util.Map;

/**
 * Represents a vector search result from Qdrant.
 */
public class VectorSearchResult {
    private String id;
    private Float score;
    private Map<String, Object> payload;

    public VectorSearchResult() {}

    public VectorSearchResult(String id, Float score, Map<String, Object> payload) {
        this.id = id;
        this.score = score;
        this.payload = payload;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Float getScore() {
        return score;
    }

    public void setScore(Float score) {
        this.score = score;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }
}