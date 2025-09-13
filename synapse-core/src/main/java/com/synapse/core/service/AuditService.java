package com.synapse.core.service;

import com.synapse.data.entity.AuditLog;
import com.synapse.data.repository.jpa.AuditLogRepository;
import com.synapse.security.authorization.AuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Service for audit logging functionality.
 */
@Service
@Transactional
public class AuditService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private AuthorizationService authorizationService;

    /**
     * Log user action with details.
     */
    public void logAction(String actionType, String resourceType, String resourceId, Map<String, Object> details) {
        UUID currentUserId = authorizationService.getCurrentUserId();
        logAction(currentUserId, actionType, resourceType, resourceId, details, null, null);
    }

    /**
     * Log user action with IP and user agent.
     */
    public void logAction(String actionType, String resourceType, String resourceId, 
                         Map<String, Object> details, InetAddress ipAddress, String userAgent) {
        UUID currentUserId = authorizationService.getCurrentUserId();
        logAction(currentUserId, actionType, resourceType, resourceId, details, ipAddress, userAgent);
    }

    /**
     * Log action for specific user.
     */
    public void logAction(UUID userId, String actionType, String resourceType, String resourceId, 
                         Map<String, Object> details, InetAddress ipAddress, String userAgent) {
        AuditLog auditLog = new AuditLog(userId, actionType, resourceType, resourceId);
        auditLog.setDetails(details);
        auditLog.setIpAddress(ipAddress);
        auditLog.setUserAgent(userAgent);
        
        auditLogRepository.save(auditLog);
    }

    /**
     * Log user login.
     */
    public void logUserLogin(UUID userId, InetAddress ipAddress, String userAgent) {
        logAction(userId, AuditLog.USER_LOGIN, "USER", userId.toString(), 
                 Map.of("success", true), ipAddress, userAgent);
    }

    /**
     * Log user logout.
     */
    public void logUserLogout(UUID userId) {
        logAction(userId, AuditLog.USER_LOGOUT, "USER", userId.toString(), null, null, null);
    }

    /**
     * Log search query.
     */
    public void logSearchQuery(String query, int resultCount) {
        logAction(AuditLog.SEARCH_QUERY, "SEARCH", null, 
                 Map.of("query", query, "resultCount", resultCount));
    }

    /**
     * Log Q&A query.
     */
    public void logQAQuery(String question, String answer, double confidence) {
        logAction(AuditLog.QA_QUERY, "QA", null, 
                 Map.of("question", question, "answerLength", answer.length(), "confidence", confidence));
    }

    /**
     * Log document access.
     */
    public void logDocumentAccess(String documentId, String action) {
        logAction(AuditLog.DOCUMENT_ACCESS, "DOCUMENT", documentId, 
                 Map.of("action", action));
    }

    /**
     * Log administrative action.
     */
    public void logAdminAction(String action, String targetType, String targetId, Map<String, Object> details) {
        logAction(AuditLog.ADMIN_ACTION, targetType, targetId, 
                 Map.of("adminAction", action, "details", details));
    }

    /**
     * Get audit logs with pagination.
     */
    public Page<AuditLog> getAuditLogs(Pageable pageable) {
        return auditLogRepository.findAll(pageable);
    }

    /**
     * Search audit logs by criteria.
     */
    public Page<AuditLog> searchAuditLogs(UUID userId, String actionType, String resourceType, 
                                         LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        return auditLogRepository.findByCriteria(userId, actionType, resourceType, startTime, endTime, pageable);
    }

    /**
     * Get audit logs for specific user.
     */
    public Page<AuditLog> getUserAuditLogs(UUID userId, Pageable pageable) {
        return auditLogRepository.findByUserIdOrderByTimestampDesc(userId, pageable);
    }

    /**
     * Get audit logs by action type.
     */
    public Page<AuditLog> getAuditLogsByAction(String actionType, Pageable pageable) {
        return auditLogRepository.findByActionTypeOrderByTimestampDesc(actionType, pageable);
    }

    /**
     * Get audit statistics.
     */
    public AuditStatistics getAuditStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        long totalLogs = auditLogRepository.countByTimestampBetween(startTime, endTime);
        long loginCount = auditLogRepository.countByActionType(AuditLog.USER_LOGIN);
        long searchCount = auditLogRepository.countByActionType(AuditLog.SEARCH_QUERY);
        long qaCount = auditLogRepository.countByActionType(AuditLog.QA_QUERY);
        
        return new AuditStatistics(totalLogs, loginCount, searchCount, qaCount);
    }

    /**
     * Audit statistics DTO.
     */
    public static class AuditStatistics {
        private long totalLogs;
        private long loginCount;
        private long searchCount;
        private long qaCount;

        public AuditStatistics(long totalLogs, long loginCount, long searchCount, long qaCount) {
            this.totalLogs = totalLogs;
            this.loginCount = loginCount;
            this.searchCount = searchCount;
            this.qaCount = qaCount;
        }

        public long getTotalLogs() { return totalLogs; }
        public long getLoginCount() { return loginCount; }
        public long getSearchCount() { return searchCount; }
        public long getQaCount() { return qaCount; }
    }
}