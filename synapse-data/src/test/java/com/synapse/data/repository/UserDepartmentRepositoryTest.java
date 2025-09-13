package com.synapse.data.repository;

import com.synapse.data.entity.Department;
import com.synapse.data.entity.User;
import com.synapse.data.entity.UserDepartment;
import com.synapse.data.repository.jpa.DepartmentRepository;
import com.synapse.data.repository.jpa.UserDepartmentRepository;
import com.synapse.data.repository.jpa.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for UserDepartmentRepository.
 */
@DataJpaTest
@ActiveProfiles("test")
class UserDepartmentRepositoryTest {

    @Autowired
    private UserDepartmentRepository userDepartmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    private User testUser;
    private Department testDepartment;
    private UserDepartment userDepartment;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "test@example.com", "hashedpassword");
        userRepository.save(testUser);

        testDepartment = new Department("Test Department", "Test Description");
        departmentRepository.save(testDepartment);

        userDepartment = new UserDepartment(testUser, testDepartment);
        userDepartmentRepository.save(userDepartment);
    }

    @Test
    void shouldFindByUserIdAndNotDeleted() {
        List<UserDepartment> userDepartments = userDepartmentRepository.findByUserIdAndNotDeleted(testUser.getUserId());
        
        assertThat(userDepartments).hasSize(1);
        assertThat(userDepartments.get(0).getDepartment().getName()).isEqualTo("Test Department");
    }

    @Test
    void shouldFindByDepartmentIdAndNotDeleted() {
        List<UserDepartment> userDepartments = userDepartmentRepository.findByDepartmentIdAndNotDeleted(testDepartment.getDepartmentId());
        
        assertThat(userDepartments).hasSize(1);
        assertThat(userDepartments.get(0).getUser().getUsername()).isEqualTo("testuser");
    }

    @Test
    void shouldNotFindDeletedDepartments() {
        testDepartment.setDeleted(true);
        departmentRepository.save(testDepartment);

        List<UserDepartment> userDepartments = userDepartmentRepository.findByDepartmentIdAndNotDeleted(testDepartment.getDepartmentId());
        
        assertThat(userDepartments).isEmpty();
    }

    @Test
    void shouldCheckIfUserIsInDepartment() {
        boolean exists = userDepartmentRepository.existsByUserIdAndDepartmentIdAndNotDeleted(
            testUser.getUserId(), testDepartment.getDepartmentId());
        
        assertThat(exists).isTrue();
    }

    @Test
    void shouldCountUsersByDepartment() {
        long count = userDepartmentRepository.countByDepartmentIdAndNotDeleted(testDepartment.getDepartmentId());
        
        assertThat(count).isEqualTo(1);
    }
}