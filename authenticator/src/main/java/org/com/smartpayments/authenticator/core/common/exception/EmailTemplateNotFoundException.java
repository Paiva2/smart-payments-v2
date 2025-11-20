package org.com.smartpayments.authenticator.core.common.exception;

import org.com.smartpayments.authenticator.core.common.exception.base.NotFoundException;

public class EmailTemplateNotFoundException extends NotFoundException {
    public EmailTemplateNotFoundException(String message) {
        super(String.format("Email template not found! Name: %s", message));
    }
}
