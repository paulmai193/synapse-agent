package com.synapse.security.authorization;

import com.synapse.data.entity.User;
import com.synapse.data.repository.jpa.UserRepository;
import com.synapse.security.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for authorization and permission validation.
 */
@Service
public class AuthorizationService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Check if current user has any of the specified roles.
     */
    public boolean hasAnyRole(String... roles) {
        User currentUser = getCurrentUser();
        if (currentUser == null) return false;

        Set<String> userRoles = currentUser.getRoles().stream()
            .map(role -> role.getName())
            .collect(Collectors.toSet());

        for (String role : roles) {
            if (userRoles.contains(role)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if current user has all of the specified roles.
     */
    public boolean hasAllRoles(String... roles) {
        User currentUser = getCurrentUser();
        if (currentUser == null) return false;

        Set<String> userRoles = currentUser.getRoles().stream()
            .map(role -> role.getName())
            .collect(Collectors.toSet());

        for (String role : roles) {
            if (!userRoles.contains(role)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if current user has access to specified project.
     */
    public boolean hasProjectAccess(UUID projectId) {
        User currentUser = getCurrentUser();
        if (currentUser == null) return false;

        // SYSTEM_ADMIN has access to all projects
        if (hasAnyRole("SYSTEM_ADMIN")) {
            return true;
        }

        // Check if user is assigned to the project
        if (currentUser.getUserProject() != null) {
            return currentUser.getUserProject().getProject().getProjectId().equals(projectId);
        }

        return false;
    }

    /**
     * Check if current user has access to specified department.
     */
    public boolean hasDepartmentAccess(UUID departmentId) {
        User currentUser = getCurrentUser();
        if (currentUser == null) return false;

        // SYSTEM_ADMIN has access to all departments
        if (hasAnyRole("SYSTEM_ADMIN")) {
            return true;
        }

        // Check if user is assigned to the department
        return currentUser.getUserDepartments().stream()
            .anyMatch(ud -> ud.getDepartment().getDepartmentId().equals(departmentId));
    }

    /**
     * Check if current user can manage users (has admin role).
     */
    public boolean canManageUsers() {
        return hasAnyRole("SYSTEM_ADMIN", "PROJECT_ADMIN", "DEPARTMENT_ADMIN");
    }

    /**
     * Check if current user can manage projects.
     */
    public boolean canManageProjects() {
        return hasAnyRole("SYSTEM_ADMIN");
    }

    /**
     * Check if current user can manage departments.
     */
    public boolean canManageDepartments() {
        return hasAnyRole("SYSTEM_ADMIN");
    }

    /**
     * Check if current user can access user data.
     */
    public boolean canAccessUser(UUID userId) {
        User currentUser = getCurrentUser();
        if (currentUser == null) return false;

        // Users can access their own data
        if (currentUser.getUserId().equals(userId)) {
            return true;
        }

        // Admins can access user data
        return canManageUsers();
    }

    /**
     * Get current authenticated user.
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetailsServiceImpl.UserPrincipal) {
            UserDetailsServiceImpl.UserPrincipal userPrincipal = (UserDetailsServiceImpl.UserPrincipal) principal;
            return userPrincipal.getUser();
        }

        return null;
    }

    /**
     * Get current user ID.
     */
    public UUID getCurrentUserId() {
        User currentUser = getCurrentUser();
        return currentUser != null ? currentUser.getUserId() : null;
    }
}