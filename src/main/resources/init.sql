CREATE DATABASE expense-control;

\c expense-control

-- Create a new application user
CREATE USER app_user WITH PASSWORD 'app_password';

GRANT CONNECT ON DATABASE expense-control TO app_user;

GRANT USAGE ON SCHEMA public TO app_user;
GRANT CREATE ON SCHEMA public TO app_user;

GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO app_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO app_user;
GRANT ALL PRIVILEGES ON ALL FUNCTIONS IN SCHEMA public TO app_user;
