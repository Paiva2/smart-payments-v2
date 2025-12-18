package org.com.smartpayments.authenticator.core.ports.in;

import java.text.ParseException;

public interface UsecaseVoidPort<I> {
    void execute(I input) throws ParseException;
}
