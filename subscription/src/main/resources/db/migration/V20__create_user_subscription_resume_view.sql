create or replace view vi_user_subscription_resume as
select usr.id as user_id,
       usb.value as "value",
       usb.unlimited_sms_credits as unlimited_sms_credits,
       usb.unlimited_email_credits as unlimited_email_credits,
       usb.unlimited_whatsapp_credits as unlimited_whatsApp_credits,
       usb.next_payment_date as next_payment_date,
       usb.recurrence as recurrence,
       pln.type as plan,
       coalesce(SUM(usbch.amount) FILTER (
           WHERE usbch.credit_type = 'EMAIL'
               AND (usbch.expires_at >= now() OR usbch.expires_at IS NULL)
               AND (usbch.valid_from <= now() OR usbch.valid_from IS NULL)
           ), 0) AS email_credits,
       coalesce(SUM(usbch.amount) FILTER (
           WHERE usbch.credit_type = 'SMS'
               AND (usbch.expires_at >= now() OR usbch.expires_at IS NULL)
               AND (usbch.valid_from <= now() OR usbch.valid_from IS NULL)
           ), 0) AS sms_credits,
       coalesce(SUM(usbch.amount) FILTER (
           WHERE usbch.credit_type = 'WHATS_APP'
               AND (usbch.expires_at >= now() OR usbch.expires_at IS NULL)
               AND (usbch.valid_from <= now() OR usbch.valid_from IS NULL)
           ), 0) AS whatsapp_credits
from users usr
         join users_subscriptions usb on usb.user_id = usr.id
         left join plans pln on pln.id = usb.plan_id
         left join users_subscriptions_credits_history usbch on usbch.user_subscription_id = usb.id
GROUP BY
    usr.id, usb.id, pln.type;