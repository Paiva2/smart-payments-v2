INSERT INTO public.email_templates (name, html_body)
VALUES ('user-subscription-expired', '<!DOCTYPE html>
<html lang="pt-BR">
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width,initial-scale=1" />
    <meta http-equiv="X-UA-Compatible" content="IE=edge" />
    <title>Assinatura expirada - Smart Payments</title>
    <style>
      html,
      body {
        margin: 0;
        padding: 0;
        height: 100%;
        background-color: #f4f6f8;
      }
      img {
        border: 0;
        display: block;
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
  .preheader {
    display: none !important;
    visibility: hidden;
    opacity: 0;
    color: transparent;
    height: 0;
    width: 0;
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

  .alert-box {
    background: #fff7ed;
    border: 1px solid #fed7aa;
    border-radius: 8px;
    padding: 16px;
    margin: 20px 0;
  }
  .alert-title {
    font-size: 15px;
    font-weight: 600;
    margin: 0 0 8px 0;
    color: #9a3412;
  }
  .alert-text {
    font-size: 14px;
    color: #7c2d12;
    margin: 0;
  }

  .divider {
    height: 1px;
    background: #eef2f7;
    margin: 20px 0;
  }

  .footer {
    padding: 18px 32px;
    font-size: 13px;
    color: #94a3b8;
  }
</style>
</head>
    <body>
        <div class="preheader">
          Sua assinatura está expirada, mas você ainda pode regularizar o pagamento.
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
                      Identificamos que o pagamento da sua assinatura não foi realizado na data de vencimento.
                      Por esse motivo, sua assinatura encontra-se <strong>temporariamente expirada</strong>.
                    </p>

                    <div class="alert-box">
                      <p class="alert-title">⚠️ Atenção</p>
                      <p class="alert-text">
                        Você tem até <strong>${MAX_DAYS_PAYMENT_SUBSCRIPTION}</strong> para regularizar o pagamento.
                        Durante esse período, sua assinatura não será cancelada.
                      </p>
                    </div>

                    <p class="lead">
                      Caso o pagamento não seja confirmado dentro desse prazo,
                      sua assinatura será <strong>cancelada automaticamente</strong>.
                    </p>

                    <p class="lead">
                      Para evitar o cancelamento e continuar utilizando nossos serviços,
                      basta realizar o pagamento pendente dentro do prazo informado.
                    </p>

                    <p class="lead">
                      Prazo máximo para pagamento: ${SUBSCRIPTION_EXPIRES_AT}
                    </p>

                    <div class="divider"></div>

                    <p class="lead">
                      Se você já efetuou o pagamento, desconsidere este e-mail.
                      Em caso de dúvidas ou inconsistências, nosso suporte está à disposição.
                    </p>
                  </td>
                </tr>

                <tr>
                  <td class="footer">
                    <p style="margin:0 0 8px 0;">
                      Atenciosamente,<br />
                      <strong>Equipe Smart Payments</strong>
                    </p>
                  </td>
                </tr>

              </table>
            </td>
          </tr>
        </table>
  </body>
</html>
')