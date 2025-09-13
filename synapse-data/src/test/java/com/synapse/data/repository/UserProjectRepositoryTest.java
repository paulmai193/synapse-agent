package com.synapse.data.repository;

import com.synapse.data.entity.Project;
import com.synapse.data.entity.User;
import com.synapse.data.entity.UserProject;
import com.synapse.data.repository.jpa.ProjectRepository;
import com.synapse.data.repository.jpa.UserProjectRepository;
import com.synapse.data.repository.jpa.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for UserProjectRepository.
 */
@DataJpaTest
@ActiveProfiles("test")
class UserProjectRepositoryTest {

    @Autowired
    private UserProjectRepository userProjectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    private User testUser;
    private Project testProject;
    private UserProject userProject;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "test@example.com", "hashedpassword");
        userRepository.save(testUser);

        testProject = new Project("Test Project", "Test Description");
        projectRepository.save(testProject);

        userProject = new UserProject(testUser, testProject);
        userProjectRepository.save(userProject);
    }

    @Test
    void shouldFindByUserId() {
        Optional<UserProject> found = userProjectRepository.findByUserId(testUser.getUserId());
        
        assertThat(found).isPresent();
        assertThat(found.get().getProject().getName()).isEqualTo("Test Project");
    }

    @Test
    void shouldFindByProjectIdAndNotDeleted() {
        List<UserProject> userProjects = userProjectRepository.findByProjectIdAndNotDeleted(testProject.getProjectId());
        
        assertThat(userProjects).hasSize(1);
        assertThat(userProjects.get(0).getUser().getUsername()).isEqualTo("testuser");
    }

    @Test
    void shouldNotFindDeletedProjects() {
        testProject.setDeleted(true);
        projectRepository.save(testProject);

        List<UserProject> userProjects = userProjectRepository.findByProjectIdAndNotDeleted(testProject.getProjectId());
        
        assertThat(userProjects).isEmpty();
    }

    @Test
    void shouldCheckIfUserHasProjectAssignment() {
        boolean exists = userProjectRepository.existsByUserId(testUser.getUserId());
        
        assertThat(exists).isTrue();
    }

    @Test
    void shouldCountUsersByProject() {
        long count = userProjectRepository.countByProjectIdAndNotDeleted(testProject.getProjectId());
        
        assertThat(count).isEqualTo(1);
    }
}