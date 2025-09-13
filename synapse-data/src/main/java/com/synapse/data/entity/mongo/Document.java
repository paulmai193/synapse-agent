package com.synapse.data.entity.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * MongoDB document entity for storing document information.
 */
@Document(collection = "documents")
public class Document {

    @Id
    private String id;

    @Indexed
    private String title;

    private String content;

    private String originalFormat;

    @Indexed
    private String language;

    @Indexed
    private DocumentStatus status = DocumentStatus.ACTIVE;

    @Indexed
    private Boolean deleted = false;

    private DocumentMetadata metadata;

    private AccessControl accessControl;

    private List<String> chunkIds;

    @Indexed
    private ProcessingStatus processingStatus = ProcessingStatus.PENDING;

    @Indexed
    private LocalDateTime createdAt = LocalDateTime.now();

    @Indexed
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Constructors
    public Document() {}

    public Document(String title, String content, String originalFormat) {
        this.title = title;
        this.content = content;
        this.originalFormat = originalFormat;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getOriginalFormat() { return originalFormat; }
    public void setOriginalFormat(String originalFormat) { this.originalFormat = originalFormat; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public DocumentStatus getStatus() { return status; }
    public void setStatus(DocumentStatus status) { this.status = status; }

    public Boolean getDeleted() { return deleted; }
    public void setDeleted(Boolean deleted) { this.deleted = deleted; }

    public DocumentMetadata getMetadata() { return metadata; }
    public void setMetadata(DocumentMetadata metadata) { this.metadata = metadata; }

    public AccessControl getAccessControl() { return accessControl; }
    public void setAccessControl(AccessControl accessControl) { this.accessControl = accessControl; }

    public List<String> getChunkIds() { return chunkIds; }
    public void setChunkIds(List<String> chunkIds) { this.chunkIds = chunkIds; }

    public ProcessingStatus getProcessingStatus() { return processingStatus; }
    public void setProcessingStatus(ProcessingStatus processingStatus) { this.processingStatus = processingStatus; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Nested classes
    public static class DocumentMetadata {
        private String author;
        private LocalDateTime createdDate;
        private String source;
        private String sourceId;
        private List<String> tags;
        private Long fileSize;
        private String mimeType;

        public String getAuthor() { return author; }
        public void setAuthor(String author) { this.author = author; }

        public LocalDateTime getCreatedDate() { return createdDate; }
        public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }

        public String getSourceId() { return sourceId; }
        public void setSourceId(String sourceId) { this.sourceId = sourceId; }

        public List<String> getTags() { return tags; }
        public void setTags(List<String> tags) { this.tags = tags; }

        public Long getFileSize() { return fileSize; }
        public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

        public String getMimeType() { return mimeType; }
        public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    }

    public static class AccessControl {
        private UUID projectId;
        private Set<UUID> departmentIds;
        private UUID uploadedBy;
        private VisibilityType visibility;

        public UUID getProjectId() { return projectId; }
        public void setProjectId(UUID projectId) { this.projectId = projectId; }

        public Set<UUID> getDepartmentIds() { return departmentIds; }
        public void setDepartmentIds(Set<UUID> departmentIds) { this.departmentIds = departmentIds; }

        public UUID getUploadedBy() { return uploadedBy; }
        public void setUploadedBy(UUID uploadedBy) { this.uploadedBy = uploadedBy; }

        public VisibilityType getVisibility() { return visibility; }
        public void setVisibility(VisibilityType visibility) { this.visibility = visibility; }
    }

    public enum DocumentStatus {
        ACTIVE, INACTIVE
    }

    public enum ProcessingStatus {
        PENDING, PROCESSING, COMPLETED, FAILED
    }

    public enum VisibilityType {
        PROJECT, DEPARTMENT, RESTRICTED
    }
}