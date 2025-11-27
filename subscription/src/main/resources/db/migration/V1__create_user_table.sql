CREATE TABLE users (
    id BIGSERIAL NOT NULL PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(50) NOT NULL UNIQUE,
    cpf_cnpj VARCHAR(20) NOT NULL UNIQUE,
    type VARCHAR(15) NOT NULL,
    ddi VARCHAR(5) NOT NULL,
    phone VARCHAR(30),
    birthdate DATE NOT NULL,
    active BOOLEAN NOT NULL
);
