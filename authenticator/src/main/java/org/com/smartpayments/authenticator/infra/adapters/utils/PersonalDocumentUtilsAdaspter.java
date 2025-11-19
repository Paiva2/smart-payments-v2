package org.com.smartpayments.authenticator.infra.adapters.utils;

import org.com.smartpayments.authenticator.core.ports.out.utils.PersonalDocumentUtils;
import org.springframework.stereotype.Component;

import java.util.InputMismatchException;

@Component
public class PersonalDocumentUtilsAdaspter implements PersonalDocumentUtils {
    @Override
    public Boolean isValidCpf(String cpf) {
        if (cpf == null) {
            return false;
        }

        cpf = cpf.replace(".", "");
        cpf = cpf.replace("-", "");

        if (cpf.equals("00000000000") ||
            cpf.equals("11111111111") ||
            cpf.equals("22222222222") || cpf.equals("33333333333") ||
            cpf.equals("44444444444") || cpf.equals("55555555555") ||
            cpf.equals("66666666666") || cpf.equals("77777777777") ||
            cpf.equals("88888888888") || cpf.equals("99999999999") ||
            (cpf.length() != 11)) {
            return false;
        }

        if (!cpf.chars().allMatch(Character::isDigit)) {
            return false;
        }

        int d1, d2;
        int digit1, digit2, rest;
        int digitCPF;
        String nDigResult;

        d1 = d2 = 0;
        digit1 = digit2 = rest = 0;

        for (int nCount = 1; nCount < cpf.length() - 1; nCount++) {
            digitCPF = Integer.valueOf(cpf.substring(nCount - 1, nCount)).intValue();

            d1 = d1 + (11 - nCount) * digitCPF;

            d2 = d2 + (12 - nCount) * digitCPF;
        }

        rest = (d1 % 11);

        if (rest < 2) {
            digit1 = 0;
        } else {
            digit1 = 11 - rest;
        }


        d2 += 2 * digit1;

        rest = (d2 % 11);

        if (rest < 2) {
            digit2 = 0;
        } else {
            digit2 = 11 - rest;
        }

        String nDigVerify = cpf.substring(cpf.length() - 2, cpf.length());

        nDigResult = String.valueOf(digit1) + String.valueOf(digit2);

        return nDigVerify.equals(nDigResult);
    }

    @Override
    public Boolean isValidCnpj(String cnpj) {
        cnpj = cnpj.replace(".", "");
        cnpj = cnpj.replace("-", "");
        cnpj = cnpj.replace("/", "");

        try {
            Long.parseLong(cnpj);
        } catch (NumberFormatException e) {
            return false;
        }

        if (cnpj.equals("00000000000000") || cnpj.equals("11111111111111")
            || cnpj.equals("22222222222222") || cnpj.equals("33333333333333")
            || cnpj.equals("44444444444444") || cnpj.equals("55555555555555")
            || cnpj.equals("66666666666666") || cnpj.equals("77777777777777")
            || cnpj.equals("88888888888888") || cnpj.equals("99999999999999")
            || (cnpj.length() != 14)) {
            return false;
        }

        char dig13, dig14;
        int sm, i, r, num, weight;

        try {
            sm = 0;
            weight = 2;

            for (i = 11; i >= 0; i--) {
                num = (int) (cnpj.charAt(i) - 48);
                sm = sm + (num * weight);
                weight = weight + 1;

                if (weight == 10) {
                    weight = 2;
                }
            }

            r = sm % 11;

            if ((r == 0) || (r == 1)) {
                dig13 = '0';
            } else {
                dig13 = (char) ((11 - r) + 48);
            }

            sm = 0;
            weight = 2;

            for (i = 12; i >= 0; i--) {
                num = (int) (cnpj.charAt(i) - 48);
                sm = sm + (num * weight);
                weight = weight + 1;

                if (weight == 10) {
                    weight = 2;
                }
            }

            r = sm % 11;

            if ((r == 0) || (r == 1)) {
                dig14 = '0';
            } else {
                dig14 = (char) ((11 - r) + 48);
            }

            return (dig13 == cnpj.charAt(12)) && (dig14 == cnpj.charAt(13));
        } catch (InputMismatchException erro) {
            return false;
        }
    }
}
