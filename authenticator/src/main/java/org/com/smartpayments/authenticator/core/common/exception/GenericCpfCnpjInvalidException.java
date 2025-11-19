package org.com.smartpayments.authenticator.core.common.exception;

import org.com.smartpayments.authenticator.core.common.exception.base.BadRequestException;

public class GenericCpfCnpjInvalidException extends BadRequestException {
    public GenericCpfCnpjInvalidException(String message) {
        super(String.format("Invalid document. %s", message));
    }
}
