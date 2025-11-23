package org.com.smartpayments.authenticator.core.common.exception;

import org.com.smartpayments.authenticator.core.common.exception.base.BadRequestException;

public class PasswordResetTokenExpiredException extends BadRequestException {
    public PasswordResetTokenExpiredException() {
        super("Password reset token has expired!");
    }
}
