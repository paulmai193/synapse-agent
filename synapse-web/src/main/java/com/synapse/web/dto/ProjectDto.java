package com.synapse.web.dto;

import com.synapse.data.entity.Project;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for Project entity.
 */
public class ProjectDto {
    private UUID projectId;
    private String name;
    private String description;
    private Project.ProjectStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ProjectDto() {}

    public ProjectDto(Project project) {
        this.projectId = project.getProjectId();
        this.name = project.getName();
        this.description = project.getDescription();
        this.status = project.getStatus();
        this.createdAt = project.getCreatedAt();
        this.updatedAt = project.getUpdatedAt();
    }

    // Getters and Setters
    public UUID getProjectId() { return projectId; }
    public void setProjectId(UUID projectId) { this.projectId = projectId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Project.ProjectStatus getStatus() { return status; }
    public void setStatus(Project.ProjectStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static class CreateProjectRequest {
        @NotBlank(message = "Project name is required")
        @Size(max = 255, message = "Project name must not exceed 255 characters")
        private String name;

        private String description;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class StatusUpdateRequest {
        private Project.ProjectStatus status;

        public Project.ProjectStatus getStatus() { return status; }
        public void setStatus(Project.ProjectStatus status) { this.status = status; }
    }
}