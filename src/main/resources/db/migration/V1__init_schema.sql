CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    roles VARCHAR(100) NOT NULL
);

CREATE TABLE photos (
    id SERIAL PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    height INTEGER NOT NULL,
    material VARCHAR(100),
    color VARCHAR(100),
    type VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
