package com.synapse.data.repository.jpa;

import com.synapse.data.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Role entity operations.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    // Find role by name
    Optional<Role> findByName(String name);

    // Find roles by names
    @Query("SELECT r FROM Role r WHERE r.name IN :names")
    List<Role> findByNameIn(@Param("names") List<String> names);

    // Check if role exists by name
    boolean existsByName(String name);

    // Find default user role
    @Query("SELECT r FROM Role r WHERE r.name = 'USER'")
    Optional<Role> findDefaultUserRole();

    // Find admin roles
    @Query("SELECT r FROM Role r WHERE r.name IN ('SYSTEM_ADMIN', 'PROJECT_ADMIN', 'DEPARTMENT_ADMIN')")
    List<Role> findAdminRoles();
}