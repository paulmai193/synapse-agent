package com.synapse.core.initialization;

import com.synapse.data.entity.Role;
import com.synapse.data.entity.User;
import com.synapse.data.repository.jpa.RoleRepository;
import com.synapse.data.repository.jpa.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DataInitializationService.
 */
@ExtendWith(MockitoExtension.class)
class DataInitializationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private DataInitializationService dataInitializationService;

    private Role systemAdminRole;

    @BeforeEach
    void setUp() {
        systemAdminRole = new Role(Role.SYSTEM_ADMIN, "System Administrator", null);
        systemAdminRole.setRoleId(UUID.randomUUID());
    }

    @Test
    void shouldCreateDefaultAdminUserWhenNoneExists() {
        when(userRepository.findByRoleNameAndNotDeleted(Role.SYSTEM_ADMIN)).thenReturn(Collections.emptyList());
        when(userRepository.existsByUsernameAndNotDeleted("admin")).thenReturn(false);
        when(roleRepository.findByName(Role.SYSTEM_ADMIN)).thenReturn(Optional.of(systemAdminRole));
        when(passwordEncoder.encode(anyString())).thenReturn("hashedpassword");
        when(userRepository.save(any(User.class))).thenReturn(new User());

        dataInitializationService.initializeSystemData();

        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldSkipCreationWhenAdminUserExists() {
        User existingAdmin = new User("admin", "admin@synapse.local", "hashedpassword");
        when(userRepository.findByRoleNameAndNotDeleted(Role.SYSTEM_ADMIN)).thenReturn(Collections.singletonList(existingAdmin));

        dataInitializationService.initializeSystemData();

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldReturnTrueWhenInitialized() {
        User existingAdmin = new User("admin", "admin@synapse.local", "hashedpassword");
        when(userRepository.findByRoleNameAndNotDeleted(Role.SYSTEM_ADMIN)).thenReturn(Collections.singletonList(existingAdmin));

        boolean initialized = dataInitializationService.isInitialized();

        assertThat(initialized).isTrue();
    }

    @Test
    void shouldReturnCompletedSetupStatus() {
        User existingAdmin = new User("admin", "admin@synapse.local", "hashedpassword");
        when(userRepository.findByRoleNameAndNotDeleted(Role.SYSTEM_ADMIN)).thenReturn(Collections.singletonList(existingAdmin));

        DataInitializationService.SetupStatus status = dataInitializationService.getSetupStatus();

        assertThat(status.isCompleted()).isTrue();
        assertThat(status.getMessage()).isEqualTo("System setup completed");
    }
}