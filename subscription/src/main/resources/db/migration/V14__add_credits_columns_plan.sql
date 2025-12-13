ALTER TABLE plans
    ADD COLUMN unlimited_email_credits BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN email_credits_quantity INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN unlimited_whats_app_credits BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN whats_app_credits_quantity INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN unlimited_sms_credits BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN sms_credits_quantity INTEGER NOT NULL DEFAULT 0;

UPDATE plans SET email_credits_quantity = 0,
                 unlimited_email_credits = false,
                 sms_credits_quantity = 10,
                 whats_app_credits_quantity = 10
WHERE id = ((SELECT pl.id FROM plans pl where pl.type = 'FREE'));

UPDATE plans SET email_credits_quantity = 0,
                 sms_credits_quantity = 15,
                 whats_app_credits_quantity = 15
WHERE id = ((SELECT pl.id FROM plans pl where pl.type = 'STARTER'));
