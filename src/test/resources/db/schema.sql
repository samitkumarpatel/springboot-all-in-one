CREATE TABLE IF NOT EXISTS customers (
       id SERIAL PRIMARY KEY,
       first_name VARCHAR(255) NOT NULL,
       last_name VARCHAR(255) NOT NULL,
       age INTEGER,
       date_of_birth DATE,
       active BOOLEAN
);