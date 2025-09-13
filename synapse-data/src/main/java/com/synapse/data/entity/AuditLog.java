package com.synapse.data.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * AuditLog entity for tracking user activities and system events.
 */
@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_user_id", columnList = "user_id"),
    @Index(name = "idx_audit_timestamp", columnList = "timestamp"),
    @Index(name = "idx_audit_action_type", columnList = "action_type"),
    @Index(name = "idx_audit_resource", columnList = "resource_type, resource_id")
})
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "log_id")
    private UUID logId;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "action_type", nullable = false, length = 100)
    private String actionType;

    @Column(name = "resource_type", length = 100)
    private String resourceType;

    @Column(name = "resource_id")
    private String resourceId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "details", columnDefinition = "jsonb")
    private Map<String, Object> details;

    @Column(name = "ip_address", columnDefinition = "inet")
    private InetAddress ipAddress;

    @Column(name = "user_agent", columnDefinition = "text")
    private String userAgent;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    // Constructors
    public AuditLog() {}

    public AuditLog(UUID userId, String actionType, String resourceType, String resourceId) {
        this.userId = userId;
        this.actionType = actionType;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    // Getters and Setters
    public UUID getLogId() { return logId; }
    public void setLogId(UUID logId) { this.logId = logId; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }

    public String getResourceId() { return resourceId; }
    public void setResourceId(String resourceId) { this.resourceId = resourceId; }

    public Map<String, Object> getDetails() { return details; }
    public void setDetails(Map<String, Object> details) { this.details = details; }

    public InetAddress getIpAddress() { return ipAddress; }
    public void setIpAddress(InetAddress ipAddress) { this.ipAddress = ipAddress; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    // Audit action types
    public static final String USER_LOGIN = "USER_LOGIN";
    public static final String USER_LOGOUT = "USER_LOGOUT";
    public static final String USER_REGISTER = "USER_REGISTER";
    public static final String USER_UPDATE = "USER_UPDATE";
    public static final String USER_DELETE = "USER_DELETE";
    public static final String ROLE_ASSIGN = "ROLE_ASSIGN";
    public static final String ROLE_REMOVE = "ROLE_REMOVE";
    public static final String PROJECT_ASSIGN = "PROJECT_ASSIGN";
    public static final String DEPARTMENT_ASSIGN = "DEPARTMENT_ASSIGN";
    public static final String DOCUMENT_UPLOAD = "DOCUMENT_UPLOAD";
    public static final String DOCUMENT_ACCESS = "DOCUMENT_ACCESS";
    public static final String DOCUMENT_DELETE = "DOCUMENT_DELETE";
    public static final String SEARCH_QUERY = "SEARCH_QUERY";
    public static final String QA_QUERY = "QA_QUERY";
    public static final String ADMIN_ACTION = "ADMIN_ACTION";
}