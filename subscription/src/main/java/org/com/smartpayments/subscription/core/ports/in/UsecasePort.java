package org.com.smartpayments.subscription.core.ports.in;

public interface UsecasePort<I, O> {
    O execute(I input);
}
