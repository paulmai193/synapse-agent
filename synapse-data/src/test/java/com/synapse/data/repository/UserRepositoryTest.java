package com.synapse.data.repository;

import com.synapse.data.entity.User;
import com.synapse.data.entity.Role;
import com.synapse.data.repository.jpa.UserRepository;
import com.synapse.data.repository.jpa.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for UserRepository.
 */
@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private User testUser;
    private Role userRole;

    @BeforeEach
    void setUp() {
        // Create test role
        userRole = new Role("USER", "Regular user role", null);
        roleRepository.save(userRole);

        // Create test user
        testUser = new User("testuser", "test@example.com", "hashedpassword");
        testUser.addRole(userRole);
        userRepository.save(testUser);
    }

    @Test
    void shouldFindAllActiveUsers() {
        List<User> activeUsers = userRepository.findAllActive();
        
        assertThat(activeUsers).hasSize(1);
        assertThat(activeUsers.get(0).getUsername()).isEqualTo("testuser");
    }

    @Test
    void shouldNotFindDeletedUsers() {
        testUser.setDeleted(true);
        userRepository.save(testUser);

        List<User> activeUsers = userRepository.findAllActive();
        
        assertThat(activeUsers).isEmpty();
    }

    @Test
    void shouldFindUserByUsernameAndNotDeleted() {
        Optional<User> foundUser = userRepository.findByUsernameAndNotDeleted("testuser");
        
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void shouldNotFindDeletedUserByUsername() {
        testUser.setDeleted(true);
        userRepository.save(testUser);

        Optional<User> foundUser = userRepository.findByUsernameAndNotDeleted("testuser");
        
        assertThat(foundUser).isEmpty();
    }

    @Test
    void shouldFindUserByEmailAndNotDeleted() {
        Optional<User> foundUser = userRepository.findByEmailAndNotDeleted("test@example.com");
        
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("testuser");
    }

    @Test
    void shouldFindUsersByStatus() {
        List<User> activeUsers = userRepository.findByStatusAndNotDeleted(User.UserStatus.ACTIVE);
        
        assertThat(activeUsers).hasSize(1);
        assertThat(activeUsers.get(0).getStatus()).isEqualTo(User.UserStatus.ACTIVE);
    }

    @Test
    void shouldFindUsersByRoleName() {
        List<User> usersWithRole = userRepository.findByRoleNameAndNotDeleted("USER");
        
        assertThat(usersWithRole).hasSize(1);
        assertThat(usersWithRole.get(0).getRoles()).contains(userRole);
    }

    @Test
    void shouldCheckIfUsernameExists() {
        boolean exists = userRepository.existsByUsernameAndNotDeleted("testuser");
        boolean notExists = userRepository.existsByUsernameAndNotDeleted("nonexistent");
        
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    void shouldCheckIfEmailExists() {
        boolean exists = userRepository.existsByEmailAndNotDeleted("test@example.com");
        boolean notExists = userRepository.existsByEmailAndNotDeleted("nonexistent@example.com");
        
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    void shouldSearchUsersByUsernameOrEmail() {
        Page<User> searchResults = userRepository.searchByUsernameOrEmailAndNotDeleted(
            "test", PageRequest.of(0, 10));
        
        assertThat(searchResults.getContent()).hasSize(1);
        assertThat(searchResults.getContent().get(0).getUsername()).isEqualTo("testuser");
    }
}