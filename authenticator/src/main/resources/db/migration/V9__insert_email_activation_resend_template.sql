INSERT INTO public.email_templates (name, html_body)
VALUES ('user-email-activation-resend',
        '<!doctype html>
<html lang="en">
   <head>
      <meta charset="utf-8">
      <meta name="viewport" content="width=device-width,initial-scale=1">
      <meta http-equiv="X-UA-Compatible" content="IE=edge">
      <title>Email Activation Reminder — Smart Payments</title>
      <style> html,body { margin:0; padding:0; height:100%; background-color:#f4f6f8; } img { border:0; -ms-interpolation-mode:bicubic; display:block; } a { color:inherit; text-decoration:none; }.email-wrap { width:100%; background-color:#f4f6f8; padding:24px 0; }
         .email-container { width:100%; max-width:620px; margin:0 auto; background:#ffffff; border-radius:8px; overflow:hidden; box-shadow:0 6px 18px rgba(19,24,33,0.06); }
         .header { background:linear-gradient(90deg,#0b74ff,#00c6ff); padding:24px; text-align:center; color:#ffffff; }
         .logo { font-family: ''Helvetica Neue'', Helvetica, Arial, sans-serif; font-size:20px; font-weight:700; letter-spacing:0.2px; }
         .preheader { display:none !important; visibility:hidden; opacity:0; color:transparent; height:0; width:0; }
         .content { padding:28px 32px; font-family: ''Helvetica Neue'', Helvetica, Arial, sans-serif; color:#111827; }
         .greeting { font-size:20px; font-weight:700; margin:0 0 12px 0; }
         .lead { font-size:15px; line-height:1.5; margin:0 0 18px 0; color:#334155; }
         .btn { display:inline-block; padding:12px 20px; border-radius:8px; background:#0b74ff; color:#ffffff; font-weight:600; font-size:15px; }
         .btn-secondary { background:#e6eefc; color:#0b74ff; }
         .btn:visited {color: #ffffff}
         .divider { height:1px; background:#eef2f7; margin:20px 0; }
         .footer { padding:18px 32px; font-size:13px; color:#94a3b8; }
         @media only screen and (max-width:480px) {
         .content { padding:20px; }
         .header { padding:18px; }
         .logo { font-size:18px; }
         .greeting { font-size:18px; }
         .btn { width:100%; text-align:center; display:block; }
         }
      </style>
   </head>
   <body>
      <div class="preheader">Reminder: activate your Smart Payments account to continue.</div>
      <table role="presentation" class="email-wrap" width="100%" cellpadding="0" cellspacing="0">
         <tr>
            <td align="center">
               <table role="presentation" class="email-container" width="100%" cellpadding="0" cellspacing="0">
                  <tr>
                     <td class="header">
                        <div class="logo">Smart Payments</div>
                     </td>
                  </tr>
                  <tr>
                     <td class="content">
                        <p class="greeting">Hi ${FIRST_NAME},</p>
                        <p class="lead">It looks like you haven’t confirmed your email address yet. To finish setting up your <strong>Smart Payments</strong> account and unlock all features, please complete your activation.</p>
                        <p class="lead">If you already tried earlier, no worries — you can use the button below to request activation again.</p>
                        <p style="text-align:center; margin:22px 0;">
                           <a href="${ACTIVATION_LINK}" class="btn" target="_blank" rel="noopener">Activate your email</a>
                        </p>
                        <p class="lead">If the button doesn’t work, copy and paste the following link into your browser:</p>
                        <p style="word-break:break-all; font-size:13px; color:#64748b;">${ACTIVATION_LINK}</p>
                        <p class="lead">This link will expire in ${EXPIRATION_TIME}.</p>
                        <div class="divider"></div>
                        <p style="margin-top:18px; font-size:14px; color:#475569;">If you didn''t request this email or no longer wish to activate your account, feel free to ignore this message.</p>
                     </td>
                  </tr>
                  <tr>
                     <td class="footer">
                        <p style="margin:0 0 8px 0;">Best regards,<br/><strong>The Smart Payments Team</strong></p>
                     </td>
                  </tr>
               </table>
            </td>
         </tr>
      </table>
   </body>
</html>'
       )