
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    account_number VARCHAR(255) NOT NULL,
    account_type VARCHAR(255) NOT NULL,
    username VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    date_joined timestamp NOT NULL,
    date_last_login timestamp
);
