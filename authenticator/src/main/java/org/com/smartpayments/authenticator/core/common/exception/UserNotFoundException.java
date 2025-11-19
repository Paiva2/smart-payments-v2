package org.com.smartpayments.authenticator.core.common.exception;

import org.com.smartpayments.authenticator.core.common.exception.base.NotFoundException;

public class UserNotFoundException extends NotFoundException {
    public UserNotFoundException() {
        super("User not found!");
    }
}
