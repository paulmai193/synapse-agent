package com.synapse.web.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for document upload and processing pipeline.
 * Task 14.1: Create comprehensive integration test suite
 */
@SpringBootTest
@AutoConfigureWebMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class DocumentProcessingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should upload and process document successfully")
    void testDocumentUpload() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file", "test.txt", "text/plain", "Test document content".getBytes());

        mockMvc.perform(multipart("/api/documents/upload")
                .file(file)
                .param("title", "Test Document")
                .param("projectId", "1"))
                .andExpected(status().isOk())
                .andExpected(jsonPath("$.title").value("Test Document"));
    }

    @Test
    @DisplayName("Should reject unsupported file format")
    void testUnsupportedFileFormat() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file", "test.exe", "application/octet-stream", "Binary content".getBytes());

        mockMvc.perform(multipart("/api/documents/upload")
                .file(file)
                .param("title", "Invalid Document"))
                .andExpected(status().isBadRequest());
    }
}