package com.synapse.data.repository.jpa;

import com.synapse.data.entity.Department;
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
 * Repository interface for Department entity operations.
 */
@Repository
public interface DepartmentRepository extends JpaRepository<Department, UUID> {

    // Active departments only (not deleted)
    @Query("SELECT d FROM Department d WHERE d.deleted = false")
    List<Department> findAllActive();

    @Query("SELECT d FROM Department d WHERE d.deleted = false")
    Page<Department> findAllActive(Pageable pageable);

    // Find by ID with active filter
    @Query("SELECT d FROM Department d WHERE d.departmentId = :departmentId AND d.deleted = false")
    Optional<Department> findByIdAndNotDeleted(@Param("departmentId") UUID departmentId);

    // Find by name with active filter
    @Query("SELECT d FROM Department d WHERE d.name = :name AND d.deleted = false")
    Optional<Department> findByNameAndNotDeleted(@Param("name") String name);

    // Find by status
    @Query("SELECT d FROM Department d WHERE d.status = :status AND d.deleted = false")
    List<Department> findByStatusAndNotDeleted(@Param("status") Department.DepartmentStatus status);

    @Query("SELECT d FROM Department d WHERE d.status = :status AND d.deleted = false")
    Page<Department> findByStatusAndNotDeleted(@Param("status") Department.DepartmentStatus status, Pageable pageable);

    // Check if department name exists (excluding deleted)
    @Query("SELECT COUNT(d) > 0 FROM Department d WHERE d.name = :name AND d.deleted = false")
    boolean existsByNameAndNotDeleted(@Param("name") String name);

    // Search departments by name
    @Query("SELECT d FROM Department d WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) AND d.deleted = false")
    Page<Department> searchByNameAndNotDeleted(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Find departments by user
    @Query("SELECT d FROM Department d JOIN d.userDepartments ud WHERE ud.user.userId = :userId AND d.deleted = false")
    List<Department> findByUserIdAndNotDeleted(@Param("userId") UUID userId);

    // Count active departments
    @Query("SELECT COUNT(d) FROM Department d WHERE d.deleted = false")
    long countActive();

    // Count departments by status
    @Query("SELECT COUNT(d) FROM Department d WHERE d.status = :status AND d.deleted = false")
    long countByStatusAndNotDeleted(@Param("status") Department.DepartmentStatus status);
}