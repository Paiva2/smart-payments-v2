package org.com.smartpayments.authenticator.application.entrypoint.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.com.smartpayments.authenticator.core.domain.usecase.user.registerUser.RegisterUserUsecase;
import org.com.smartpayments.authenticator.core.ports.in.dto.RegisterUserInput;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("${server.api-suffix}")
public class UserController {
    private final RegisterUserUsecase registerUserUsecase;

    @PostMapping("/user/register")
    public ResponseEntity<Void> registerUser(@RequestBody @Valid RegisterUserInput input) {
        registerUserUsecase.execute(input);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
