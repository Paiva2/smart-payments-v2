package org.com.smartpayments.authenticator.application.entrypoint.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.com.smartpayments.authenticator.core.domain.usecase.user.authUser.AuthUserUsecase;
import org.com.smartpayments.authenticator.core.domain.usecase.user.registerUser.RegisterUserUsecase;
import org.com.smartpayments.authenticator.core.ports.in.dto.AuthUserInput;
import org.com.smartpayments.authenticator.core.ports.in.dto.RegisterUserInput;
import org.com.smartpayments.authenticator.core.ports.out.dto.AuthUserOutput;
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
    private final AuthUserUsecase authUserUsecase;

    @PostMapping("/user/register")
    public ResponseEntity<Void> registerUser(@RequestBody @Valid RegisterUserInput input) {
        registerUserUsecase.execute(input);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/user/auth")
    public ResponseEntity<AuthUserOutput> authUser(@RequestBody @Valid AuthUserInput input) {
        AuthUserOutput output = authUserUsecase.execute(input);
        return ResponseEntity.status(HttpStatus.OK).body(output);
    }
}
