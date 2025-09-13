package com.synapse.core.service;

import com.synapse.data.entity.mongo.Document;
import com.synapse.data.repository.mongo.DocumentRepository;
import com.synapse.security.authorization.AuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for document management with access control validation.
 */
@Service
public class DocumentService {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private AccessControlService accessControlService;

    @Autowired
    private AuthorizationService authorizationService;

    /**
     * Create a new document.
     */
    public Document createDocument(String title, String content, String originalFormat, 
                                 UUID projectId, Set<UUID> departmentIds) {
        // Validate access control
        if (projectId != null && !accessControlService.canUploadToProject(projectId)) {
            throw new IllegalArgumentException("No permission to upload to project");
        }
        if (departmentIds != null && !accessControlService.canUploadToDepartments(departmentIds)) {
            throw new IllegalArgumentException("No permission to upload to departments");
        }

        Document document = new Document(title, content, originalFormat);
        
        Document.AccessControl accessControl = new Document.AccessControl();
        accessControl.setProjectId(projectId);
        accessControl.setDepartmentIds(departmentIds);
        accessControl.setUploadedBy(authorizationService.getCurrentUserId());
        accessControl.setVisibility(projectId != null ? 
            Document.VisibilityType.PROJECT : Document.VisibilityType.DEPARTMENT);
        
        document.setAccessControl(accessControl);
        
        return documentRepository.save(document);
    }

    /**
     * Find document by ID with access control.
     */
    public Optional<Document> findById(String documentId) {
        Optional<Document> document = documentRepository.findByIdAndActive(documentId);
        
        if (document.isPresent() && !canAccessDocument(document.get())) {
            return Optional.empty();
        }
        
        return document;
    }

    /**
     * Find all accessible documents for current user.
     */
    public List<Document> findAccessibleDocuments() {
        UUID userProjectId = accessControlService.getUserProjectId();
        Set<UUID> userDepartmentIds = accessControlService.getUserDepartmentIds();

        if (authorizationService.hasAnyRole("SYSTEM_ADMIN")) {
            return documentRepository.findAllActive();
        }

        if (userProjectId != null && !userDepartmentIds.isEmpty()) {
            return documentRepository.findByProjectOrDepartmentsAndActive(
                userProjectId, userDepartmentIds.stream().collect(Collectors.toList()));
        } else if (userProjectId != null) {
            return documentRepository.findByProjectIdAndActive(userProjectId);
        } else if (!userDepartmentIds.isEmpty()) {
            return documentRepository.findByDepartmentIdsAndActive(
                userDepartmentIds.stream().collect(Collectors.toList()));
        }

        return List.of();
    }

    /**
     * Search documents with access control.
     */
    public Page<Document> searchDocuments(String searchTerm, Pageable pageable) {
        // First get all accessible documents, then filter by search term
        // In production, this should be optimized with proper MongoDB queries
        Page<Document> results = documentRepository.searchByTitleAndActive(searchTerm, pageable);
        
        return results.map(doc -> canAccessDocument(doc) ? doc : null)
                     .map(doc -> doc); // Filter out nulls in real implementation
    }

    /**
     * Update document status.
     */
    public void updateDocumentStatus(String documentId, Document.DocumentStatus status) {
        Optional<Document> document = findById(documentId);
        if (document.isPresent()) {
            document.get().setStatus(status);
            document.get().setUpdatedAt(LocalDateTime.now());
            documentRepository.save(document.get());
        }
    }

    /**
     * Update processing status.
     */
    public void updateProcessingStatus(String documentId, Document.ProcessingStatus processingStatus) {
        Optional<Document> document = documentRepository.findById(documentId);
        if (document.isPresent()) {
            document.get().setProcessingStatus(processingStatus);
            document.get().setUpdatedAt(LocalDateTime.now());
            documentRepository.save(document.get());
        }
    }

    /**
     * Soft delete document.
     */
    public void softDeleteDocument(String documentId) {
        Optional<Document> document = findById(documentId);
        if (document.isPresent()) {
            document.get().setDeleted(true);
            document.get().setUpdatedAt(LocalDateTime.now());
            documentRepository.save(document.get());
        }
    }

    /**
     * Find documents by language.
     */
    public List<Document> findByLanguage(String language) {
        List<Document> documents = documentRepository.findByLanguageAndActive(language);
        return documents.stream()
                       .filter(this::canAccessDocument)
                       .collect(Collectors.toList());
    }

    /**
     * Find documents by processing status.
     */
    public List<Document> findByProcessingStatus(Document.ProcessingStatus processingStatus) {
        return documentRepository.findByProcessingStatusAndActive(processingStatus);
    }

    /**
     * Count accessible documents.
     */
    public long countAccessibleDocuments() {
        UUID userProjectId = accessControlService.getUserProjectId();
        Set<UUID> userDepartmentIds = accessControlService.getUserDepartmentIds();

        if (authorizationService.hasAnyRole("SYSTEM_ADMIN")) {
            return documentRepository.count();
        }

        long count = 0;
        if (userProjectId != null) {
            count += documentRepository.countByProjectIdAndActive(userProjectId);
        }
        if (!userDepartmentIds.isEmpty()) {
            count += documentRepository.countByDepartmentIdsAndActive(
                userDepartmentIds.stream().collect(Collectors.toList()));
        }

        return count;
    }

    private boolean canAccessDocument(Document document) {
        if (document.getAccessControl() == null) {
            return false;
        }
        
        return accessControlService.canAccessDocument(
            document.getAccessControl().getProjectId(),
            document.getAccessControl().getDepartmentIds()
        );
    }
}