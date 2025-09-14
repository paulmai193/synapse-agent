package com.synapse.data.repository.jpa;

import com.synapse.data.entity.SearchAnalytics;
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
 * Repository for SearchAnalytics entity.
 * Task 13.1: Implement search analytics and feedback collection
 */
@Repository
public interface SearchAnalyticsRepository extends JpaRepository<SearchAnalytics, UUID> {

    /**
     * Find search analytics by user ID with pagination.
     */
    Page<SearchAnalytics> findByUserIdOrderByTimestampDesc(UUID userId, Pageable pageable);

    /**
     * Find search analytics within date range.
     */
    @Query("SELECT sa FROM SearchAnalytics sa WHERE sa.timestamp BETWEEN :startDate AND :endDate ORDER BY sa.timestamp DESC")
    Page<SearchAnalytics> findByTimestampBetween(@Param("startDate") LocalDateTime startDate, 
                                                @Param("endDate") LocalDateTime endDate, 
                                                Pageable pageable);

    /**
     * Find most popular queries in the last N days.
     */
    @Query("SELECT sa.queryText, COUNT(sa) as queryCount FROM SearchAnalytics sa " +
           "WHERE sa.timestamp >= :since " +
           "GROUP BY sa.queryText " +
           "ORDER BY queryCount DESC")
    List<Object[]> findPopularQueries(@Param("since") LocalDateTime since, Pageable pageable);

    /**
     * Calculate average response time for searches.
     */
    @Query("SELECT AVG(sa.responseTimeMs) FROM SearchAnalytics sa WHERE sa.timestamp >= :since")
    Double getAverageResponseTime(@Param("since") LocalDateTime since);

    /**
     * Get search analytics with feedback.
     */
    @Query("SELECT sa FROM SearchAnalytics sa WHERE sa.feedbackRating IS NOT NULL OR sa.feedbackHelpful IS NOT NULL")
    Page<SearchAnalytics> findSearchesWithFeedback(Pageable pageable);

    /**
     * Count searches by user in date range.
     */
    @Query("SELECT COUNT(sa) FROM SearchAnalytics sa WHERE sa.userId = :userId AND sa.timestamp BETWEEN :startDate AND :endDate")
    Long countByUserIdAndTimestampBetween(@Param("userId") UUID userId, 
                                         @Param("startDate") LocalDateTime startDate, 
                                         @Param("endDate") LocalDateTime endDate);

    /**
     * Get click-through rate statistics.
     */
    @Query("SELECT " +
           "COUNT(sa) as totalSearches, " +
           "COUNT(CASE WHEN sa.clickedResults IS NOT NULL AND sa.clickedResults != '[]' THEN 1 END) as searchesWithClicks " +
           "FROM SearchAnalytics sa WHERE sa.timestamp >= :since")
    Object[] getClickThroughStats(@Param("since") LocalDateTime since);

    /**
     * Find slow searches (above threshold).
     */
    @Query("SELECT sa FROM SearchAnalytics sa WHERE sa.responseTimeMs > :threshold AND sa.timestamp >= :since ORDER BY sa.responseTimeMs DESC")
    List<SearchAnalytics> findSlowSearches(@Param("threshold") Long threshold, @Param("since") LocalDateTime since);

    /**
     * Get search performance metrics by search type.
     */
    @Query("SELECT sa.searchType, AVG(sa.responseTimeMs), COUNT(sa) FROM SearchAnalytics sa " +
           "WHERE sa.timestamp >= :since " +
           "GROUP BY sa.searchType")
    List<Object[]> getPerformanceBySearchType(@Param("since") LocalDateTime since);

    /**
     * Find searches with low user satisfaction (poor ratings).
     */
    @Query("SELECT sa FROM SearchAnalytics sa WHERE sa.feedbackRating <= 2 OR sa.feedbackHelpful = false")
    List<SearchAnalytics> findLowSatisfactionSearches();
}