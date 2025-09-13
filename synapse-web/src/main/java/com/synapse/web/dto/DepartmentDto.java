package com.synapse.web.dto;

import com.synapse.data.entity.Department;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for Department entity.
 */
public class DepartmentDto {
    private UUID departmentId;
    private String name;
    private String description;
    private Department.DepartmentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public DepartmentDto() {}

    public DepartmentDto(Department department) {
        this.departmentId = department.getDepartmentId();
        this.name = department.getName();
        this.description = department.getDescription();
        this.status = department.getStatus();
        this.createdAt = department.getCreatedAt();
        this.updatedAt = department.getUpdatedAt();
    }

    // Getters and Setters
    public UUID getDepartmentId() { return departmentId; }
    public void setDepartmentId(UUID departmentId) { this.departmentId = departmentId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Department.DepartmentStatus getStatus() { return status; }
    public void setStatus(Department.DepartmentStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static class CreateDepartmentRequest {
        @NotBlank(message = "Department name is required")
        @Size(max = 255, message = "Department name must not exceed 255 characters")
        private String name;

        private String description;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    public static class StatusUpdateRequest {
        private Department.DepartmentStatus status;

        public Department.DepartmentStatus getStatus() { return status; }
        public void setStatus(Department.DepartmentStatus status) { this.status = status; }
    }
}