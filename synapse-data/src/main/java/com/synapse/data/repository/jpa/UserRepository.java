package com.synapse.data.repository.jpa;

import com.synapse.data.entity.User;
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
 * Repository interface for User entity operations.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // Active users only (not deleted)
    @Query("SELECT u FROM User u WHERE u.deleted = false")
    List<User> findAllActive();

    @Query("SELECT u FROM User u WHERE u.deleted = false")
    Page<User> findAllActive(Pageable pageable);

    // Find by username/email with active filter
    @Query("SELECT u FROM User u WHERE u.username = :username AND u.deleted = false")
    Optional<User> findByUsernameAndNotDeleted(@Param("username") String username);

    @Query("SELECT u FROM User u WHERE u.email = :email AND u.deleted = false")
    Optional<User> findByEmailAndNotDeleted(@Param("email") String email);

    // Find by status
    @Query("SELECT u FROM User u WHERE u.status = :status AND u.deleted = false")
    List<User> findByStatusAndNotDeleted(@Param("status") User.UserStatus status);

    @Query("SELECT u FROM User u WHERE u.status = :status AND u.deleted = false")
    Page<User> findByStatusAndNotDeleted(@Param("status") User.UserStatus status, Pageable pageable);

    // Find users by role
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName AND u.deleted = false")
    List<User> findByRoleNameAndNotDeleted(@Param("roleName") String roleName);

    // Find users by project
    @Query("SELECT u FROM User u JOIN u.userProject up WHERE up.project.projectId = :projectId AND u.deleted = false")
    List<User> findByProjectIdAndNotDeleted(@Param("projectId") UUID projectId);

    // Find users by department
    @Query("SELECT u FROM User u JOIN u.userDepartments ud WHERE ud.department.departmentId = :departmentId AND u.deleted = false")
    List<User> findByDepartmentIdAndNotDeleted(@Param("departmentId") UUID departmentId);

    // Check if username/email exists (excluding deleted)
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.username = :username AND u.deleted = false")
    boolean existsByUsernameAndNotDeleted(@Param("username") String username);

    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email AND u.deleted = false")
    boolean existsByEmailAndNotDeleted(@Param("email") String email);

    // Search users by username or email
    @Query("SELECT u FROM User u WHERE (LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND u.deleted = false")
    Page<User> searchByUsernameOrEmailAndNotDeleted(@Param("searchTerm") String searchTerm, Pageable pageable);
}