package com.synapse.data.repository.jpa;

import com.synapse.data.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Project entity operations.
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {

    // Active projects only (not deleted)
    @Query("SELECT p FROM Project p WHERE p.deleted = false")
    List<Project> findAllActive();

    @Query("SELECT p FROM Project p WHERE p.deleted = false")
    Page<Project> findAllActive(Pageable pageable);

    // Find by ID with active filter
    @Query("SELECT p FROM Project p WHERE p.projectId = :projectId AND p.deleted = false")
    Optional<Project> findByIdAndNotDeleted(@Param("projectId") UUID projectId);

    // Find by name with active filter
    @Query("SELECT p FROM Project p WHERE p.name = :name AND p.deleted = false")
    Optional<Project> findByNameAndNotDeleted(@Param("name") String name);

    // Find by status
    @Query("SELECT p FROM Project p WHERE p.status = :status AND p.deleted = false")
    List<Project> findByStatusAndNotDeleted(@Param("status") Project.ProjectStatus status);

    @Query("SELECT p FROM Project p WHERE p.status = :status AND p.deleted = false")
    Page<Project> findByStatusAndNotDeleted(@Param("status") Project.ProjectStatus status, Pageable pageable);

    // Check if project name exists (excluding deleted)
    @Query("SELECT COUNT(p) > 0 FROM Project p WHERE p.name = :name AND p.deleted = false")
    boolean existsByNameAndNotDeleted(@Param("name") String name);

    // Search projects by name
    @Query("SELECT p FROM Project p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) AND p.deleted = false")
    Page<Project> searchByNameAndNotDeleted(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Count active projects
    @Query("SELECT COUNT(p) FROM Project p WHERE p.deleted = false")
    long countActive();

    // Count projects by status
    @Query("SELECT COUNT(p) FROM Project p WHERE p.status = :status AND p.deleted = false")
    long countByStatusAndNotDeleted(@Param("status") Project.ProjectStatus status);
}