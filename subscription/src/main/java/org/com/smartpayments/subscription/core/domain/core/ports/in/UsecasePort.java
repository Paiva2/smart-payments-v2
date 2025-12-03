package org.com.smartpayments.subscription.core.domain.core.ports.in;

public interface UsecasePort<I, O> {
    O execute(I input);
}
