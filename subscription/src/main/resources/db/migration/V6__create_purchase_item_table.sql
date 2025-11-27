CREATE TABLE purchase_items (
    id BIGSERIAL PRIMARY KEY,
    description VARCHAR(300),
    quantity INTEGER NOT NULL,
    value NUMERIC(12, 2) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT NULL,
    purchase_id BIGSERIAL NOT NULL,
    credit_id BIGSERIAL,
    plan_id BIGSERIAL,

    CONSTRAINT fk_purchase_items_purchase
        FOREIGN KEY (purchase_id) REFERENCES purchases (id),

    CONSTRAINT fk_purchase_items_credit
        FOREIGN KEY (credit_id) REFERENCES credits (id),

    CONSTRAINT fk_purchase_items_plan
        FOREIGN KEY (plan_id) REFERENCES plans (id)
);
