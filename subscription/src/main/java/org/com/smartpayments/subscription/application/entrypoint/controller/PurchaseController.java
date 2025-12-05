package org.com.smartpayments.subscription.application.entrypoint.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.com.smartpayments.subscription.core.domain.usecase.purchase.newCreditsPurchase.NewCreditsPurchaseUsecase;
import org.com.smartpayments.subscription.core.domain.usecase.purchase.newSubscriptionPurchase.NewSubscriptionPurchaseUsecase;
import org.com.smartpayments.subscription.core.ports.in.dto.NewCreditsPurchaseInput;
import org.com.smartpayments.subscription.core.ports.in.dto.NewSubscriptionPurchaseInput;
import org.com.smartpayments.subscription.core.ports.out.dto.NewCreditsPurchaseOutput;
import org.com.smartpayments.subscription.core.ports.out.dto.NewSubscriptionPurchaseOutput;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("${server.api-suffix}")
public class PurchaseController {
    private final NewSubscriptionPurchaseUsecase newSubscriptionPurchaseUsecase;
    private final NewCreditsPurchaseUsecase newCreditsPurchaseUsecase;

    @PostMapping("purchase/subscription")
    public ResponseEntity<NewSubscriptionPurchaseOutput> newSubscriptionPurchase(Authentication authentication, @RequestBody @Valid NewSubscriptionPurchaseInput input) {
        Long userId = (Long) authentication.getPrincipal();
        input.setUserId(userId);
        NewSubscriptionPurchaseOutput output = newSubscriptionPurchaseUsecase.execute(input);
        return ResponseEntity.ok().body(output);
    }

    @PostMapping("purchase/credit")
    public ResponseEntity<NewCreditsPurchaseOutput> newCreditsPurchase(Authentication authentication, @RequestBody @Valid NewCreditsPurchaseInput input) {
        Long userId = (Long) authentication.getPrincipal();
        input.setUserId(userId);
        NewCreditsPurchaseOutput output = newCreditsPurchaseUsecase.execute(input);
        return ResponseEntity.ok().body(output);
    }
}
