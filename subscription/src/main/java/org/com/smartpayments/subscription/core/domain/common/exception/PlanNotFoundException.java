package org.com.smartpayments.subscription.core.domain.common.exception;

import org.com.smartpayments.subscription.core.domain.common.base.NotFoundException;

public class PlanNotFoundException extends NotFoundException {
    public PlanNotFoundException(Long plan) {
        super(String.format("Plan with id: %d not found!", plan));
    }

    public PlanNotFoundException(String msg) {
        super(msg);
    }
}
