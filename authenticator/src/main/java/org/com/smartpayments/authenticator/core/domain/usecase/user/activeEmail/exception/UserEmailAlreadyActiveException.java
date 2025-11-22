package org.com.smartpayments.authenticator.core.domain.usecase.user.activeEmail.exception;

import org.com.smartpayments.authenticator.core.common.exception.base.ConflictException;

public class UserEmailAlreadyActiveException extends ConflictException {
    public UserEmailAlreadyActiveException() {
        super("E-mail already active!");
    }
}
