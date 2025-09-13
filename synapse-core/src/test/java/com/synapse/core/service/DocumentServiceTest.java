package com.synapse.core.service;

import com.synapse.data.entity.mongo.Document;
import com.synapse.data.repository.mongo.DocumentRepository;
import com.synapse.security.authorization.AuthorizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for DocumentService.
 */
@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private AccessControlService accessControlService;

    @Mock
    private AuthorizationService authorizationService;

    @InjectMocks
    private DocumentService documentService;

    private UUID projectId;
    private UUID departmentId;
    private UUID userId;
    private Document testDocument;

    @BeforeEach
    void setUp() {
        projectId = UUID.randomUUID();
        departmentId = UUID.randomUUID();
        userId = UUID.randomUUID();
        
        testDocument = new Document("Test Document", "Test content", "txt");
        testDocument.setId("doc123");
        
        Document.AccessControl accessControl = new Document.AccessControl();
        accessControl.setProjectId(projectId);
        accessControl.setDepartmentIds(Set.of(departmentId));
        testDocument.setAccessControl(accessControl);
    }

    @Test
    void shouldCreateDocumentWithValidAccess() {
        when(accessControlService.canUploadToProject(projectId)).thenReturn(true);
        when(authorizationService.getCurrentUserId()).thenReturn(userId);
        when(documentRepository.save(any(Document.class))).thenReturn(testDocument);

        Document result = documentService.createDocument("Test", "Content", "txt", projectId, null);

        assertThat(result).isNotNull();
        verify(documentRepository).save(any(Document.class));
    }

    @Test
    void shouldThrowExceptionWhenNoProjectAccess() {
        when(accessControlService.canUploadToProject(projectId)).thenReturn(false);

        assertThatThrownBy(() -> documentService.createDocument("Test", "Content", "txt", projectId, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("No permission to upload to project");
    }

    @Test
    void shouldFindDocumentWithAccess() {
        when(documentRepository.findByIdAndActive("doc123")).thenReturn(Optional.of(testDocument));
        when(accessControlService.canAccessDocument(projectId, Set.of(departmentId))).thenReturn(true);

        Optional<Document> result = documentService.findById("doc123");

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo("doc123");
    }

    @Test
    void shouldNotFindDocumentWithoutAccess() {
        when(documentRepository.findByIdAndActive("doc123")).thenReturn(Optional.of(testDocument));
        when(accessControlService.canAccessDocument(projectId, Set.of(departmentId))).thenReturn(false);

        Optional<Document> result = documentService.findById("doc123");

        assertThat(result).isEmpty();
    }

    @Test
    void shouldFindAccessibleDocumentsForSystemAdmin() {
        when(authorizationService.hasAnyRole("SYSTEM_ADMIN")).thenReturn(true);
        when(documentRepository.findAllActive()).thenReturn(List.of(testDocument));

        List<Document> result = documentService.findAccessibleDocuments();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("doc123");
    }

    @Test
    void shouldUpdateDocumentStatus() {
        when(documentRepository.findByIdAndActive("doc123")).thenReturn(Optional.of(testDocument));
        when(accessControlService.canAccessDocument(projectId, Set.of(departmentId))).thenReturn(true);
        when(documentRepository.save(any(Document.class))).thenReturn(testDocument);

        documentService.updateDocumentStatus("doc123", Document.DocumentStatus.INACTIVE);

        verify(documentRepository).save(testDocument);
    }

    @Test
    void shouldSoftDeleteDocument() {
        when(documentRepository.findByIdAndActive("doc123")).thenReturn(Optional.of(testDocument));
        when(accessControlService.canAccessDocument(projectId, Set.of(departmentId))).thenReturn(true);
        when(documentRepository.save(any(Document.class))).thenReturn(testDocument);

        documentService.softDeleteDocument("doc123");

        verify(documentRepository).save(testDocument);
        assertThat(testDocument.getDeleted()).isTrue();
    }
}