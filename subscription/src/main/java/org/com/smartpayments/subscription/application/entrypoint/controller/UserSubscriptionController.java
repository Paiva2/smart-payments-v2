package org.com.smartpayments.subscription.application.entrypoint.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.com.smartpayments.subscription.core.domain.usecase.userSubscription.cancelSubscription.CancelSubscriptionUsecase;
import org.com.smartpayments.subscription.core.domain.usecase.userSubscription.getUserSubscriptionInternal.GetUserSubscriptionInternalUsecase;
import org.com.smartpayments.subscription.core.domain.usecase.userSubscriptionCreditsHistory.consumeUserCreditsInternal.ConsumeUserCreditsInternalUsecase;
import org.com.smartpayments.subscription.core.ports.in.dto.ConsumeUserCreditsInternalInput;
import org.com.smartpayments.subscription.core.ports.out.dto.ConsumeUserCreditsInternalOutput;
import org.com.smartpayments.subscription.core.ports.out.dto.GetUserSubscriptionInternalOutput;
import org.com.smartpayments.subscription.core.ports.out.dto.NewSubscriptionPurchaseOutput;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("${server.api-suffix}")
public class UserSubscriptionController {
    private final CancelSubscriptionUsecase cancelSubscriptionUsecase;
    private final GetUserSubscriptionInternalUsecase getUserSubscriptionInternalUsecase;
    private final ConsumeUserCreditsInternalUsecase consumeUserCreditsInternalUsecase;

    @PutMapping("user-subscription/cancel")
    public ResponseEntity<NewSubscriptionPurchaseOutput> cancelSubscription(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        cancelSubscriptionUsecase.execute(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("user-subscription/internal")
    public ResponseEntity<GetUserSubscriptionInternalOutput> getSubscriptionInternal(@RequestHeader("X-User-Id") Long userId) {
        GetUserSubscriptionInternalOutput output = getUserSubscriptionInternalUsecase.execute(userId);
        return ResponseEntity.ok().body(output);
    }

    @PutMapping("user-subscription/credits/consume/internal")
    public ResponseEntity<ConsumeUserCreditsInternalOutput> consumeCredits(@RequestHeader("X-IDEMPOTENCY-KEY") String idempotencyKey, @RequestBody @Valid ConsumeUserCreditsInternalInput input) {
        input.setIdempotencyKey(idempotencyKey);
        ConsumeUserCreditsInternalOutput output = consumeUserCreditsInternalUsecase.execute(input);
        return ResponseEntity.ok().body(output);
    }
}
