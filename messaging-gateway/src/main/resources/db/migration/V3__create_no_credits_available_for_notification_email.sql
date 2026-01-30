INSERT INTO public.email_templates (name, html_body, active)
VALUES ('no-credits-available-for-notification',
        '<!DOCTYPE html>

<html lang="pt-BR">
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width,initial-scale=1" />
    <meta http-equiv="X-UA-Compatible" content="IE=edge" />
    <title>Créditos Esgotados — Smart Payments</title>
<style>
  html,
  body {
    margin: 0;
    padding: 0;
    height: 100%;
    background-color: #f4f6f8;
  }
  a {
    color: inherit;
    text-decoration: none;
  }

  .email-wrap {
    width: 100%;
    background-color: #f4f6f8;
    padding: 24px 0;
  }
  .email-container {
    width: 100%;
    max-width: 620px;
    margin: 0 auto;
    background: #ffffff;
    border-radius: 8px;
    overflow: hidden;
    box-shadow: 0 6px 18px rgba(19, 24, 33, 0.06);
  }
  .header {
    background: linear-gradient(90deg, #0b74ff, #00c6ff);
    padding: 24px;
    text-align: center;
    color: #ffffff;
  }
  .logo {
    font-family: "Helvetica Neue", Helvetica, Arial, sans-serif;
    font-size: 20px;
    font-weight: 700;
  }

  .content {
    padding: 28px 32px;
    font-family: "Helvetica Neue", Helvetica, Arial, sans-serif;
    color: #111827;
  }
  .greeting {
    font-size: 20px;
    font-weight: 700;
    margin: 0 0 12px 0;
  }
  .lead {
    font-size: 15px;
    line-height: 1.5;
    margin: 0 0 16px 0;
    color: #334155;
  }

  .info-box {
    background: #f8fafc;
    border: 1px solid #e5e7eb;
    border-radius: 8px;
    padding: 16px;
    margin: 20px 0;
  }
  .info-title {
    font-size: 15px;
    font-weight: 600;
    margin-bottom: 8px;
  }

  .divider {
    height: 1px;
    background: #eef2f7;
    margin: 24px 0;
  }

  .cta {
    display: inline-block;
    margin-top: 10px;
    padding: 12px 18px;
    background-color: #0b74ff;
    color: #ffffff;
    border-radius: 6px;
    font-size: 14px;
    font-weight: 600;
  }

  .footer {
    padding: 18px 32px;
    font-size: 13px;
    color: #94a3b8;
  }
</style>

  </head>

  <body>
    <div style="display:none;">
      Seus créditos de notificação no Smart Payments acabaram.
    </div>
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
            <p class="greeting">Olá ${FIRST_NAME},</p>

            <p class="lead">
              Passando para te avisar que seus
              <strong>créditos de notificação</strong> no
              <strong>Smart Payments</strong> chegaram ao fim.
            </p>

            <div class="info-box">
              <p class="info-title">
                O que isso significa?
              </p>
              <p class="lead" style="margin:0;">
                Enquanto não houver novos créditos disponíveis,
                notificações por <strong>${PAYMENT_NOTIFICATION_TYPE}</strong>
                poderão não ser enviadas.
              </p>
            </div>

            <p class="lead">
              Para continuar recebendo lembretes dos seus agendamentos
              no meio desejado, é necessário adicionar mais créditos.
            </p>

            <a href="${ADD_CREDITS_URL}" class="cta">
              Adicionar créditos
            </a>

            <div class="divider"></div>

            <p class="lead">
              Caso tenha dúvidas ou precise de ajuda, nosso suporte
              está à disposição.
            </p>
          </td>
        </tr>

        <tr>
          <td class="footer">
            <p style="margin:0 0 8px 0;">
              Atenciosamente,<br />
              <strong>Time Smart Payments</strong>
            </p>
          </td>
        </tr>
      </table>
    </td>
  </tr>
</table>
  </body>
</html>
',true)