package smartpayments.messaginggateway.core.domain.usecase.email.sendEmail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import smartpayments.messaginggateway.core.domain.common.exception.EmailTemplateNotFoundException;
import smartpayments.messaginggateway.core.domain.model.EmailTemplate;
import smartpayments.messaginggateway.core.domain.usecase.email.sendEmail.exception.SendEmailGenericException;
import smartpayments.messaginggateway.core.ports.in.usecase.SendEmailInput;
import smartpayments.messaginggateway.core.ports.out.dataprovider.EmailTemplateDataProviderPort;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;

@Slf4j
@Service
@RequiredArgsConstructor
public class SendEmailUsecase {
    private final EmailTemplateDataProviderPort emailTemplateDataProviderPort;

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    public void execute(SendEmailInput input) {
        Optional<EmailTemplate> emailTemplate = findTemplate(input.getTemplate());

        if (emailTemplate.isEmpty()) {
            log.error("[SendEmailUsecase#execute] - Email template not found: {} | To: {}", input.getTemplate(), input.getTo());
            throw new EmailTemplateNotFoundException(input.getTemplate());
        }

        try {
            String prepareBody = mountEmailBody(emailTemplate.get().getHtmlBody(), input.getVariables());
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(from);
            helper.setTo(input.getTo());
            helper.setSubject(input.getSubject());
            helper.setText(prepareBody, true);
            helper.setCc(input.getCc().toArray(String[]::new));

            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("[SendEmailUsecase#execute] - Error while trying to send e-mail: {} | To: {}", e.getMessage(), input.getTo());
            throw new SendEmailGenericException("Error while sending message e-mail!");
        }
    }

    private Optional<EmailTemplate> findTemplate(String templateName) {
        return emailTemplateDataProviderPort.findByName(templateName);
    }

    private String mountEmailBody(String body, Map<CharSequence, Object> variables) {
        for (CharSequence variable : variables.keySet()) {
            body = body.replace(variable, Matcher.quoteReplacement(variables.get(variable).toString()));
        }

        return body;
    }
}
