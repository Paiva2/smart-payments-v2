package com.smartpayments.scheduler.core.ports.in.usecase;

public interface UsecaseVoidPort<I> {
    void execute(I input);
}
