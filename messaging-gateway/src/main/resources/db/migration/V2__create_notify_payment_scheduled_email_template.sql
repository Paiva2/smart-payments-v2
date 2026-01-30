INSERT INTO public.email_templates (name, html_body, active)
VALUES ('notify-payment-scheduled-notification',
        '<!DOCTYPE html>
<html lang="pt-BR">
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width,initial-scale=1" />
    <meta http-equiv="X-UA-Compatible" content="IE=edge" />
    <title>Pagamento Agendado — Smart Payments</title>
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
    margin-bottom: 10px;
  }
  .info-list {
    margin: 0;
    padding-left: 18px;
    font-size: 14px;
    color: #334155;
  }
  .info-list li {
    margin-bottom: 6px;
  }

  .divider {
    height: 1px;
    background: #eef2f7;
    margin: 24px 0;
  }

  .cta-cancel {
    display: inline-block;
    margin-top: 8px;
    padding: 10px 16px;
    border: 1px solid #e11d48;
    color: #e11d48;
    border-radius: 6px;
    font-size: 14px;
    font-weight: 600;
  }

  .footer {
    padding: 18px 32px;
    font-size: 13px;
    color: #94a3b8;
  }

  @media only screen and (max-width: 480px) {
    .content {
      padding: 20px;
    }
    .header {
      padding: 18px;
    }
    .greeting {
      font-size: 18px;
    }
  }
</style>
  </head>

  <body>
    <div style="display:none;">
      Você tem um pagamento agendado que precisa ser realizado.
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
              Você está recebendo este e-mail como lembrete de um pagamento que
              foi agendado por você no <strong>Smart Payments</strong>.
            </p>

            <div class="info-box">
              <p class="info-title">Detalhes do pagamento</p>
              <ul class="info-list">
                <li><strong>Nome:</strong> ${PAYMENT_NOTIFICATION_TITLE}</li>
                <li><strong>Descrição:</strong> ${PAYMENT_NOTIFICATION_DESCRIPTION}</li>
                <li><strong>Valor:</strong> ${PAYMENT_NOTIFICATION_VALUE}</li>
                <li><strong>Próxima notificação:</strong> ${PAYMENT_NOTIFICATION_NEXT_NOTIFICATION_DATE}</li>
                <li><strong>Recebedores:</strong>
                  <ul>${PAYMENT_NOTIFICATION_RECEIVERS_LIST}</ul>
                </li>
              </ul>
            </div>

            <p class="lead">
              Para garantir que o pagamento seja processado corretamente,
              lembre-se de realizá-lo até a data informada.
            </p>

            <div class="divider"></div>

            <p class="lead">
              Caso não deseje mais receber notificações sobre este pagamento,
              você pode cancelá-las a qualquer momento na sua conta do <strong>Smart Payments</strong>.
            </p>

            <p class="lead">
              Caso já tenha realizado o pagamento, desconsidere este aviso.
              Se precisar de ajuda, nosso suporte está à disposição.
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