package org.com.smartpayments.subscription.core.common.exception;

import org.com.smartpayments.subscription.core.common.exception.base.NotFoundException;

public class PlanNotFoundException extends NotFoundException {
    public PlanNotFoundException(Long plan) {
        super(String.format("Plan with id: %d not found!", plan));
    }

    public PlanNotFoundException(String msg) {
        super(msg);
    }
}
