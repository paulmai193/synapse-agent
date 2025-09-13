package com.synapse.data.repository.mongo;

import com.synapse.data.entity.mongo.Chunk;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Chunk entity operations.
 */
@Repository
public interface ChunkRepository extends MongoRepository<Chunk, String> {

    // Find chunks by document ID
    List<Chunk> findByDocumentIdOrderByChunkIndex(String documentId);

    // Find chunks by document IDs
    @Query("{ 'documentId': { $in: ?0 } }")
    List<Chunk> findByDocumentIds(List<String> documentIds);

    // Find chunks by language
    List<Chunk> findByLanguage(String language);

    // Find chunks with embeddings
    @Query("{ 'embeddingId': { $ne: null } }")
    List<Chunk> findChunksWithEmbeddings();

    // Find chunks without embeddings
    @Query("{ 'embeddingId': null }")
    List<Chunk> findChunksWithoutEmbeddings();

    // Search chunks by content
    @Query("{ 'content': { $regex: ?0, $options: 'i' } }")
    Page<Chunk> searchByContent(String contentPattern, Pageable pageable);

    // Count chunks by document
    long countByDocumentId(String documentId);

    // Delete chunks by document ID
    void deleteByDocumentId(String documentId);

    // Find chunks by embedding ID
    List<Chunk> findByEmbeddingId(String embeddingId);
}