CREATE TABLE purchases (
   id BIGSERIAL PRIMARY KEY NOT NULL,
   payment_method VARCHAR(20) NOT NULL,
   total_value NUMERIC(12, 2) NOT NULL DEFAULT 0,
   status VARCHAR(30) NOT NULL,
   installments INTEGER NOT NULL,
   created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
   updated_at TIMESTAMP DEFAULT NULL,
   user_id BIGSERIAL NOT NULL,

   CONSTRAINT fk_purchases_user
       FOREIGN KEY (user_id) REFERENCES users (id)
);
