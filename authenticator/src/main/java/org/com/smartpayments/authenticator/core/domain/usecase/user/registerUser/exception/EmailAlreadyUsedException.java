package org.com.smartpayments.authenticator.core.domain.usecase.user.registerUser.exception;

import org.com.smartpayments.authenticator.core.common.exception.base.ConflictException;

public class EmailAlreadyUsedException extends ConflictException {
    public EmailAlreadyUsedException() {
        super("E-mail provided is already being used!");
    }
}
