package org.com.smartpayments.authenticator.core.ports.in;

public interface UsecasePort<I, O> {
    O execute(I input);
}
