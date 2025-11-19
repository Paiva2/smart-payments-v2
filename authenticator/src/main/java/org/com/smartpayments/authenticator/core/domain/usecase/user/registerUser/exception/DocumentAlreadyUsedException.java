package org.com.smartpayments.authenticator.core.domain.usecase.user.registerUser.exception;

import org.com.smartpayments.authenticator.core.common.exception.base.ConflictException;

public class DocumentAlreadyUsedException extends ConflictException {
    public DocumentAlreadyUsedException() {
        super("Cpf or Cnpj provided is already being used!");
    }
}
