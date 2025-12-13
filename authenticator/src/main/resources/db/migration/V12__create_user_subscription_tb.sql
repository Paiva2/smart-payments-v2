CREATE TABLE users_subscriptions (
    id BIGSERIAL PRIMARY KEY,
    value NUMERIC(12, 2) NOT NULL DEFAULT 0,
    next_payment_date TIMESTAMP DEFAULT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    recurrence VARCHAR(50) DEFAULT NULL,
    plan VARCHAR(50) NULL,
    unlimited_email_credits BOOLEAN DEFAULT NULL,
    email_credits INTEGER DEFAULT NULL,
    unlimited_whatsapp_credits BOOLEAN DEFAULT NULL,
    whatsapp_credits INTEGER DEFAULT NULL,
    unlimited_sms_credits BOOLEAN DEFAULT NULL,
    sms_credits INTEGER DEFAULT NULL,
    user_id BIGINT NOT NULL,

    CONSTRAINT fk_users_subscriptions_user
        FOREIGN KEY (user_id)
    REFERENCES users (id)
);
