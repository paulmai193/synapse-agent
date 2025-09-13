package com.synapse.core.service;

import com.synapse.data.entity.mongo.Document;
import com.synapse.data.repository.mongo.ChunkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for DocumentProcessingService.
 */
@ExtendWith(MockitoExtension.class)
class DocumentProcessingServiceTest {

    @Mock
    private DocumentService documentService;

    @Mock
    private ChunkRepository chunkRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private DocumentProcessingService documentProcessingService;

    private MultipartFile validFile;
    private Document testDocument;
    private UUID projectId;

    @BeforeEach
    void setUp() {
        validFile = new MockMultipartFile(
            "file", 
            "test.txt", 
            "text/plain", 
            "This is test content for document processing.".getBytes()
        );
        
        testDocument = new Document("Test Document", "Test content", "txt");
        testDocument.setId("doc123");
        
        projectId = UUID.randomUUID();
    }

    @Test
    void shouldProcessValidTextFile() throws IOException {
        when(documentService.createDocument(anyString(), anyString(), anyString(), any(), any()))
            .thenReturn(testDocument);
        when(chunkRepository.save(any())).thenReturn(new com.synapse.data.entity.mongo.Chunk());

        Document result = documentProcessingService.processUploadedFile(validFile, "Test Title", projectId, null);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("doc123");
        verify(documentService).updateProcessingStatus("doc123", Document.ProcessingStatus.PROCESSING);
        verify(documentService).updateProcessingStatus("doc123", Document.ProcessingStatus.COMPLETED);
    }

    @Test
    void shouldThrowExceptionForEmptyFile() {
        MultipartFile emptyFile = new MockMultipartFile("file", "empty.txt", "text/plain", new byte[0]);

        assertThatThrownBy(() -> documentProcessingService.processUploadedFile(emptyFile, "Title", projectId, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("File is empty");
    }

    @Test
    void shouldThrowExceptionForUnsupportedFileType() {
        MultipartFile unsupportedFile = new MockMultipartFile(
            "file", 
            "test.exe", 
            "application/octet-stream", 
            "binary content".getBytes()
        );

        assertThatThrownBy(() -> documentProcessingService.processUploadedFile(unsupportedFile, "Title", projectId, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("File type not supported");
    }

    @Test
    void shouldGetProcessingStatistics() {
        when(documentService.findByProcessingStatus(Document.ProcessingStatus.PENDING)).thenReturn(java.util.List.of());
        when(documentService.findByProcessingStatus(Document.ProcessingStatus.PROCESSING)).thenReturn(java.util.List.of());
        when(documentService.findByProcessingStatus(Document.ProcessingStatus.COMPLETED)).thenReturn(java.util.List.of(testDocument));
        when(documentService.findByProcessingStatus(Document.ProcessingStatus.FAILED)).thenReturn(java.util.List.of());

        DocumentProcessingService.ProcessingStatistics stats = documentProcessingService.getProcessingStatistics();

        assertThat(stats.getPending()).isEqualTo(0);
        assertThat(stats.getProcessing()).isEqualTo(0);
        assertThat(stats.getCompleted()).isEqualTo(1);
        assertThat(stats.getFailed()).isEqualTo(0);
    }
}