package org.com.smartpayments.authenticator.infra.adapters.utils;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.smartpayments.authenticator.core.common.exception.EmailTemplateNotFoundException;
import org.com.smartpayments.authenticator.core.common.exception.GenericException;
import org.com.smartpayments.authenticator.core.domain.model.EmailTemplate;
import org.com.smartpayments.authenticator.core.ports.in.dto.SendEmailInput;
import org.com.smartpayments.authenticator.core.ports.out.dataProvider.EmailTemplateDataProviderPort;
import org.com.smartpayments.authenticator.core.ports.out.utils.EmailUtilsPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Matcher;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailUtilsAdapter implements EmailUtilsPort {
    private final EmailTemplateDataProviderPort emailTemplateDataProviderPort;

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    @Override
    public void sendEmail(SendEmailInput input) {
        try {
            EmailTemplate emailTemplate = findTemplate(input.getTemplateName());
            String prepareBody = mountEmailBody(emailTemplate.getHtmlBody(), input.getVariables());

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(from);
            helper.setTo(input.getTo());
            helper.setSubject(input.getSubject());
            helper.setText(prepareBody, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("Error while sending message e-mail!", e);
            throw new GenericException("Error while sending message e-mail!");
        }
    }

    private EmailTemplate findTemplate(String name) {
        return emailTemplateDataProviderPort.findByName(name)
            .orElseThrow(() -> new EmailTemplateNotFoundException(name));
    }

    private String mountEmailBody(String body, Map<String, Object> variables) {
        for (String variable : variables.keySet()) {
            body = body.replace(variable, Matcher.quoteReplacement(variables.get(variable).toString()));
        }

        return body;
    }
}
