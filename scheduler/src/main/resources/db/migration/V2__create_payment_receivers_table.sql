CREATE TABLE payment_receivers (
   id BIGSERIAL PRIMARY KEY,
   identification VARCHAR(255) NOT NULL,
   payment_scheduled_notification_id BIGSERIAL NOT NULL REFERENCES payment_scheduled_notification(id)
);