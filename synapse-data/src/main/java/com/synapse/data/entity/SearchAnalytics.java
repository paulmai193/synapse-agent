package com.synapse.data.entity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity for tracking search interactions and analytics.
 * Task 13.1: Implement search analytics and feedback collection
 */
@Entity
@Table(name = "search_analytics", indexes = {
    @Index(name = "idx_search_analytics_user_id", columnList = "user_id"),
    @Index(name = "idx_search_analytics_timestamp", columnList = "timestamp"),
    @Index(name = "idx_search_analytics_query_hash", columnList = "query_hash"),
    @Index(name = "idx_search_analytics_user_timestamp", columnList = "user_id, timestamp")
})
public class SearchAnalytics {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "query_text", nullable = false, length = 1000)
    private String queryText;

    @Column(name = "query_hash", nullable = false)
    private String queryHash;

    @Column(name = "query_language", length = 10)
    private String queryLanguage;

    @Column(name = "results_count")
    private Integer resultsCount;

    @Column(name = "response_time_ms")
    private Long responseTimeMs;

    @Column(name = "clicked_results")
    private String clickedResults; // JSON array of clicked document IDs

    @Column(name = "feedback_rating")
    private Integer feedbackRating; // 1-5 rating

    @Column(name = "feedback_helpful")
    private Boolean feedbackHelpful;

    @Column(name = "feedback_comment", length = 500)
    private String feedbackComment;

    @Column(name = "search_type", length = 20)
    private String searchType; // "cached", "fresh", "optimized"

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "session_id")
    private String sessionId;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    public SearchAnalytics() {
        this.timestamp = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getQueryText() {
        return queryText;
    }

    public void setQueryText(String queryText) {
        this.queryText = queryText;
        this.queryHash = String.valueOf(queryText.hashCode());
    }

    public String getQueryHash() {
        return queryHash;
    }

    public void setQueryHash(String queryHash) {
        this.queryHash = queryHash;
    }

    public String getQueryLanguage() {
        return queryLanguage;
    }

    public void setQueryLanguage(String queryLanguage) {
        this.queryLanguage = queryLanguage;
    }

    public Integer getResultsCount() {
        return resultsCount;
    }

    public void setResultsCount(Integer resultsCount) {
        this.resultsCount = resultsCount;
    }

    public Long getResponseTimeMs() {
        return responseTimeMs;
    }

    public void setResponseTimeMs(Long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }

    public String getClickedResults() {
        return clickedResults;
    }

    public void setClickedResults(String clickedResults) {
        this.clickedResults = clickedResults;
    }

    public Integer getFeedbackRating() {
        return feedbackRating;
    }

    public void setFeedbackRating(Integer feedbackRating) {
        this.feedbackRating = feedbackRating;
    }

    public Boolean getFeedbackHelpful() {
        return feedbackHelpful;
    }

    public void setFeedbackHelpful(Boolean feedbackHelpful) {
        this.feedbackHelpful = feedbackHelpful;
    }

    public String getFeedbackComment() {
        return feedbackComment;
    }

    public void setFeedbackComment(String feedbackComment) {
        this.feedbackComment = feedbackComment;
    }

    public String getSearchType() {
        return searchType;
    }

    public void setSearchType(String searchType) {
        this.searchType = searchType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
}