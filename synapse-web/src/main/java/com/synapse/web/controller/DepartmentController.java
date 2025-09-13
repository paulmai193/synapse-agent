package com.synapse.web.controller;

import com.synapse.core.service.DepartmentService;
import com.synapse.data.entity.Department;
import com.synapse.web.dto.DepartmentDto;
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
 * REST controller for department management operations.
 */
@RestController
@RequestMapping("/departments")
@CrossOrigin(origins = "*")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @PostMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<DepartmentDto> createDepartment(@Valid @RequestBody DepartmentDto.CreateDepartmentRequest request) {
        try {
            Department department = departmentService.createDepartment(request.getName(), request.getDescription());
            return ResponseEntity.ok(new DepartmentDto(department));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('PROJECT_ADMIN') or hasRole('DEPARTMENT_ADMIN')")
    public ResponseEntity<List<DepartmentDto>> getAllDepartments() {
        List<Department> departments = departmentService.findAllActiveDepartments();
        List<DepartmentDto> departmentDtos = departments.stream()
            .map(DepartmentDto::new)
            .collect(Collectors.toList());
        return ResponseEntity.ok(departmentDtos);
    }

    @GetMapping("/{departmentId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('PROJECT_ADMIN') or hasRole('DEPARTMENT_ADMIN')")
    public ResponseEntity<DepartmentDto> getDepartment(@PathVariable UUID departmentId) {
        return departmentService.findById(departmentId)
            .map(department -> ResponseEntity.ok(new DepartmentDto(department)))
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{departmentId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<DepartmentDto> updateDepartment(@PathVariable UUID departmentId, 
                                                        @Valid @RequestBody DepartmentDto.CreateDepartmentRequest request) {
        try {
            Department department = departmentService.updateDepartment(departmentId, request.getName(), request.getDescription());
            return ResponseEntity.ok(new DepartmentDto(department));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{departmentId}/status")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<Void> updateDepartmentStatus(@PathVariable UUID departmentId, 
                                                      @Valid @RequestBody DepartmentDto.StatusUpdateRequest request) {
        try {
            departmentService.updateDepartmentStatus(departmentId, request.getStatus());
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{departmentId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<Void> softDeleteDepartment(@PathVariable UUID departmentId) {
        try {
            departmentService.softDeleteDepartment(departmentId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('PROJECT_ADMIN') or hasRole('DEPARTMENT_ADMIN')")
    public ResponseEntity<Page<DepartmentDto>> searchDepartments(@RequestParam String searchTerm, Pageable pageable) {
        Page<Department> departments = departmentService.searchDepartments(searchTerm, pageable);
        Page<DepartmentDto> departmentDtos = new PageImpl<>(
            departments.getContent().stream()
                .map(DepartmentDto::new)
                .collect(Collectors.toList()),
            pageable,
            departments.getTotalElements()
        );
        return ResponseEntity.ok(departmentDtos);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('DEPARTMENT_ADMIN') or #userId == authentication.principal.user.userId")
    public ResponseEntity<List<DepartmentDto>> getUserDepartments(@PathVariable UUID userId) {
        List<Department> departments = departmentService.findDepartmentsByUser(userId);
        List<DepartmentDto> departmentDtos = departments.stream()
            .map(DepartmentDto::new)
            .collect(Collectors.toList());
        return ResponseEntity.ok(departmentDtos);
    }
}