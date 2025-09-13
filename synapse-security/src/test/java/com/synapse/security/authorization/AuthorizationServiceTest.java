package com.synapse.security.authorization;

import com.synapse.data.entity.*;
import com.synapse.data.repository.jpa.UserRepository;
import com.synapse.security.service.UserDetailsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AuthorizationService.
 */
@ExtendWith(MockitoExtension.class)
class AuthorizationServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthorizationService authorizationService;

    private User testUser;
    private Role systemAdminRole;
    private Project testProject;
    private Department testDepartment;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "test@example.com", "hashedpassword");
        testUser.setUserId(UUID.randomUUID());

        systemAdminRole = new Role("SYSTEM_ADMIN", "System Administrator", null);
        testProject = new Project("Test Project", "Description");
        testProject.setProjectId(UUID.randomUUID());
        testDepartment = new Department("Test Department", "Description");
        testDepartment.setDepartmentId(UUID.randomUUID());

        // Mock security context
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        UserDetailsServiceImpl.UserPrincipal userPrincipal = new UserDetailsServiceImpl.UserPrincipal(testUser);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void shouldReturnTrueWhenUserHasRole() {
        Set<Role> roles = new HashSet<>();
        roles.add(systemAdminRole);
        testUser.setRoles(roles);

        boolean hasRole = authorizationService.hasAnyRole("SYSTEM_ADMIN");

        assertThat(hasRole).isTrue();
    }

    @Test
    void shouldReturnTrueWhenSystemAdminAccessesProject() {
        Set<Role> roles = new HashSet<>();
        roles.add(systemAdminRole);
        testUser.setRoles(roles);

        boolean hasAccess = authorizationService.hasProjectAccess(testProject.getProjectId());

        assertThat(hasAccess).isTrue();
    }

    @Test
    void shouldReturnCurrentUser() {
        User currentUser = authorizationService.getCurrentUser();

        assertThat(currentUser).isEqualTo(testUser);
    }
}