package org.com.smartpayments.authenticator.core.ports.in;

public interface UsecaseVoidPort<I> {
    void execute(I input);
}
