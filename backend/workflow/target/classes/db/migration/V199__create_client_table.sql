CREATE TABLE IF NOT EXISTS CLIENT (
                                      client_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                      name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NULL,
    phone VARCHAR(50) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );