package com.synapse.core.service;

import com.synapse.data.entity.Project;
import com.synapse.data.repository.jpa.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for project management operations.
 */
@Service
@Transactional
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    /**
     * Create a new project.
     */
    public Project createProject(String name, String description) {
        if (projectRepository.existsByNameAndNotDeleted(name)) {
            throw new IllegalArgumentException("Project name already exists");
        }

        Project project = new Project(name, description);
        return projectRepository.save(project);
    }

    /**
     * Update project details.
     */
    public Project updateProject(UUID projectId, String name, String description) {
        Project project = findActiveProjectById(projectId);
        
        // Check if name is already taken by another project
        if (!project.getName().equals(name) && 
            projectRepository.existsByNameAndNotDeleted(name)) {
            throw new IllegalArgumentException("Project name already exists");
        }

        project.setName(name);
        project.setDescription(description);
        return projectRepository.save(project);
    }

    /**
     * Update project status.
     */
    public void updateProjectStatus(UUID projectId, Project.ProjectStatus status) {
        Project project = findActiveProjectById(projectId);
        project.setStatus(status);
        projectRepository.save(project);
    }

    /**
     * Soft delete project.
     */
    public void softDeleteProject(UUID projectId) {
        Project project = findActiveProjectById(projectId);
        project.setDeleted(true);
        projectRepository.save(project);
    }

    /**
     * Find project by ID.
     */
    public Optional<Project> findById(UUID projectId) {
        return projectRepository.findByIdAndNotDeleted(projectId);
    }

    /**
     * Find project by name.
     */
    public Optional<Project> findByName(String name) {
        return projectRepository.findByNameAndNotDeleted(name);
    }

    /**
     * Find all active projects.
     */
    public List<Project> findAllActiveProjects() {
        return projectRepository.findAllActive();
    }

    /**
     * Find active projects with pagination.
     */
    public Page<Project> findActiveProjects(Pageable pageable) {
        return projectRepository.findAllActive(pageable);
    }

    /**
     * Find projects by status.
     */
    public List<Project> findProjectsByStatus(Project.ProjectStatus status) {
        return projectRepository.findByStatusAndNotDeleted(status);
    }

    /**
     * Search projects by name.
     */
    public Page<Project> searchProjects(String searchTerm, Pageable pageable) {
        return projectRepository.searchByNameAndNotDeleted(searchTerm, pageable);
    }

    /**
     * Count active projects.
     */
    public long countActiveProjects() {
        return projectRepository.countActive();
    }

    /**
     * Count projects by status.
     */
    public long countProjectsByStatus(Project.ProjectStatus status) {
        return projectRepository.countByStatusAndNotDeleted(status);
    }

    private Project findActiveProjectById(UUID projectId) {
        return projectRepository.findByIdAndNotDeleted(projectId)
            .orElseThrow(() -> new IllegalArgumentException("Project not found"));
    }
}