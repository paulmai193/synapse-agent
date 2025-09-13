package com.synapse.core.service;

import com.synapse.data.entity.Department;
import com.synapse.data.repository.jpa.DepartmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for department management operations.
 */
@Service
@Transactional
public class DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    /**
     * Create a new department.
     */
    public Department createDepartment(String name, String description) {
        if (departmentRepository.existsByNameAndNotDeleted(name)) {
            throw new IllegalArgumentException("Department name already exists");
        }

        Department department = new Department(name, description);
        return departmentRepository.save(department);
    }

    /**
     * Update department details.
     */
    public Department updateDepartment(UUID departmentId, String name, String description) {
        Department department = findActiveDepartmentById(departmentId);
        
        // Check if name is already taken by another department
        if (!department.getName().equals(name) && 
            departmentRepository.existsByNameAndNotDeleted(name)) {
            throw new IllegalArgumentException("Department name already exists");
        }

        department.setName(name);
        department.setDescription(description);
        return departmentRepository.save(department);
    }

    /**
     * Update department status.
     */
    public void updateDepartmentStatus(UUID departmentId, Department.DepartmentStatus status) {
        Department department = findActiveDepartmentById(departmentId);
        department.setStatus(status);
        departmentRepository.save(department);
    }

    /**
     * Soft delete department.
     */
    public void softDeleteDepartment(UUID departmentId) {
        Department department = findActiveDepartmentById(departmentId);
        department.setDeleted(true);
        departmentRepository.save(department);
    }

    /**
     * Find department by ID.
     */
    public Optional<Department> findById(UUID departmentId) {
        return departmentRepository.findByIdAndNotDeleted(departmentId);
    }

    /**
     * Find department by name.
     */
    public Optional<Department> findByName(String name) {
        return departmentRepository.findByNameAndNotDeleted(name);
    }

    /**
     * Find all active departments.
     */
    public List<Department> findAllActiveDepartments() {
        return departmentRepository.findAllActive();
    }

    /**
     * Find active departments with pagination.
     */
    public Page<Department> findActiveDepartments(Pageable pageable) {
        return departmentRepository.findAllActive(pageable);
    }

    /**
     * Find departments by status.
     */
    public List<Department> findDepartmentsByStatus(Department.DepartmentStatus status) {
        return departmentRepository.findByStatusAndNotDeleted(status);
    }

    /**
     * Find departments by user.
     */
    public List<Department> findDepartmentsByUser(UUID userId) {
        return departmentRepository.findByUserIdAndNotDeleted(userId);
    }

    /**
     * Search departments by name.
     */
    public Page<Department> searchDepartments(String searchTerm, Pageable pageable) {
        return departmentRepository.searchByNameAndNotDeleted(searchTerm, pageable);
    }

    /**
     * Count active departments.
     */
    public long countActiveDepartments() {
        return departmentRepository.countActive();
    }

    /**
     * Count departments by status.
     */
    public long countDepartmentsByStatus(Department.DepartmentStatus status) {
        return departmentRepository.countByStatusAndNotDeleted(status);
    }

    private Department findActiveDepartmentById(UUID departmentId) {
        return departmentRepository.findByIdAndNotDeleted(departmentId)
            .orElseThrow(() -> new IllegalArgumentException("Department not found"));
    }
}