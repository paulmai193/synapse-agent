package com.synapse.data.repository.jpa;

import com.synapse.data.entity.UserProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for UserProject entity operations.
 */
@Repository
public interface UserProjectRepository extends JpaRepository<UserProject, UUID> {

    // Find by user ID
    Optional<UserProject> findByUserId(UUID userId);

    // Find by project ID
    @Query("SELECT up FROM UserProject up WHERE up.project.projectId = :projectId AND up.project.deleted = false")
    List<UserProject> findByProjectIdAndNotDeleted(@Param("projectId") UUID projectId);

    // Find users in active projects
    @Query("SELECT up FROM UserProject up WHERE up.project.status = 'ACTIVE' AND up.project.deleted = false")
    List<UserProject> findByActiveProjects();

    // Check if user has project assignment
    boolean existsByUserId(UUID userId);

    // Count users in project
    @Query("SELECT COUNT(up) FROM UserProject up WHERE up.project.projectId = :projectId AND up.project.deleted = false")
    long countByProjectIdAndNotDeleted(@Param("projectId") UUID projectId);
}