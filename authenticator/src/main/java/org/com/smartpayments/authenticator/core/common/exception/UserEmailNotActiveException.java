package org.com.smartpayments.authenticator.core.common.exception;

import org.com.smartpayments.authenticator.core.common.exception.base.BadRequestException;

public class UserEmailNotActiveException extends BadRequestException {
    public UserEmailNotActiveException() {
        super("User e-mail not active!");
    }
}
