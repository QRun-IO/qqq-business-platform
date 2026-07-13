-- QQQ Business Platform PostgreSQL Initialization
-- Creates required users and databases for the platform

-- Create users with proper permissions
CREATE USER platform_ddl WITH PASSWORD 'platform_ddl_password';
CREATE USER platform_app WITH PASSWORD 'platform_app_password';

-- Grant database creation to DDL user (for migrations)
ALTER USER platform_ddl CREATEDB;
ALTER USER platform_ddl SUPERUSER; -- Needed for schema operations

-- Grant connect and usage permissions
GRANT CONNECT ON DATABASE qqq_platform TO platform_ddl;
GRANT CONNECT ON DATABASE qqq_platform TO platform_app;

-- Switch to the platform database
\c qqq_platform;

-- Create Keycloak schema for OIDC provider
CREATE SCHEMA IF NOT EXISTS keycloak;
GRANT ALL ON SCHEMA keycloak TO platform_ddl;
GRANT USAGE ON SCHEMA keycloak TO platform_app;

-- Grant schema permissions to DDL user (for Liquibase migrations)
GRANT ALL ON SCHEMA public TO platform_ddl;
GRANT USAGE ON SCHEMA public TO platform_app;
GRANT CREATE ON SCHEMA public TO platform_ddl;

-- Make platform_ddl owner of the database for full access
ALTER DATABASE qqq_platform OWNER TO platform_ddl;

-- Grant table permissions (to be applied to future tables)
ALTER DEFAULT PRIVILEGES FOR USER platform_ddl IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO platform_app;
ALTER DEFAULT PRIVILEGES FOR USER platform_ddl IN SCHEMA public GRANT USAGE, SELECT ON SEQUENCES TO platform_app;

-- Performance tuning for development
ALTER SYSTEM SET shared_buffers = '256MB';
ALTER SYSTEM SET effective_cache_size = '1GB';
ALTER SYSTEM SET maintenance_work_mem = '64MB';
ALTER SYSTEM SET checkpoint_completion_target = 0.7;
ALTER SYSTEM SET wal_buffers = '16MB';
ALTER SYSTEM SET default_statistics_target = 100;
ALTER SYSTEM SET random_page_cost = 1.1;
ALTER SYSTEM SET effective_io_concurrency = 200;

-- Logging for development
ALTER SYSTEM SET log_min_duration_statement = '1000ms';
ALTER SYSTEM SET log_statement = 'mod';

SELECT pg_reload_conf();

-- Show user information
\du