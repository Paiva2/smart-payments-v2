package org.com.smartpayments.authenticator.core.ports.out.utils;

public interface PersonalDocumentUtilsPort {
    Boolean isValidCpf(String cpf);

    Boolean isValidCnpj(String cpf);
}
