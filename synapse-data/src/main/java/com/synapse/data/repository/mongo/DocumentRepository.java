package com.synapse.data.repository.mongo;

import com.synapse.data.entity.mongo.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Document entity operations.
 */
@Repository
public interface DocumentRepository extends MongoRepository<Document, String> {

    // Find active documents only
    @Query("{ 'status': 'ACTIVE', 'deleted': false }")
    List<Document> findAllActive();

    @Query("{ 'status': 'ACTIVE', 'deleted': false }")
    Page<Document> findAllActive(Pageable pageable);

    // Find by ID with active filter
    @Query("{ '_id': ?0, 'status': 'ACTIVE', 'deleted': false }")
    Optional<Document> findByIdAndActive(String id);

    // Find by project
    @Query("{ 'accessControl.projectId': ?0, 'status': 'ACTIVE', 'deleted': false }")
    List<Document> findByProjectIdAndActive(UUID projectId);

    // Find by departments
    @Query("{ 'accessControl.departmentIds': { $in: ?0 }, 'status': 'ACTIVE', 'deleted': false }")
    List<Document> findByDepartmentIdsAndActive(List<UUID> departmentIds);

    // Find by project or departments
    @Query("{ $or: [ { 'accessControl.projectId': ?0 }, { 'accessControl.departmentIds': { $in: ?1 } } ], 'status': 'ACTIVE', 'deleted': false }")
    List<Document> findByProjectOrDepartmentsAndActive(UUID projectId, List<UUID> departmentIds);

    // Find by language
    @Query("{ 'language': ?0, 'status': 'ACTIVE', 'deleted': false }")
    List<Document> findByLanguageAndActive(String language);

    // Find by processing status
    @Query("{ 'processingStatus': ?0, 'status': 'ACTIVE', 'deleted': false }")
    List<Document> findByProcessingStatusAndActive(Document.ProcessingStatus processingStatus);

    // Find by uploaded user
    @Query("{ 'accessControl.uploadedBy': ?0, 'status': 'ACTIVE', 'deleted': false }")
    List<Document> findByUploadedByAndActive(UUID uploadedBy);

    // Search by title
    @Query("{ 'title': { $regex: ?0, $options: 'i' }, 'status': 'ACTIVE', 'deleted': false }")
    Page<Document> searchByTitleAndActive(String titlePattern, Pageable pageable);

    // Search by content
    @Query("{ 'content': { $regex: ?0, $options: 'i' }, 'status': 'ACTIVE', 'deleted': false }")
    Page<Document> searchByContentAndActive(String contentPattern, Pageable pageable);

    // Find by date range
    @Query("{ 'createdAt': { $gte: ?0, $lte: ?1 }, 'status': 'ACTIVE', 'deleted': false }")
    List<Document> findByCreatedAtBetweenAndActive(LocalDateTime startDate, LocalDateTime endDate);

    // Count by project
    @Query(value = "{ 'accessControl.projectId': ?0, 'status': 'ACTIVE', 'deleted': false }", count = true)
    long countByProjectIdAndActive(UUID projectId);

    // Count by departments
    @Query(value = "{ 'accessControl.departmentIds': { $in: ?0 }, 'status': 'ACTIVE', 'deleted': false }", count = true)
    long countByDepartmentIdsAndActive(List<UUID> departmentIds);

    // Find recent documents
    @Query("{ 'status': 'ACTIVE', 'deleted': false }")
    List<Document> findRecentDocuments(Pageable pageable);
}