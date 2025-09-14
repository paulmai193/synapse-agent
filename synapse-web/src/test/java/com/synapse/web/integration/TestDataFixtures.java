package com.synapse.web.integration;

import com.synapse.data.entity.*;
import com.synapse.data.repository.jpa.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Test data fixtures for integration tests.
 * Task 14.1: Create comprehensive integration test suite
 */
@Component
@Transactional
public class TestDataFixtures {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    public User createTestUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash("$2a$10$test.hash");
        user.setStatus(User.UserStatus.ACTIVE);
        return userRepository.save(user);
    }

    public Project createTestProject(String name) {
        Project project = new Project();
        project.setName(name);
        project.setDescription("Test project");
        project.setStatus(Project.ProjectStatus.ACTIVE);
        return projectRepository.save(project);
    }

    public Department createTestDepartment(String name) {
        Department department = new Department();
        department.setName(name);
        department.setDescription("Test department");
        department.setStatus(Department.DepartmentStatus.ACTIVE);
        return departmentRepository.save(department);
    }

    public Role createTestRole(String name) {
        Role role = new Role();
        role.setName(name);
        role.setDescription("Test role");
        return roleRepository.save(role);
    }

    public void cleanupTestData() {
        userRepository.deleteAll();
        projectRepository.deleteAll();
        departmentRepository.deleteAll();
        roleRepository.deleteAll();
    }
}