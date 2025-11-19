package org.com.smartpayments.authenticator.core.domain.usecase.user.registerUser.exception;

public class DocumentAlreadyUsedException extends RuntimeException {
    public DocumentAlreadyUsedException() {
        super("Cpf or Cnpj provided is already being used!");
    }
}
