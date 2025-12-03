CREATE TABLE purchase_charges (
    id BIGSERIAL PRIMARY KEY,
    external_id VARCHAR(200) UNIQUE NOT NULL,
    total_value NUMERIC(12, 2) NOT NULL DEFAULT 0,
    status VARCHAR(60) NOT NULL,
    payment_date TIMESTAMP DEFAULT NULL,
    due_date TIMESTAMP DEFAULT NULL,
    purchase_id BIGINT NOT NULL,
    payment_url VARCHAR(200) NOT NULL,

    CONSTRAINT fk_purchase_charge_purchase
      FOREIGN KEY (purchase_id) REFERENCES purchases(id)
);
