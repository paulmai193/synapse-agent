package com.synapse.data.repository.jpa;

import com.synapse.data.entity.UserDepartment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository interface for UserDepartment entity operations.
 */
@Repository
public interface UserDepartmentRepository extends JpaRepository<UserDepartment, UserDepartment.UserDepartmentId> {

    // Find by user ID
    @Query("SELECT ud FROM UserDepartment ud WHERE ud.user.userId = :userId AND ud.department.deleted = false")
    List<UserDepartment> findByUserIdAndNotDeleted(@Param("userId") UUID userId);

    // Find by department ID
    @Query("SELECT ud FROM UserDepartment ud WHERE ud.department.departmentId = :departmentId AND ud.department.deleted = false")
    List<UserDepartment> findByDepartmentIdAndNotDeleted(@Param("departmentId") UUID departmentId);

    // Find users in active departments
    @Query("SELECT ud FROM UserDepartment ud WHERE ud.department.status = 'ACTIVE' AND ud.department.deleted = false")
    List<UserDepartment> findByActiveDepartments();

    // Check if user is in department
    @Query("SELECT COUNT(ud) > 0 FROM UserDepartment ud WHERE ud.user.userId = :userId AND ud.department.departmentId = :departmentId AND ud.department.deleted = false")
    boolean existsByUserIdAndDepartmentIdAndNotDeleted(@Param("userId") UUID userId, @Param("departmentId") UUID departmentId);

    // Count users in department
    @Query("SELECT COUNT(ud) FROM UserDepartment ud WHERE ud.department.departmentId = :departmentId AND ud.department.deleted = false")
    long countByDepartmentIdAndNotDeleted(@Param("departmentId") UUID departmentId);

    // Delete by user and department
    @Query("DELETE FROM UserDepartment ud WHERE ud.user.userId = :userId AND ud.department.departmentId = :departmentId")
    void deleteByUserIdAndDepartmentId(@Param("userId") UUID userId, @Param("departmentId") UUID departmentId);
}