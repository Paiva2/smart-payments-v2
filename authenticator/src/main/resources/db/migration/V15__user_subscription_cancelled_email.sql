INSERT INTO public.email_templates (name, html_body)
VALUES ('subscription-cancelled', '<!DOCTYPE html>
<html lang="pt-BR">
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <meta http-equiv="X-UA-Compatible" content="IE=edge" />
    <title>Assinatura cancelada - Smart Payments</title>

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
      Sua assinatura foi cancelada.
    </div>

    <table
      role="presentation"
      class="email-wrap"
      width="100%"
      cellpadding="0"
      cellspacing="0"
    >
      <tr>
        <td align="center">
          <table
            role="presentation"
            class="email-container"
            width="100%"
            cellpadding="0"
            cellspacing="0"
          >
            <tr>
              <td class="header">
                <div class="logo">Smart Payments</div>
              </td>
            </tr>

            <tr>
              <td class="content">
                <p class="greeting">Olá ${FIRST_NAME},</p>

                <p class="lead">
                  Sua assinatura foi cancelada.
                </p>

                <p class="lead">
                  A partir deste momento, o acesso aos serviços vinculados à assinatura
                  encontra-se indisponível.
                </p>

                <p class="lead">
                  Caso deseje voltar a utilizar nossos serviços, você pode realizar
                  uma nova contratação a qualquer momento.
                </p>

                <div class="divider"></div>

                <p class="lead">
                  Se você acredita que este cancelamento ocorreu por engano ou
                  se tiver qualquer dúvida, nossa equipe de suporte está pronta para ajudar.
                </p>
              </td>
            </tr>

            <tr>
              <td class="footer">
                <p style="margin: 0 0 8px 0;">
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