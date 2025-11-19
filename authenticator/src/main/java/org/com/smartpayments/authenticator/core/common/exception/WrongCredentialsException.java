package org.com.smartpayments.authenticator.core.common.exception;

import org.com.smartpayments.authenticator.core.common.exception.base.BadRequestException;

public class WrongCredentialsException extends BadRequestException {
    public WrongCredentialsException() {
        super("Wrong credentials!");
    }
}
