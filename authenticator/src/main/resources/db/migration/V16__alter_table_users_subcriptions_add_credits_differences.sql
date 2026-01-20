alter table users_subscriptions add column subscription_email_credits INTEGER DEFAULT NULL;
alter table users_subscriptions add column subscription_sms_credits INTEGER DEFAULT NULL;
alter table users_subscriptions add column subscription_whatsapp_credits INTEGER DEFAULT NULL;