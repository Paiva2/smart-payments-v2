package org.com.smartpayments.authenticator.core.domain.usecase.user.authUser.exception;

import org.com.smartpayments.authenticator.core.common.exception.base.ForbiddenException;

public class WrongCredentialsException extends ForbiddenException {
    public WrongCredentialsException() {
        super("Wrong credentials!");
    }
}
