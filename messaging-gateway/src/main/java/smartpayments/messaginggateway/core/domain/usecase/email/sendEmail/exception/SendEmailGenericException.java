package smartpayments.messaginggateway.core.domain.usecase.email.sendEmail.exception;

public class SendEmailGenericException extends RuntimeException {
    public SendEmailGenericException(String message) {
        super(message);
    }
}
