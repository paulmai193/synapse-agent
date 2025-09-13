package com.synapse.data.repository.mongo;

import com.synapse.data.entity.mongo.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for DocumentRepository.
 */
@DataMongoTest
@ActiveProfiles("test")
class DocumentRepositoryTest {

    @Autowired
    private DocumentRepository documentRepository;

    private Document testDocument;
    private UUID projectId;
    private UUID departmentId;

    @BeforeEach
    void setUp() {
        documentRepository.deleteAll();
        
        projectId = UUID.randomUUID();
        departmentId = UUID.randomUUID();
        
        testDocument = new Document("Test Document", "Test content", "txt");
        testDocument.setLanguage("en");
        
        Document.AccessControl accessControl = new Document.AccessControl();
        accessControl.setProjectId(projectId);
        accessControl.setDepartmentIds(Set.of(departmentId));
        accessControl.setVisibility(Document.VisibilityType.PROJECT);
        testDocument.setAccessControl(accessControl);
        
        documentRepository.save(testDocument);
    }

    @Test
    void shouldFindAllActiveDocuments() {
        List<Document> activeDocuments = documentRepository.findAllActive();
        
        assertThat(activeDocuments).hasSize(1);
        assertThat(activeDocuments.get(0).getTitle()).isEqualTo("Test Document");
    }

    @Test
    void shouldNotFindDeletedDocuments() {
        testDocument.setDeleted(true);
        documentRepository.save(testDocument);

        List<Document> activeDocuments = documentRepository.findAllActive();
        
        assertThat(activeDocuments).isEmpty();
    }

    @Test
    void shouldFindByIdAndActive() {
        Optional<Document> found = documentRepository.findByIdAndActive(testDocument.getId());
        
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Test Document");
    }

    @Test
    void shouldFindByProjectId() {
        List<Document> documents = documentRepository.findByProjectIdAndActive(projectId);
        
        assertThat(documents).hasSize(1);
        assertThat(documents.get(0).getAccessControl().getProjectId()).isEqualTo(projectId);
    }

    @Test
    void shouldFindByDepartmentIds() {
        List<Document> documents = documentRepository.findByDepartmentIdsAndActive(List.of(departmentId));
        
        assertThat(documents).hasSize(1);
        assertThat(documents.get(0).getAccessControl().getDepartmentIds()).contains(departmentId);
    }

    @Test
    void shouldFindByLanguage() {
        List<Document> documents = documentRepository.findByLanguageAndActive("en");
        
        assertThat(documents).hasSize(1);
        assertThat(documents.get(0).getLanguage()).isEqualTo("en");
    }

    @Test
    void shouldCountByProjectId() {
        long count = documentRepository.countByProjectIdAndActive(projectId);
        
        assertThat(count).isEqualTo(1);
    }
}