package org.com.smartpayments.subscription.core.ports.out.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.com.smartpayments.subscription.core.domain.enums.EUserSubscriptionState;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AsyncSubscriptionPlanStateInput {
    private EUserSubscriptionState state;
    private Long userSubscriptionId;
    private Long userId;
}
