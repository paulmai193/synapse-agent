-- Performance optimization indexes for Synapse AI Agent
-- Task 12.3: Optimize search and Q&A response times

-- Additional indexes for frequently queried fields
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_users_status_deleted ON users(status, deleted);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_projects_status_deleted ON projects(status, deleted);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_departments_status_deleted ON departments(status, deleted);

-- Composite indexes for user access control queries
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_user_projects_composite ON user_projects(user_id, project_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_user_departments_composite ON user_departments(user_id, department_id);

-- Audit log performance indexes
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_audit_logs_user_timestamp ON audit_logs(user_id, timestamp DESC);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_audit_logs_action_timestamp ON audit_logs(action_type, timestamp DESC);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_audit_logs_resource ON audit_logs(resource_type, resource_id);

-- Partial indexes for active records only (better performance)
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_users_active_only ON users(user_id) WHERE status = 'ACTIVE' AND deleted = FALSE;
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_projects_active_only ON projects(project_id) WHERE status = 'ACTIVE' AND deleted = FALSE;
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_departments_active_only ON departments(department_id) WHERE status = 'ACTIVE' AND deleted = FALSE;

-- JSONB indexes for role permissions (GIN indexes for better JSON query performance)
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_roles_permissions_gin ON roles USING GIN (permissions);

-- Audit log details JSONB index
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_audit_logs_details_gin ON audit_logs USING GIN (details);

-- Covering indexes for common query patterns
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_users_lookup_covering ON users(user_id, username, email, status) WHERE deleted = FALSE;
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_projects_lookup_covering ON projects(project_id, name, status) WHERE deleted = FALSE;
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_departments_lookup_covering ON departments(department_id, name, status) WHERE deleted = FALSE;

-- Time-based partitioning preparation for audit_logs (for future scaling)
-- This creates an index that will help with time-based queries and future partitioning
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_audit_logs_timestamp_hash ON audit_logs(date_trunc('day', timestamp), user_id);

-- Statistics update for query planner optimization
ANALYZE users;
ANALYZE projects;
ANALYZE departments;
ANALYZE user_projects;
ANALYZE user_departments;
ANALYZE audit_logs;
ANALYZE roles;

-- Create materialized view for user access summary (for caching frequently accessed data)
CREATE MATERIALIZED VIEW IF NOT EXISTS user_access_summary AS
SELECT 
    u.user_id,
    u.username,
    u.email,
    u.status as user_status,
    up.project_id,
    p.name as project_name,
    p.status as project_status,
    array_agg(DISTINCT ud.department_id) as department_ids,
    array_agg(DISTINCT d.name) as department_names,
    array_agg(DISTINCT ur.role_id) as role_ids,
    array_agg(DISTINCT r.name) as role_names
FROM users u
LEFT JOIN user_projects up ON u.user_id = up.user_id
LEFT JOIN projects p ON up.project_id = p.project_id AND p.deleted = FALSE
LEFT JOIN user_departments ud ON u.user_id = ud.user_id
LEFT JOIN departments d ON ud.department_id = d.department_id AND d.deleted = FALSE
LEFT JOIN user_roles ur ON u.user_id = ur.user_id
LEFT JOIN roles r ON ur.role_id = r.role_id
WHERE u.deleted = FALSE
GROUP BY u.user_id, u.username, u.email, u.status, up.project_id, p.name, p.status;

-- Index on materialized view for fast lookups
CREATE UNIQUE INDEX IF NOT EXISTS idx_user_access_summary_user_id ON user_access_summary(user_id);
CREATE INDEX IF NOT EXISTS idx_user_access_summary_project_id ON user_access_summary(project_id);

-- Function to refresh materialized view (can be called periodically)
CREATE OR REPLACE FUNCTION refresh_user_access_summary()
RETURNS void AS $$
BEGIN
    REFRESH MATERIALIZED VIEW CONCURRENTLY user_access_summary;
END;
$$ LANGUAGE plpgsql;

-- Comments for documentation
COMMENT ON INDEX idx_users_status_deleted IS 'Composite index for filtering active users';
COMMENT ON INDEX idx_user_projects_composite IS 'Composite index for user-project access control queries';
COMMENT ON INDEX idx_user_departments_composite IS 'Composite index for user-department access control queries';
COMMENT ON INDEX idx_audit_logs_user_timestamp IS 'Composite index for user audit history queries';
COMMENT ON INDEX idx_roles_permissions_gin IS 'GIN index for JSON permission queries';
COMMENT ON MATERIALIZED VIEW user_access_summary IS 'Materialized view for caching user access control data';