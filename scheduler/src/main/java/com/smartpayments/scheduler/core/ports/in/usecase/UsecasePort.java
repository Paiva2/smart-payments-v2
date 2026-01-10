package com.smartpayments.scheduler.core.ports.in.usecase;

public interface UsecasePort<I, O> {
    O execute(I input);
}
