package com.synapse.data.repository.jpa;

import com.synapse.data.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository interface for AuditLog entity operations.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    // Find by user ID
    Page<AuditLog> findByUserIdOrderByTimestampDesc(UUID userId, Pageable pageable);

    // Find by action type
    Page<AuditLog> findByActionTypeOrderByTimestampDesc(String actionType, Pageable pageable);

    // Find by resource type
    Page<AuditLog> findByResourceTypeOrderByTimestampDesc(String resourceType, Pageable pageable);

    // Find by resource
    Page<AuditLog> findByResourceTypeAndResourceIdOrderByTimestampDesc(String resourceType, String resourceId, Pageable pageable);

    // Find by time range
    @Query("SELECT a FROM AuditLog a WHERE a.timestamp BETWEEN :startTime AND :endTime ORDER BY a.timestamp DESC")
    Page<AuditLog> findByTimestampBetweenOrderByTimestampDesc(@Param("startTime") LocalDateTime startTime, 
                                                             @Param("endTime") LocalDateTime endTime, 
                                                             Pageable pageable);

    // Find by user and time range
    @Query("SELECT a FROM AuditLog a WHERE a.userId = :userId AND a.timestamp BETWEEN :startTime AND :endTime ORDER BY a.timestamp DESC")
    Page<AuditLog> findByUserIdAndTimestampBetweenOrderByTimestampDesc(@Param("userId") UUID userId,
                                                                      @Param("startTime") LocalDateTime startTime,
                                                                      @Param("endTime") LocalDateTime endTime,
                                                                      Pageable pageable);

    // Find by action type and time range
    @Query("SELECT a FROM AuditLog a WHERE a.actionType = :actionType AND a.timestamp BETWEEN :startTime AND :endTime ORDER BY a.timestamp DESC")
    Page<AuditLog> findByActionTypeAndTimestampBetweenOrderByTimestampDesc(@Param("actionType") String actionType,
                                                                          @Param("startTime") LocalDateTime startTime,
                                                                          @Param("endTime") LocalDateTime endTime,
                                                                          Pageable pageable);

    // Search by multiple criteria
    @Query("SELECT a FROM AuditLog a WHERE " +
           "(:userId IS NULL OR a.userId = :userId) AND " +
           "(:actionType IS NULL OR a.actionType = :actionType) AND " +
           "(:resourceType IS NULL OR a.resourceType = :resourceType) AND " +
           "(:startTime IS NULL OR a.timestamp >= :startTime) AND " +
           "(:endTime IS NULL OR a.timestamp <= :endTime) " +
           "ORDER BY a.timestamp DESC")
    Page<AuditLog> findByCriteria(@Param("userId") UUID userId,
                                 @Param("actionType") String actionType,
                                 @Param("resourceType") String resourceType,
                                 @Param("startTime") LocalDateTime startTime,
                                 @Param("endTime") LocalDateTime endTime,
                                 Pageable pageable);

    // Count by action type
    long countByActionType(String actionType);

    // Count by user
    long countByUserId(UUID userId);

    // Count by time range
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.timestamp BETWEEN :startTime AND :endTime")
    long countByTimestampBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    // Find recent activities
    @Query("SELECT a FROM AuditLog a ORDER BY a.timestamp DESC")
    List<AuditLog> findRecentActivities(Pageable pageable);

    // Find suspicious activities (multiple failed logins, etc.)
    @Query("SELECT a FROM AuditLog a WHERE a.actionType = 'USER_LOGIN_FAILED' AND a.timestamp >= :since GROUP BY a.userId HAVING COUNT(a) >= :threshold")
    List<AuditLog> findSuspiciousLoginAttempts(@Param("since") LocalDateTime since, @Param("threshold") long threshold);
}