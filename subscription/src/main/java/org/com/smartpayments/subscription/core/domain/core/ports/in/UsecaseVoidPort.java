package org.com.smartpayments.subscription.core.domain.core.ports.in;

public interface UsecaseVoidPort<I> {
    void execute(I input);
}
