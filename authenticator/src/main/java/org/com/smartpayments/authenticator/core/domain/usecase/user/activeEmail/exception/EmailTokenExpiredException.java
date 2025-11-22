package org.com.smartpayments.authenticator.core.domain.usecase.user.activeEmail.exception;

import org.com.smartpayments.authenticator.core.common.exception.base.BadRequestException;

public class EmailTokenExpiredException extends BadRequestException {
    public EmailTokenExpiredException() {
        super("Invalid e-mail token. Token expired!");
    }
}
