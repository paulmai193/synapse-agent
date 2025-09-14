-- Search Analytics schema for tracking search interactions and feedback
-- Task 13.1: Implement search analytics and feedback collection

-- Search Analytics table
CREATE TABLE search_analytics (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    query_text VARCHAR(1000) NOT NULL,
    query_hash VARCHAR(255) NOT NULL,
    query_language VARCHAR(10),
    results_count INTEGER,
    response_time_ms BIGINT,
    clicked_results TEXT, -- JSON array of clicked document IDs
    feedback_rating INTEGER CHECK (feedback_rating >= 1 AND feedback_rating <= 5),
    feedback_helpful BOOLEAN,
    feedback_comment VARCHAR(500),
    search_type VARCHAR(20), -- 'cached', 'fresh', 'optimized'
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    session_id VARCHAR(255),
    ip_address INET,
    user_agent VARCHAR(500)
);

-- Indexes for search analytics performance
CREATE INDEX idx_search_analytics_user_id ON search_analytics(user_id);
CREATE INDEX idx_search_analytics_timestamp ON search_analytics(timestamp);
CREATE INDEX idx_search_analytics_query_hash ON search_analytics(query_hash);
CREATE INDEX idx_search_analytics_user_timestamp ON search_analytics(user_id, timestamp);
CREATE INDEX idx_search_analytics_feedback ON search_analytics(feedback_rating, feedback_helpful) WHERE feedback_rating IS NOT NULL OR feedback_helpful IS NOT NULL;
CREATE INDEX idx_search_analytics_response_time ON search_analytics(response_time_ms) WHERE response_time_ms IS NOT NULL;
CREATE INDEX idx_search_analytics_search_type ON search_analytics(search_type);

-- Partial index for searches with clicks (for CTR analysis)
CREATE INDEX idx_search_analytics_with_clicks ON search_analytics(timestamp) WHERE clicked_results IS NOT NULL AND clicked_results != '[]';

-- Composite index for popular queries analysis
CREATE INDEX idx_search_analytics_query_time ON search_analytics(query_hash, timestamp);

-- Comments for documentation
COMMENT ON TABLE search_analytics IS 'Tracks search interactions, performance metrics, and user feedback for analytics';
COMMENT ON COLUMN search_analytics.query_hash IS 'Hash of query text for efficient grouping and analysis';
COMMENT ON COLUMN search_analytics.clicked_results IS 'JSON array of document IDs that were clicked from search results';
COMMENT ON COLUMN search_analytics.search_type IS 'Type of search: cached (fast), fresh (new), or optimized';
COMMENT ON COLUMN search_analytics.response_time_ms IS 'Search response time in milliseconds for performance tracking';

-- Function to update search analytics statistics (can be called periodically)
CREATE OR REPLACE FUNCTION update_search_analytics_stats()
RETURNS TABLE(
    total_searches BIGINT,
    avg_response_time NUMERIC,
    cache_hit_rate NUMERIC,
    avg_rating NUMERIC
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        COUNT(*) as total_searches,
        AVG(sa.response_time_ms) as avg_response_time,
        (COUNT(CASE WHEN sa.search_type = 'cached' THEN 1 END) * 100.0 / COUNT(*)) as cache_hit_rate,
        AVG(sa.feedback_rating) as avg_rating
    FROM search_analytics sa
    WHERE sa.timestamp >= CURRENT_TIMESTAMP - INTERVAL '30 days';
END;
$$ LANGUAGE plpgsql;

-- View for search quality metrics
CREATE VIEW search_quality_metrics AS
SELECT 
    DATE_TRUNC('day', timestamp) as date,
    COUNT(*) as total_searches,
    AVG(response_time_ms) as avg_response_time,
    COUNT(CASE WHEN clicked_results IS NOT NULL AND clicked_results != '[]' THEN 1 END) * 100.0 / COUNT(*) as click_through_rate,
    AVG(feedback_rating) as avg_rating,
    COUNT(CASE WHEN feedback_helpful = true THEN 1 END) * 100.0 / NULLIF(COUNT(CASE WHEN feedback_helpful IS NOT NULL THEN 1 END), 0) as helpful_percentage
FROM search_analytics
WHERE timestamp >= CURRENT_TIMESTAMP - INTERVAL '90 days'
GROUP BY DATE_TRUNC('day', timestamp)
ORDER BY date DESC;

-- View for popular queries
CREATE VIEW popular_queries AS
SELECT 
    query_text,
    COUNT(*) as search_count,
    AVG(response_time_ms) as avg_response_time,
    AVG(results_count) as avg_results_count,
    AVG(feedback_rating) as avg_rating
FROM search_analytics
WHERE timestamp >= CURRENT_TIMESTAMP - INTERVAL '30 days'
GROUP BY query_text
HAVING COUNT(*) >= 2
ORDER BY search_count DESC, avg_rating DESC NULLS LAST
LIMIT 50;

COMMENT ON VIEW search_quality_metrics IS 'Daily search quality metrics for monitoring and improvement';
COMMENT ON VIEW popular_queries IS 'Most popular search queries with performance and satisfaction metrics';