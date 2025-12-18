INSERT INTO public.email_templates (name, html_body)
VALUES ('purchase-confirmed', '<!DOCTYPE html>
<html lang="pt-BR">
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width,initial-scale=1" />
    <meta http-equiv="X-UA-Compatible" content="IE=edge" />
    <title>Compra de Créditos Confirmada — Smart Payments</title>
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
        -ms-interpolation-mode: bicubic;
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
        letter-spacing: 0.2px;
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

      .credits-box {
        background: #f8fafc;
        border: 1px solid #e5e7eb;
        border-radius: 8px;
        padding: 16px;
        margin: 20px 0;
      }
      .credits-title {
        font-size: 15px;
        font-weight: 600;
        margin: 0 0 10px 0;
        color: #111827;
      }
      .credits-list {
        margin: 0;
        padding-left: 18px;
        color: #334155;
        font-size: 14px;
      }
      .credits-list li {
        margin-bottom: 6px;
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

      @media only screen and (max-width: 480px) {
        .content {
          padding: 20px;
        }
        .header {
          padding: 18px;
        }
        .logo {
          font-size: 18px;
        }
        .greeting {
          font-size: 18px;
        }
      }
    </style>
  </head>
  <body>
    <div class="preheader">
      Sua compra de créditos no Smart Payments foi confirmada com sucesso.
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
                  O pagamento da compra ${PURCHASE_TYPE} no <strong>Smart Payments</strong> foi
                  confirmado com sucesso.
                </p>

                <p class="lead">
                  Abaixo estão os detalhes da sua compra:
                </p>

                <div class="credits-box">
                  <p class="credits-title">
                    Resumo:
                  </p>

                  <ul class="credits-list">
                    ${PURCHASE_ITEMS_LI}
                  </ul>
                </div>

                <p class="lead">
                  ${LEAD_MESSAGE}
                </p>

                <div class="divider"></div>

                <p class="lead">
                  Caso não reconheça esta compra ou identifique qualquer
                  inconsistência, entre em contato com nosso suporte.
                </p>
              </td>
            </tr>

            <tr>
               <td class="footer">
                  <p style="margin:0 0 8px 0;">
                     Best regards,<br/>
                     <strong>The Smart Payments Team</strong>
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