CREATE TABLE payment_scheduled_notification (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(500),
    value NUMERIC(12, 2) NOT NULL,
    recurrence VARCHAR(20) NOT NULL,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP,
    next_date TIMESTAMP,
    last_date TIMESTAMP,
    last_execution_status VARCHAR(50),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    notify_whatsapp BOOLEAN NOT NULL DEFAULT FALSE,
    notify_email BOOLEAN NOT NULL DEFAULT FALSE,
    notify_sms BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP DEFAULT NULL,
    user_id BIGINT NOT NULL
);

CREATE INDEX idx_payment_scheduled_notification_user_id
    ON payment_scheduled_notification (user_id);