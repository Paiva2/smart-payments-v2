CREATE TABLE users_subscriptions (
    id BIGSERIAL PRIMARY KEY,
    external_subscription_id VARCHAR(255) UNIQUE,
    value NUMERIC(12, 2) NOT NULL DEFAULT 0,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    next_payment_date TIMESTAMP DEFAULT NULL,
    recurrence VARCHAR(50) DEFAULT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT NULL,
    user_id BIGINT NOT NULL,
    plan_id BIGINT NOT NULL,

    CONSTRAINT fk_users_subscriptions_user
        FOREIGN KEY (user_id)
         REFERENCES users (id),

    CONSTRAINT fk_users_subscriptions_plan
        FOREIGN KEY (plan_id)
         REFERENCES plans (id)
);
