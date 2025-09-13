package com.synapse.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.synapse.core.service.DocumentProcessingService;
import com.synapse.core.service.DocumentService;
import com.synapse.data.entity.mongo.Document;
import com.synapse.web.dto.DocumentDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for DocumentController.
 */
@WebMvcTest(DocumentController.class)
class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocumentService documentService;

    @MockBean
    private DocumentProcessingService documentProcessingService;

    @Autowired
    private ObjectMapper objectMapper;

    private Document testDocument;

    @BeforeEach
    void setUp() {
        testDocument = new Document("Test Document", "Test content", "txt");
        testDocument.setId("doc123");
        testDocument.setLanguage("en");
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldUploadDocument() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file", 
            "test.txt", 
            "text/plain", 
            "Test content".getBytes()
        );

        when(documentProcessingService.processUploadedFile(any(), anyString(), any(), any()))
            .thenReturn(testDocument);

        mockMvc.perform(multipart("/documents/upload")
                .file(file)
                .param("title", "Test Document")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("doc123"))
                .andExpect(jsonPath("$.title").value("Test Document"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldGetAllDocuments() throws Exception {
        when(documentService.findAccessibleDocuments()).thenReturn(Arrays.asList(testDocument));

        mockMvc.perform(get("/documents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("doc123"))
                .andExpect(jsonPath("$[0].title").value("Test Document"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldGetDocumentById() throws Exception {
        when(documentService.findById("doc123")).thenReturn(Optional.of(testDocument));

        mockMvc.perform(get("/documents/doc123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("doc123"))
                .andExpect(jsonPath("$.title").value("Test Document"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturnNotFoundForNonexistentDocument() throws Exception {
        when(documentService.findById("nonexistent")).thenReturn(Optional.empty());

        mockMvc.perform(get("/documents/nonexistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "SYSTEM_ADMIN")
    void shouldUpdateDocumentStatus() throws Exception {
        DocumentDto.StatusUpdateRequest request = new DocumentDto.StatusUpdateRequest();
        request.setStatus(Document.DocumentStatus.INACTIVE);

        mockMvc.perform(put("/documents/doc123/status")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SYSTEM_ADMIN")
    void shouldSoftDeleteDocument() throws Exception {
        mockMvc.perform(delete("/documents/doc123")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldDenyAccessToAdminEndpoints() throws Exception {
        mockMvc.perform(delete("/documents/doc123")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }
}