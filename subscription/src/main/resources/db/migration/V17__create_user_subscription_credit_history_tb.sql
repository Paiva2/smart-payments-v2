CREATE TABLE users_subscriptions_credits_history (
    id BIGSERIAL PRIMARY KEY,
    amount INTEGER NOT NULL DEFAULT 0,
    credit_type VARCHAR(50) NOT NULL,
    transaction_type VARCHAR(50) NOT NULL,
    source_usage VARCHAR(255) DEFAULT NULL,
    source_usage_id VARCHAR(255) DEFAULT NULL,
    valid_from TIMESTAMP DEFAULT NULL,
    expires_at TIMESTAMP DEFAULT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_subscription_id BIGSERIAL NOT NULL,

    CONSTRAINT fk_users_subscriptions_credits_history_user_subscription
     FOREIGN KEY (user_subscription_id)
         REFERENCES users_subscriptions (id)
);
