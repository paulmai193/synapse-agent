package com.synapse.data.entity.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MongoDB chunk entity for storing document chunks.
 */
@Document(collection = "chunks")
public class Chunk {

    @Id
    private String id;

    @Indexed
    private String documentId;

    private String content;

    @Indexed
    private Integer chunkIndex;

    private String embeddingId;

    @Indexed
    private String language;

    private ChunkMetadata metadata;

    @Indexed
    private LocalDateTime createdAt = LocalDateTime.now();

    // Constructors
    public Chunk() {}

    public Chunk(String documentId, String content, Integer chunkIndex) {
        this.documentId = documentId;
        this.content = content;
        this.chunkIndex = chunkIndex;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Integer getChunkIndex() { return chunkIndex; }
    public void setChunkIndex(Integer chunkIndex) { this.chunkIndex = chunkIndex; }

    public String getEmbeddingId() { return embeddingId; }
    public void setEmbeddingId(String embeddingId) { this.embeddingId = embeddingId; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public ChunkMetadata getMetadata() { return metadata; }
    public void setMetadata(ChunkMetadata metadata) { this.metadata = metadata; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Nested class
    public static class ChunkMetadata {
        private Integer startPosition;
        private Integer endPosition;
        private Integer wordCount;
        private List<String> entities;

        public Integer getStartPosition() { return startPosition; }
        public void setStartPosition(Integer startPosition) { this.startPosition = startPosition; }

        public Integer getEndPosition() { return endPosition; }
        public void setEndPosition(Integer endPosition) { this.endPosition = endPosition; }

        public Integer getWordCount() { return wordCount; }
        public void setWordCount(Integer wordCount) { this.wordCount = wordCount; }

        public List<String> getEntities() { return entities; }
        public void setEntities(List<String> entities) { this.entities = entities; }
    }
}