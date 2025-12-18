ALTER TABLE users_subscriptions
    ADD COLUMN unlimited_email_credits BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE users_subscriptions
    ADD COLUMN unlimited_whatsapp_credits BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE users_subscriptions
    ADD COLUMN unlimited_sms_credits BOOLEAN NOT NULL DEFAULT FALSE;