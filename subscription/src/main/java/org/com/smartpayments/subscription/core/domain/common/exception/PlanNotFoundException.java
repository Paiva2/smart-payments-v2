package org.com.smartpayments.subscription.core.domain.common.exception;

public class PlanNotFoundException extends RuntimeException {
    public PlanNotFoundException(Long plan) {
        super(String.format("Plan with id: %d not found!", plan));
    }

    public PlanNotFoundException(String msg) {
        super(msg);
    }
}
