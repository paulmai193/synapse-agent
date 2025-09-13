package com.synapse.security.authorization;

import com.synapse.core.service.AccessControlService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;
import java.util.UUID;

/**
 * Interceptor for document access control validation.
 */
@Component
public class DocumentAccessInterceptor implements HandlerInterceptor {

    @Autowired
    private AccessControlService accessControlService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();

        // Check document access for document-related endpoints
        if (requestURI.contains("/documents/") && !requestURI.contains("/upload")) {
            String documentId = extractDocumentId(requestURI);
            if (documentId != null && !canAccessDocument(documentId, request)) {
                throw new AccessDeniedException("Access denied to document: " + documentId);
            }
        }

        // Check search access
        if (requestURI.contains("/search") || requestURI.contains("/qa")) {
            if (!hasSearchAccess()) {
                throw new AccessDeniedException("Access denied to search functionality");
            }
        }

        return true;
    }

    private String extractDocumentId(String requestURI) {
        String[] parts = requestURI.split("/");
        for (int i = 0; i < parts.length; i++) {
            if ("documents".equals(parts[i]) && i + 1 < parts.length) {
                return parts[i + 1];
            }
        }
        return null;
    }

    private boolean canAccessDocument(String documentId, HttpServletRequest request) {
        // Extract project/department info from request headers or parameters
        String projectIdHeader = request.getHeader("X-Project-Id");
        String departmentIdsHeader = request.getHeader("X-Department-Ids");

        UUID projectId = projectIdHeader != null ? UUID.fromString(projectIdHeader) : null;
        Set<UUID> departmentIds = departmentIdsHeader != null ? 
            Set.of(departmentIdsHeader.split(",")).stream()
                .map(UUID::fromString)
                .collect(java.util.stream.Collectors.toSet()) : 
            Set.of();

        return accessControlService.canAccessDocument(projectId, departmentIds);
    }

    private boolean hasSearchAccess() {
        // Users with any role can perform searches within their scope
        return accessControlService.getUserProjectId() != null || 
               !accessControlService.getUserDepartmentIds().isEmpty();
    }
}