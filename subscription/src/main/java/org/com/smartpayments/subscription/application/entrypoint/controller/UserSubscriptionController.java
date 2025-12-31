package org.com.smartpayments.subscription.application.entrypoint.controller;

import lombok.AllArgsConstructor;
import org.com.smartpayments.subscription.core.domain.usecase.userSubscription.cancelSubscription.CancelSubscriptionUsecase;
import org.com.smartpayments.subscription.core.ports.out.dto.NewSubscriptionPurchaseOutput;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("${server.api-suffix}")
public class UserSubscriptionController {
    private final CancelSubscriptionUsecase cancelSubscriptionUsecase;

    @PutMapping("user-subscription/cancel")
    public ResponseEntity<NewSubscriptionPurchaseOutput> cancelSubscription(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        cancelSubscriptionUsecase.execute(userId);
        return ResponseEntity.noContent().build();
    }
}
