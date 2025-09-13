package com.synapse.data.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * UserDepartment entity representing user-department assignments (multiple departments per user).
 */
@Entity
@Table(name = "user_departments")
public class UserDepartment {

    @EmbeddedId
    private UserDepartmentId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @MapsId("userId")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    @MapsId("departmentId")
    private Department department;

    @CreationTimestamp
    @Column(name = "assigned_at", nullable = false, updatable = false)
    private LocalDateTime assignedAt;

    // Constructors
    public UserDepartment() {}

    public UserDepartment(User user, Department department) {
        this.user = user;
        this.department = department;
        this.id = new UserDepartmentId(user.getUserId(), department.getDepartmentId());
    }

    // Getters and Setters
    public UserDepartmentId getId() { return id; }
    public void setId(UserDepartmentId id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }

    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }

    @Embeddable
    public static class UserDepartmentId {
        @Column(name = "user_id")
        private UUID userId;

        @Column(name = "department_id")
        private UUID departmentId;

        public UserDepartmentId() {}

        public UserDepartmentId(UUID userId, UUID departmentId) {
            this.userId = userId;
            this.departmentId = departmentId;
        }

        public UUID getUserId() { return userId; }
        public void setUserId(UUID userId) { this.userId = userId; }

        public UUID getDepartmentId() { return departmentId; }
        public void setDepartmentId(UUID departmentId) { this.departmentId = departmentId; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof UserDepartmentId)) return false;
            UserDepartmentId that = (UserDepartmentId) o;
            return userId.equals(that.userId) && departmentId.equals(that.departmentId);
        }

        @Override
        public int hashCode() {
            return userId.hashCode() + departmentId.hashCode();
        }
    }
}