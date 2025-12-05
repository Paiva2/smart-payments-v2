package org.com.smartpayments.subscription.core.common.exception;

import org.com.smartpayments.subscription.core.common.base.NotFoundException;

public class CreditNotFoundException extends NotFoundException {
    public CreditNotFoundException(Long credit) {
        super(String.format("Credit with id: %d not found!", credit));
    }

    public CreditNotFoundException(String msg) {
        super(msg);
    }
}
