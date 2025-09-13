package com.synapse.core.service;

import com.synapse.data.entity.*;
import com.synapse.security.authorization.AuthorizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AccessControlService.
 */
@ExtendWith(MockitoExtension.class)
class AccessControlServiceTest {

    @Mock
    private AuthorizationService authorizationService;

    @InjectMocks
    private AccessControlService accessControlService;

    private UUID projectId;
    private UUID departmentId;
    private Set<UUID> departmentIds;

    @BeforeEach
    void setUp() {
        projectId = UUID.randomUUID();
        departmentId = UUID.randomUUID();
        departmentIds = Set.of(departmentId);
    }

    @Test
    void shouldAllowAccessWhenUserIsSystemAdmin() {
        when(authorizationService.getCurrentUser()).thenReturn(new User());
        when(authorizationService.hasAnyRole("SYSTEM_ADMIN")).thenReturn(true);

        boolean canAccess = accessControlService.canAccessDocument(projectId, departmentIds);

        assertThat(canAccess).isTrue();
    }

    @Test
    void shouldAllowAccessWhenUserHasProjectAccess() {
        when(authorizationService.getCurrentUser()).thenReturn(new User());
        when(authorizationService.hasAnyRole("SYSTEM_ADMIN")).thenReturn(false);
        when(authorizationService.hasProjectAccess(projectId)).thenReturn(true);

        boolean canAccess = accessControlService.canAccessDocument(projectId, departmentIds);

        assertThat(canAccess).isTrue();
    }

    @Test
    void shouldAllowAccessWhenUserHasDepartmentAccess() {
        when(authorizationService.getCurrentUser()).thenReturn(new User());
        when(authorizationService.hasAnyRole("SYSTEM_ADMIN")).thenReturn(false);
        when(authorizationService.hasProjectAccess(projectId)).thenReturn(false);
        when(authorizationService.hasDepartmentAccess(departmentId)).thenReturn(true);

        boolean canAccess = accessControlService.canAccessDocument(projectId, departmentIds);

        assertThat(canAccess).isTrue();
    }

    @Test
    void shouldDenyAccessWhenUserHasNoPermissions() {
        when(authorizationService.getCurrentUser()).thenReturn(new User());
        when(authorizationService.hasAnyRole("SYSTEM_ADMIN")).thenReturn(false);
        when(authorizationService.hasProjectAccess(projectId)).thenReturn(false);
        when(authorizationService.hasDepartmentAccess(departmentId)).thenReturn(false);

        boolean canAccess = accessControlService.canAccessDocument(projectId, departmentIds);

        assertThat(canAccess).isFalse();
    }

    @Test
    void shouldFilterAccessibleDocuments() {
        when(authorizationService.getCurrentUser()).thenReturn(new User());
        when(authorizationService.hasAnyRole("SYSTEM_ADMIN")).thenReturn(false);
        when(authorizationService.hasProjectAccess(projectId)).thenReturn(true);

        List<AccessControlService.DocumentAccessInfo> documents = Arrays.asList(
            new AccessControlService.DocumentAccessInfo("doc1", projectId, departmentIds),
            new AccessControlService.DocumentAccessInfo("doc2", UUID.randomUUID(), departmentIds)
        );

        List<String> accessibleDocs = accessControlService.filterAccessibleDocuments(documents);

        assertThat(accessibleDocs).containsExactly("doc1");
    }
}