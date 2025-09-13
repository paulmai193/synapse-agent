package com.synapse.web.controller;

import com.synapse.core.service.DocumentProcessingService;
import com.synapse.core.service.DocumentService;
import com.synapse.data.entity.mongo.Document;
import com.synapse.web.dto.DocumentDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST controller for document management operations.
 */
@RestController
@RequestMapping("/documents")
@CrossOrigin(origins = "*")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private DocumentProcessingService documentProcessingService;

    @PostMapping("/upload")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @Valid @ModelAttribute DocumentDto.UploadRequest request) {
        try {
            Document document = documentProcessingService.processUploadedFile(
                file, 
                request.getTitle(), 
                request.getProjectId(), 
                request.getDepartmentIds()
            );
            
            return ResponseEntity.ok(new DocumentDto(document));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("VALIDATION_ERROR", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                .body(new ErrorResponse("PROCESSING_ERROR", "Failed to process document"));
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<DocumentDto>> getAllDocuments() {
        List<Document> documents = documentService.findAccessibleDocuments();
        List<DocumentDto> documentDtos = documents.stream()
            .map(DocumentDto::new)
            .collect(Collectors.toList());
        return ResponseEntity.ok(documentDtos);
    }

    @GetMapping("/{documentId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<DocumentDto> getDocument(@PathVariable String documentId) {
        Optional<Document> document = documentService.findById(documentId);
        return document.map(doc -> ResponseEntity.ok(new DocumentDto(doc)))
                      .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Page<DocumentDto>> searchDocuments(
            @RequestParam String query, 
            Pageable pageable) {
        Page<Document> documents = documentService.searchDocuments(query, pageable);
        Page<DocumentDto> documentDtos = new PageImpl<>(
            documents.getContent().stream()
                .map(DocumentDto::new)
                .collect(Collectors.toList()),
            pageable,
            documents.getTotalElements()
        );
        return ResponseEntity.ok(documentDtos);
    }

    @GetMapping("/language/{language}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<DocumentDto>> getDocumentsByLanguage(@PathVariable String language) {
        List<Document> documents = documentService.findByLanguage(language);
        List<DocumentDto> documentDtos = documents.stream()
            .map(DocumentDto::new)
            .collect(Collectors.toList());
        return ResponseEntity.ok(documentDtos);
    }

    @PutMapping("/{documentId}/status")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('PROJECT_ADMIN') or hasRole('DEPARTMENT_ADMIN')")
    public ResponseEntity<Void> updateDocumentStatus(
            @PathVariable String documentId,
            @Valid @RequestBody DocumentDto.StatusUpdateRequest request) {
        try {
            documentService.updateDocumentStatus(documentId, request.getStatus());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{documentId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('PROJECT_ADMIN') or hasRole('DEPARTMENT_ADMIN')")
    public ResponseEntity<Void> softDeleteDocument(@PathVariable String documentId) {
        try {
            documentService.softDeleteDocument(documentId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{documentId}/reprocess")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<Void> reprocessDocument(@PathVariable String documentId) {
        try {
            documentProcessingService.reprocessDocument(documentId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/statistics/processing")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<DocumentProcessingService.ProcessingStatistics> getProcessingStatistics() {
        DocumentProcessingService.ProcessingStatistics stats = 
            documentProcessingService.getProcessingStatistics();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/count")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Long> getDocumentCount() {
        long count = documentService.countAccessibleDocuments();
        return ResponseEntity.ok(count);
    }

    // Error response DTO
    public static class ErrorResponse {
        private String code;
        private String message;

        public ErrorResponse(String code, String message) {
            this.code = code;
            this.message = message;
        }

        public String getCode() { return code; }
        public String getMessage() { return message; }
    }
}