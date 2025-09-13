package com.synapse.core.service;

import com.synapse.data.entity.User;
import com.synapse.security.authorization.AuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for document access control with project/department filtering.
 */
@Service
public class AccessControlService {

    @Autowired
    private AuthorizationService authorizationService;

    /**
     * Check if current user can access document based on project/department assignment.
     */
    public boolean canAccessDocument(UUID projectId, Set<UUID> departmentIds) {
        User currentUser = authorizationService.getCurrentUser();
        if (currentUser == null) {
            return false;
        }

        // SYSTEM_ADMIN can access all documents
        if (authorizationService.hasAnyRole("SYSTEM_ADMIN")) {
            return true;
        }

        // Check project access
        if (projectId != null && authorizationService.hasProjectAccess(projectId)) {
            return true;
        }

        // Check department access
        if (departmentIds != null && !departmentIds.isEmpty()) {
            return departmentIds.stream()
                .anyMatch(deptId -> authorizationService.hasDepartmentAccess(deptId));
        }

        return false;
    }

    /**
     * Filter document IDs based on user access permissions.
     */
    public List<String> filterAccessibleDocuments(List<DocumentAccessInfo> documents) {
        return documents.stream()
            .filter(doc -> canAccessDocument(doc.getProjectId(), doc.getDepartmentIds()))
            .map(DocumentAccessInfo::getDocumentId)
            .collect(Collectors.toList());
    }

    /**
     * Get user's accessible project ID.
     */
    public UUID getUserProjectId() {
        User currentUser = authorizationService.getCurrentUser();
        if (currentUser == null || currentUser.getUserProject() == null) {
            return null;
        }
        return currentUser.getUserProject().getProject().getProjectId();
    }

    /**
     * Get user's accessible department IDs.
     */
    public Set<UUID> getUserDepartmentIds() {
        User currentUser = authorizationService.getCurrentUser();
        if (currentUser == null) {
            return Set.of();
        }
        return currentUser.getUserDepartments().stream()
            .map(ud -> ud.getDepartment().getDepartmentId())
            .collect(Collectors.toSet());
    }

    /**
     * Check if user can upload documents to specified project/departments.
     */
    public boolean canUploadToProject(UUID projectId) {
        return authorizationService.hasProjectAccess(projectId);
    }

    /**
     * Check if user can upload documents to specified departments.
     */
    public boolean canUploadToDepartments(Set<UUID> departmentIds) {
        if (departmentIds == null || departmentIds.isEmpty()) {
            return false;
        }
        return departmentIds.stream()
            .allMatch(deptId -> authorizationService.hasDepartmentAccess(deptId));
    }

    /**
     * Document access information DTO.
     */
    public static class DocumentAccessInfo {
        private String documentId;
        private UUID projectId;
        private Set<UUID> departmentIds;

        public DocumentAccessInfo(String documentId, UUID projectId, Set<UUID> departmentIds) {
            this.documentId = documentId;
            this.projectId = projectId;
            this.departmentIds = departmentIds;
        }

        public String getDocumentId() { return documentId; }
        public UUID getProjectId() { return projectId; }
        public Set<UUID> getDepartmentIds() { return departmentIds; }
    }
}