package com.synapse.web.dto;

import com.synapse.data.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * DTO for User entity.
 */
public class UserDto {
    private UUID userId;
    private String username;
    private String email;
    private User.UserStatus status;
    private Set<String> roles;
    private String projectName;
    private Set<String> departmentNames;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public UserDto() {}

    public UserDto(User user) {
        this.userId = user.getUserId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.status = user.getStatus();
        this.roles = user.getRoles().stream()
            .map(role -> role.getName())
            .collect(Collectors.toSet());
        this.projectName = user.getUserProject() != null ? 
            user.getUserProject().getProject().getName() : null;
        this.departmentNames = user.getUserDepartments().stream()
            .map(ud -> ud.getDepartment().getName())
            .collect(Collectors.toSet());
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
    }

    // Getters and Setters
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public User.UserStatus getStatus() { return status; }
    public void setStatus(User.UserStatus status) { this.status = status; }

    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = roles; }

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public Set<String> getDepartmentNames() { return departmentNames; }
    public void setDepartmentNames(Set<String> departmentNames) { this.departmentNames = departmentNames; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static class RegisterRequest {
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 255, message = "Username must be between 3 and 255 characters")
        private String username;

        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        private String email;

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class RoleAssignmentRequest {
        @NotBlank(message = "Role name is required")
        private String roleName;

        public String getRoleName() { return roleName; }
        public void setRoleName(String roleName) { this.roleName = roleName; }
    }

    public static class ProjectAssignmentRequest {
        private UUID projectId;

        public UUID getProjectId() { return projectId; }
        public void setProjectId(UUID projectId) { this.projectId = projectId; }
    }

    public static class DepartmentAssignmentRequest {
        private UUID departmentId;

        public UUID getDepartmentId() { return departmentId; }
        public void setDepartmentId(UUID departmentId) { this.departmentId = departmentId; }
    }

    public static class StatusUpdateRequest {
        private User.UserStatus status;

        public User.UserStatus getStatus() { return status; }
        public void setStatus(User.UserStatus status) { this.status = status; }
    }
}