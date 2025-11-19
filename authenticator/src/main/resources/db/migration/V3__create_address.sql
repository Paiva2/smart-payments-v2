CREATE TABLE addresses (
    id BIGSERIAL PRIMARY KEY,
    street VARCHAR(50) NOT NULL,
    neighborhood VARCHAR(50) NOT NULL,
    number VARCHAR(15) NOT NULL,
    zipcode VARCHAR(50) NOT NULL,
    complement VARCHAR(300) DEFAULT NULL,
    city VARCHAR(50) NOT NULL,
    state VARCHAR(50) NOT NULL,
    country VARCHAR(10) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT NULL,
    user_id BIGINT UNIQUE,

    CONSTRAINT fk_address_user FOREIGN KEY (user_id)
       REFERENCES public.users (id)
       ON DELETE CASCADE
);