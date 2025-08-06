CREATE TABLE IF NOT EXISTS customers (
    id SERIAL PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    age INTEGER,
    date_of_birth DATE,
    active BOOLEAN
);
INSERT INTO customers (first_name, last_name, age, date_of_birth, active) VALUES ('John', 'Doe', 30, '1990-01-01', true), ('Jane', 'Smith', 25, '1995-05-15', true);