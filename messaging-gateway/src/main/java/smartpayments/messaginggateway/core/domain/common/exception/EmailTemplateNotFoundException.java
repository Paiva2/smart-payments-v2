package smartpayments.messaginggateway.core.domain.common.exception;

public class EmailTemplateNotFoundException extends RuntimeException {
    public EmailTemplateNotFoundException(String message) {
        super(String.format("Email template not found! Template name: %s", message));
    }
}
