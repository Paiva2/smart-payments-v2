CREATE TABLE user_subscription_credit_recurrences (
    user_subscription_id BIGSERIAL NOT NULL,
    credit_id BIGSERIAL NOT NULL,
    quantity INTEGER NOT NULL,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_user_subscription_credit_recurrences
        PRIMARY KEY (user_subscription_id, credit_id),

    CONSTRAINT fk_uscr_user_subscription
        FOREIGN KEY (user_subscription_id)
            REFERENCES users_subscriptions (id),

    CONSTRAINT fk_uscr_credit
        FOREIGN KEY (credit_id)
            REFERENCES credits (id)
);
