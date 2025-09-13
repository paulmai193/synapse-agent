package com.synapse.core.service;

import com.synapse.data.entity.mongo.Chunk;
import com.synapse.data.entity.mongo.Document;
import com.synapse.data.repository.mongo.ChunkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Service for document upload handling and processing.
 */
@Service
public class DocumentProcessingService {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private ChunkRepository chunkRepository;

    @Autowired
    private AuditService auditService;

    @Value("${synapse.document.upload.max-file-size:104857600}") // 100MB default
    private long maxFileSize;

    @Value("${synapse.document.upload.allowed-types:pdf,docx,txt,md}")
    private String allowedTypes;

    private static final int CHUNK_SIZE = 1000; // words per chunk
    private static final int CHUNK_OVERLAP = 100; // word overlap

    /**
     * Process uploaded file and create document.
     */
    public Document processUploadedFile(MultipartFile file, String title, 
                                      UUID projectId, Set<UUID> departmentIds) throws IOException {
        // Validate file
        validateFile(file);
        
        // Extract content
        String content = extractContent(file);
        String detectedLanguage = detectLanguage(content);
        
        // Create document
        Document document = documentService.createDocument(
            title != null ? title : file.getOriginalFilename(),
            content,
            getFileExtension(file.getOriginalFilename()),
            projectId,
            departmentIds
        );
        
        document.setLanguage(detectedLanguage);
        
        // Set metadata
        Document.DocumentMetadata metadata = new Document.DocumentMetadata();
        metadata.setSource("upload");
        metadata.setFileSize(file.getSize());
        metadata.setMimeType(file.getContentType());
        document.setMetadata(metadata);
        
        // Update processing status
        documentService.updateProcessingStatus(document.getId(), Document.ProcessingStatus.PROCESSING);
        
        try {
            // Create chunks
            List<String> chunkIds = createChunks(document.getId(), content, detectedLanguage);
            document.setChunkIds(chunkIds);
            
            // Update status to completed
            documentService.updateProcessingStatus(document.getId(), Document.ProcessingStatus.COMPLETED);
            
            // Log document upload
            auditService.logAction("DOCUMENT_UPLOAD", "DOCUMENT", document.getId(), 
                java.util.Map.of("filename", file.getOriginalFilename(), "size", file.getSize()));
            
        } catch (Exception e) {
            // Update status to failed
            documentService.updateProcessingStatus(document.getId(), Document.ProcessingStatus.FAILED);
            throw new RuntimeException("Document processing failed", e);
        }
        
        return document;
    }

    /**
     * Validate uploaded file.
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size");
        }
        
        String extension = getFileExtension(file.getOriginalFilename());
        Set<String> allowedExtensions = Set.of(allowedTypes.split(","));
        
        if (!allowedExtensions.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("File type not supported: " + extension);
        }
        
        // Basic virus scanning (placeholder - implement with actual antivirus)
        if (containsSuspiciousContent(file)) {
            throw new IllegalArgumentException("File contains suspicious content");
        }
    }

    /**
     * Extract content from file based on format.
     */
    private String extractContent(MultipartFile file) throws IOException {
        String extension = getFileExtension(file.getOriginalFilename()).toLowerCase();
        
        switch (extension) {
            case "txt":
            case "md":
                return new String(file.getBytes(), StandardCharsets.UTF_8);
            case "pdf":
                return extractPdfContent(file);
            case "docx":
                return extractDocxContent(file);
            default:
                throw new IllegalArgumentException("Unsupported file format: " + extension);
        }
    }

    /**
     * Extract content from PDF file.
     */
    private String extractPdfContent(MultipartFile file) throws IOException {
        // Placeholder - implement with Apache PDFBox or similar
        return "PDF content extraction not implemented yet";
    }

    /**
     * Extract content from DOCX file.
     */
    private String extractDocxContent(MultipartFile file) throws IOException {
        // Placeholder - implement with Apache POI or similar
        return "DOCX content extraction not implemented yet";
    }

    /**
     * Detect language of content.
     */
    private String detectLanguage(String content) {
        // Placeholder - implement with language detection library
        // For now, assume English
        return "en";
    }

    /**
     * Create chunks from document content.
     */
    private List<String> createChunks(String documentId, String content, String language) {
        List<String> chunkIds = new ArrayList<>();
        String[] words = content.split("\\s+");
        
        for (int i = 0; i < words.length; i += CHUNK_SIZE - CHUNK_OVERLAP) {
            int endIndex = Math.min(i + CHUNK_SIZE, words.length);
            String chunkContent = String.join(" ", Arrays.copyOfRange(words, i, endIndex));
            
            Chunk chunk = new Chunk(documentId, chunkContent, chunkIds.size());
            chunk.setLanguage(language);
            
            // Set chunk metadata
            Chunk.ChunkMetadata metadata = new Chunk.ChunkMetadata();
            metadata.setStartPosition(i);
            metadata.setEndPosition(endIndex);
            metadata.setWordCount(endIndex - i);
            chunk.setMetadata(metadata);
            
            Chunk savedChunk = chunkRepository.save(chunk);
            chunkIds.add(savedChunk.getId());
        }
        
        return chunkIds;
    }

    /**
     * Get file extension from filename.
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    /**
     * Basic suspicious content detection.
     */
    private boolean containsSuspiciousContent(MultipartFile file) {
        // Placeholder - implement actual virus scanning
        return false;
    }

    /**
     * Reprocess failed document.
     */
    public void reprocessDocument(String documentId) {
        // Implementation for reprocessing failed documents
        documentService.updateProcessingStatus(documentId, Document.ProcessingStatus.PROCESSING);
        // Add reprocessing logic here
    }

    /**
     * Get processing statistics.
     */
    public ProcessingStatistics getProcessingStatistics() {
        long pending = documentService.findByProcessingStatus(Document.ProcessingStatus.PENDING).size();
        long processing = documentService.findByProcessingStatus(Document.ProcessingStatus.PROCESSING).size();
        long completed = documentService.findByProcessingStatus(Document.ProcessingStatus.COMPLETED).size();
        long failed = documentService.findByProcessingStatus(Document.ProcessingStatus.FAILED).size();
        
        return new ProcessingStatistics(pending, processing, completed, failed);
    }

    /**
     * Processing statistics DTO.
     */
    public static class ProcessingStatistics {
        private long pending;
        private long processing;
        private long completed;
        private long failed;

        public ProcessingStatistics(long pending, long processing, long completed, long failed) {
            this.pending = pending;
            this.processing = processing;
            this.completed = completed;
            this.failed = failed;
        }

        public long getPending() { return pending; }
        public long getProcessing() { return processing; }
        public long getCompleted() { return completed; }
        public long getFailed() { return failed; }
    }
}