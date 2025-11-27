CREATE TABLE addresses (
   id BIGSERIAL NOT NULL PRIMARY KEY,
   street VARCHAR(50) NOT NULL,
   neighborhood VARCHAR(50) NOT NULL,
   number VARCHAR(15) NOT NULL,
   zipcode VARCHAR(50) NOT NULL,
   complement VARCHAR(300),
   city VARCHAR(50) NOT NULL,
   state VARCHAR(50) NOT NULL,
   country VARCHAR(10) NOT NULL,
   user_id BIGSERIAL,

   CONSTRAINT fk_addresses_user
       FOREIGN KEY (user_id) REFERENCES users (id)
);
