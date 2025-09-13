package com.synapse.web.controller;

import com.synapse.core.service.ProjectService;
import com.synapse.data.entity.Project;
import com.synapse.web.dto.ProjectDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for project management operations.
 */
@RestController
@RequestMapping("/projects")
@CrossOrigin(origins = "*")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @PostMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<ProjectDto> createProject(@Valid @RequestBody ProjectDto.CreateProjectRequest request) {
        try {
            Project project = projectService.createProject(request.getName(), request.getDescription());
            return ResponseEntity.ok(new ProjectDto(project));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('PROJECT_ADMIN') or hasRole('DEPARTMENT_ADMIN')")
    public ResponseEntity<List<ProjectDto>> getAllProjects() {
        List<Project> projects = projectService.findAllActiveProjects();
        List<ProjectDto> projectDtos = projects.stream()
            .map(ProjectDto::new)
            .collect(Collectors.toList());
        return ResponseEntity.ok(projectDtos);
    }

    @GetMapping("/{projectId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('PROJECT_ADMIN') or hasRole('DEPARTMENT_ADMIN')")
    public ResponseEntity<ProjectDto> getProject(@PathVariable UUID projectId) {
        return projectService.findById(projectId)
            .map(project -> ResponseEntity.ok(new ProjectDto(project)))
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{projectId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<ProjectDto> updateProject(@PathVariable UUID projectId, 
                                                  @Valid @RequestBody ProjectDto.CreateProjectRequest request) {
        try {
            Project project = projectService.updateProject(projectId, request.getName(), request.getDescription());
            return ResponseEntity.ok(new ProjectDto(project));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{projectId}/status")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<Void> updateProjectStatus(@PathVariable UUID projectId, 
                                                   @Valid @RequestBody ProjectDto.StatusUpdateRequest request) {
        try {
            projectService.updateProjectStatus(projectId, request.getStatus());
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{projectId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<Void> softDeleteProject(@PathVariable UUID projectId) {
        try {
            projectService.softDeleteProject(projectId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('PROJECT_ADMIN') or hasRole('DEPARTMENT_ADMIN')")
    public ResponseEntity<Page<ProjectDto>> searchProjects(@RequestParam String searchTerm, Pageable pageable) {
        Page<Project> projects = projectService.searchProjects(searchTerm, pageable);
        Page<ProjectDto> projectDtos = new PageImpl<>(
            projects.getContent().stream()
                .map(ProjectDto::new)
                .collect(Collectors.toList()),
            pageable,
            projects.getTotalElements()
        );
        return ResponseEntity.ok(projectDtos);
    }
}