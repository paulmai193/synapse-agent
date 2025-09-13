package com.synapse.core.service;

import com.synapse.data.entity.*;
import com.synapse.data.repository.jpa.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Service for user management operations including registration and role assignment.
 */
@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Register a new user with default USER role.
     */
    public User registerUser(String username, String email, String password) {
        if (userRepository.existsByUsernameAndNotDeleted(username)) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmailAndNotDeleted(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User(username, email, passwordEncoder.encode(password));
        
        // Assign default USER role
        Role userRole = roleRepository.findByName(Role.USER)
            .orElseThrow(() -> new IllegalStateException("Default USER role not found"));
        user.addRole(userRole);

        return userRepository.save(user);
    }

    /**
     * Assign role to user.
     */
    public void assignRole(UUID userId, String roleName) {
        User user = findActiveUserById(userId);
        Role role = roleRepository.findByName(roleName)
            .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName));
        
        user.addRole(role);
        userRepository.save(user);
    }

    /**
     * Remove role from user.
     */
    public void removeRole(UUID userId, String roleName) {
        User user = findActiveUserById(userId);
        Role role = roleRepository.findByName(roleName)
            .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName));
        
        user.removeRole(role);
        userRepository.save(user);
    }

    /**
     * Assign user to project (single project per user).
     */
    public void assignProject(UUID userId, UUID projectId) {
        User user = findActiveUserById(userId);
        Project project = projectRepository.findByIdAndNotDeleted(projectId)
            .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        // Remove existing project assignment
        if (user.getUserProject() != null) {
            user.setUserProject(null);
        }

        // Create new project assignment
        UserProject userProject = new UserProject(user, project);
        user.setUserProject(userProject);
        
        userRepository.save(user);
    }

    /**
     * Remove user from project.
     */
    public void removeFromProject(UUID userId) {
        User user = findActiveUserById(userId);
        user.setUserProject(null);
        userRepository.save(user);
    }

    /**
     * Assign user to department (multiple departments allowed).
     */
    public void assignDepartment(UUID userId, UUID departmentId) {
        User user = findActiveUserById(userId);
        Department department = departmentRepository.findByIdAndNotDeleted(departmentId)
            .orElseThrow(() -> new IllegalArgumentException("Department not found"));

        UserDepartment userDepartment = new UserDepartment(user, department);
        user.getUserDepartments().add(userDepartment);
        
        userRepository.save(user);
    }

    /**
     * Remove user from department.
     */
    public void removeFromDepartment(UUID userId, UUID departmentId) {
        User user = findActiveUserById(userId);
        user.getUserDepartments().removeIf(ud -> 
            ud.getDepartment().getDepartmentId().equals(departmentId));
        
        userRepository.save(user);
    }

    /**
     * Update user status.
     */
    public void updateUserStatus(UUID userId, User.UserStatus status) {
        User user = findActiveUserById(userId);
        user.setStatus(status);
        userRepository.save(user);
    }

    /**
     * Soft delete user.
     */
    public void softDeleteUser(UUID userId) {
        User user = findActiveUserById(userId);
        user.setDeleted(true);
        userRepository.save(user);
    }

    /**
     * Find user by username.
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsernameAndNotDeleted(username);
    }

    /**
     * Find all active users.
     */
    public List<User> findAllActiveUsers() {
        return userRepository.findAllActive();
    }

    /**
     * Find users by role.
     */
    public List<User> findUsersByRole(String roleName) {
        return userRepository.findByRoleNameAndNotDeleted(roleName);
    }

    /**
     * Check if user has role.
     */
    public boolean hasRole(UUID userId, String roleName) {
        User user = findActiveUserById(userId);
        return user.getRoles().stream()
            .anyMatch(role -> role.getName().equals(roleName));
    }

    /**
     * Get user's project.
     */
    public Optional<Project> getUserProject(UUID userId) {
        User user = findActiveUserById(userId);
        return Optional.ofNullable(user.getUserProject())
            .map(UserProject::getProject);
    }

    /**
     * Get user's departments.
     */
    public Set<Department> getUserDepartments(UUID userId) {
        User user = findActiveUserById(userId);
        return user.getUserDepartments().stream()
            .map(UserDepartment::getDepartment)
            .collect(java.util.stream.Collectors.toSet());
    }

    private User findActiveUserById(UUID userId) {
        return userRepository.findById(userId)
            .filter(user -> !user.getDeleted())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}