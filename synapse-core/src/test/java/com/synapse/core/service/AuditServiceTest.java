package com.synapse.core.service;

import com.synapse.data.entity.AuditLog;
import com.synapse.data.repository.jpa.AuditLogRepository;
import com.synapse.security.authorization.AuthorizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AuditService.
 */
@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private AuthorizationService authorizationService;

    @InjectMocks
    private AuditService auditService;

    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        when(authorizationService.getCurrentUserId()).thenReturn(testUserId);
    }

    @Test
    void shouldLogAction() {
        auditService.logAction(AuditLog.USER_LOGIN, "USER", testUserId.toString(), 
            Map.of("success", true));

        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void shouldLogUserLogin() {
        auditService.logUserLogin(testUserId, null, "Mozilla/5.0");

        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void shouldLogSearchQuery() {
        auditService.logSearchQuery("test query", 5);

        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void shouldLogQAQuery() {
        auditService.logQAQuery("test question", "test answer", 0.95);

        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void shouldLogDocumentAccess() {
        auditService.logDocumentAccess("doc123", "VIEW");

        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void shouldLogAdminAction() {
        auditService.logAdminAction("CREATE_USER", "USER", "user123", 
            Map.of("username", "newuser"));

        verify(auditLogRepository).save(any(AuditLog.class));
    }
}