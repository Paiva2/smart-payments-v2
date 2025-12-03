package org.com.smartpayments.subscription.application.entrypoint.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.com.smartpayments.subscription.core.domain.core.ports.in.dto.NewCreditsPurchaseInput;
import org.com.smartpayments.subscription.core.domain.core.ports.in.dto.NewSubscriptionPurchaseInput;
import org.com.smartpayments.subscription.core.domain.core.ports.out.dto.NewCreditsPurchaseOutput;
import org.com.smartpayments.subscription.core.domain.core.ports.out.dto.NewSubscriptionPurchaseOutput;
import org.com.smartpayments.subscription.core.domain.core.usecase.purchase.newCreditsPurchase.NewCreditsPurchaseUsecase;
import org.com.smartpayments.subscription.core.domain.core.usecase.purchase.newSubscriptionPurchase.NewSubscriptionPurchaseUsecase;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<NewSubscriptionPurchaseOutput> newSubscriptionPurchase(@RequestBody @Valid NewSubscriptionPurchaseInput input) {
        input.setUserId(1L);//todo:
        NewSubscriptionPurchaseOutput output = newSubscriptionPurchaseUsecase.execute(input);
        return ResponseEntity.ok().body(output);
    }

    @PostMapping("purchase/credits")
    public ResponseEntity<NewCreditsPurchaseOutput> newCreditsPurchase(@RequestBody @Valid NewCreditsPurchaseInput input) {
        input.setUserId(1L);//todo:
        NewCreditsPurchaseOutput output = newCreditsPurchaseUsecase.execute(input);
        return ResponseEntity.ok().body(output);
    }
}
