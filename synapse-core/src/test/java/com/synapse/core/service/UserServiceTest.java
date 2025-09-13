package com.synapse.core.service;

import com.synapse.data.entity.*;
import com.synapse.data.repository.jpa.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private Role userRole;
    private Project testProject;
    private Department testDepartment;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "test@example.com", "hashedpassword");
        testUser.setUserId(UUID.randomUUID());

        userRole = new Role("USER", "Regular user role", null);
        userRole.setRoleId(UUID.randomUUID());

        testProject = new Project("Test Project", "Test Description");
        testProject.setProjectId(UUID.randomUUID());

        testDepartment = new Department("Test Department", "Test Description");
        testDepartment.setDepartmentId(UUID.randomUUID());
    }

    @Test
    void shouldRegisterUserWithDefaultRole() {
        when(userRepository.existsByUsernameAndNotDeleted("testuser")).thenReturn(false);
        when(userRepository.existsByEmailAndNotDeleted("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("hashedpassword");
        when(roleRepository.findByName(Role.USER)).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.registerUser("testuser", "test@example.com", "password");

        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenUsernameExists() {
        when(userRepository.existsByUsernameAndNotDeleted("testuser")).thenReturn(true);

        assertThatThrownBy(() -> userService.registerUser("testuser", "test@example.com", "password"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Username already exists");
    }

    @Test
    void shouldThrowExceptionWhenEmailExists() {
        when(userRepository.existsByUsernameAndNotDeleted("testuser")).thenReturn(false);
        when(userRepository.existsByEmailAndNotDeleted("test@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.registerUser("testuser", "test@example.com", "password"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Email already exists");
    }

    @Test
    void shouldAssignRoleToUser() {
        UUID userId = testUser.getUserId();
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.assignRole(userId, "ADMIN");

        verify(userRepository).save(testUser);
    }

    @Test
    void shouldAssignProjectToUser() {
        UUID userId = testUser.getUserId();
        UUID projectId = testProject.getProjectId();
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(projectRepository.findByIdAndNotDeleted(projectId)).thenReturn(Optional.of(testProject));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.assignProject(userId, projectId);

        verify(userRepository).save(testUser);
    }

    @Test
    void shouldAssignDepartmentToUser() {
        UUID userId = testUser.getUserId();
        UUID departmentId = testDepartment.getDepartmentId();
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(departmentRepository.findByIdAndNotDeleted(departmentId)).thenReturn(Optional.of(testDepartment));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.assignDepartment(userId, departmentId);

        verify(userRepository).save(testUser);
    }

    @Test
    void shouldUpdateUserStatus() {
        UUID userId = testUser.getUserId();
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.updateUserStatus(userId, User.UserStatus.INACTIVE);

        assertThat(testUser.getStatus()).isEqualTo(User.UserStatus.INACTIVE);
        verify(userRepository).save(testUser);
    }

    @Test
    void shouldSoftDeleteUser() {
        UUID userId = testUser.getUserId();
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.softDeleteUser(userId);

        assertThat(testUser.getDeleted()).isTrue();
        verify(userRepository).save(testUser);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUserStatus(userId, User.UserStatus.INACTIVE))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("User not found");
    }
}