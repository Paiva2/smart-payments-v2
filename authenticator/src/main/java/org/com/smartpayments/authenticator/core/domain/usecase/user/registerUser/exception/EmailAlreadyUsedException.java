package org.com.smartpayments.authenticator.core.domain.usecase.user.registerUser.exception;

public class EmailAlreadyUsedException extends RuntimeException {
    public EmailAlreadyUsedException() {
        super("E-mail provided is already being used!");
    }
}
