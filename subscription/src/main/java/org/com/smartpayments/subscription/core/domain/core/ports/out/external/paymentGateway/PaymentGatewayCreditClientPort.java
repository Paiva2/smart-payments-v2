package org.com.smartpayments.subscription.core.domain.core.ports.out.external.paymentGateway;

import org.com.smartpayments.subscription.core.domain.core.ports.out.external.dto.CreateCreditChargeOutput;
import org.com.smartpayments.subscription.core.domain.core.ports.out.external.dto.DeleteChargeOutput;
import org.com.smartpayments.subscription.core.domain.core.ports.out.external.dto.NewCreditChargeClientInput;
import org.com.smartpayments.subscription.core.domain.model.User;

public interface PaymentGatewayCreditClientPort {
    CreateCreditChargeOutput createCreditCharge(User user, NewCreditChargeClientInput input);

    DeleteChargeOutput deleteCharge(User user, String chargeId);
}
