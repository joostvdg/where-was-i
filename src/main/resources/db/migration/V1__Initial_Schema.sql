-- Create the auth schema if it doesn't exist
CREATE SCHEMA IF NOT EXISTS wwi_auth;

-- Create the users table in the auth schema if it doesn't exist
CREATE TABLE IF NOT EXISTS wwi_auth.users (
    id SERIAL PRIMARY KEY,
    account_number VARCHAR(255) NOT NULL,
    account_type VARCHAR(255) NOT NULL,
    username VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    date_joined timestamp NOT NULL,
    date_last_login timestamp,
    CONSTRAINT unique_username_account_type UNIQUE (username, account_type)
);

-- Create an index on id
CREATE INDEX IF NOT EXISTS idx_users_id ON wwi_auth.users(id);

-- Create an index on username
CREATE INDEX IF NOT EXISTS idx_users_username ON wwi_auth.users(username);