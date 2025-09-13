package com.synapse.web.dto;

import com.synapse.data.entity.mongo.Document;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * DTO for Document entity.
 */
public class DocumentDto {
    private String id;
    private String title;
    private String originalFormat;
    private String language;
    private Document.DocumentStatus status;
    private Document.ProcessingStatus processingStatus;
    private DocumentMetadataDto metadata;
    private AccessControlDto accessControl;
    private List<String> chunkIds;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public DocumentDto() {}

    public DocumentDto(Document document) {
        this.id = document.getId();
        this.title = document.getTitle();
        this.originalFormat = document.getOriginalFormat();
        this.language = document.getLanguage();
        this.status = document.getStatus();
        this.processingStatus = document.getProcessingStatus();
        this.chunkIds = document.getChunkIds();
        this.createdAt = document.getCreatedAt();
        this.updatedAt = document.getUpdatedAt();
        
        if (document.getMetadata() != null) {
            this.metadata = new DocumentMetadataDto(document.getMetadata());
        }
        
        if (document.getAccessControl() != null) {
            this.accessControl = new AccessControlDto(document.getAccessControl());
        }
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getOriginalFormat() { return originalFormat; }
    public void setOriginalFormat(String originalFormat) { this.originalFormat = originalFormat; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public Document.DocumentStatus getStatus() { return status; }
    public void setStatus(Document.DocumentStatus status) { this.status = status; }

    public Document.ProcessingStatus getProcessingStatus() { return processingStatus; }
    public void setProcessingStatus(Document.ProcessingStatus processingStatus) { this.processingStatus = processingStatus; }

    public DocumentMetadataDto getMetadata() { return metadata; }
    public void setMetadata(DocumentMetadataDto metadata) { this.metadata = metadata; }

    public AccessControlDto getAccessControl() { return accessControl; }
    public void setAccessControl(AccessControlDto accessControl) { this.accessControl = accessControl; }

    public List<String> getChunkIds() { return chunkIds; }
    public void setChunkIds(List<String> chunkIds) { this.chunkIds = chunkIds; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Nested DTOs
    public static class DocumentMetadataDto {
        private String author;
        private LocalDateTime createdDate;
        private String source;
        private List<String> tags;
        private Long fileSize;
        private String mimeType;

        public DocumentMetadataDto() {}

        public DocumentMetadataDto(Document.DocumentMetadata metadata) {
            this.author = metadata.getAuthor();
            this.createdDate = metadata.getCreatedDate();
            this.source = metadata.getSource();
            this.tags = metadata.getTags();
            this.fileSize = metadata.getFileSize();
            this.mimeType = metadata.getMimeType();
        }

        public String getAuthor() { return author; }
        public void setAuthor(String author) { this.author = author; }

        public LocalDateTime getCreatedDate() { return createdDate; }
        public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }

        public List<String> getTags() { return tags; }
        public void setTags(List<String> tags) { this.tags = tags; }

        public Long getFileSize() { return fileSize; }
        public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

        public String getMimeType() { return mimeType; }
        public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    }

    public static class AccessControlDto {
        private UUID projectId;
        private Set<UUID> departmentIds;
        private UUID uploadedBy;
        private Document.VisibilityType visibility;

        public AccessControlDto() {}

        public AccessControlDto(Document.AccessControl accessControl) {
            this.projectId = accessControl.getProjectId();
            this.departmentIds = accessControl.getDepartmentIds();
            this.uploadedBy = accessControl.getUploadedBy();
            this.visibility = accessControl.getVisibility();
        }

        public UUID getProjectId() { return projectId; }
        public void setProjectId(UUID projectId) { this.projectId = projectId; }

        public Set<UUID> getDepartmentIds() { return departmentIds; }
        public void setDepartmentIds(Set<UUID> departmentIds) { this.departmentIds = departmentIds; }

        public UUID getUploadedBy() { return uploadedBy; }
        public void setUploadedBy(UUID uploadedBy) { this.uploadedBy = uploadedBy; }

        public Document.VisibilityType getVisibility() { return visibility; }
        public void setVisibility(Document.VisibilityType visibility) { this.visibility = visibility; }
    }

    public static class UploadRequest {
        @NotBlank(message = "Title is required")
        @Size(max = 255, message = "Title must not exceed 255 characters")
        private String title;

        private UUID projectId;
        private Set<UUID> departmentIds;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public UUID getProjectId() { return projectId; }
        public void setProjectId(UUID projectId) { this.projectId = projectId; }

        public Set<UUID> getDepartmentIds() { return departmentIds; }
        public void setDepartmentIds(Set<UUID> departmentIds) { this.departmentIds = departmentIds; }
    }

    public static class StatusUpdateRequest {
        private Document.DocumentStatus status;

        public Document.DocumentStatus getStatus() { return status; }
        public void setStatus(Document.DocumentStatus status) { this.status = status; }
    }
}