package org.com.smartpayments.authenticator.application.entrypoint.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.com.smartpayments.authenticator.core.domain.usecase.user.authUser.AuthUserUsecase;
import org.com.smartpayments.authenticator.core.domain.usecase.user.registerUser.RegisterUserUsecase;
import org.com.smartpayments.authenticator.core.domain.usecase.user.updateUserProfile.UpdateUserProfileUsecase;
import org.com.smartpayments.authenticator.core.domain.usecase.user.userProfile.UserProfileUsecase;
import org.com.smartpayments.authenticator.core.ports.in.dto.AuthUserInput;
import org.com.smartpayments.authenticator.core.ports.in.dto.RegisterUserInput;
import org.com.smartpayments.authenticator.core.ports.in.dto.UpdateUserProfileInput;
import org.com.smartpayments.authenticator.core.ports.out.dto.AuthUserOutput;
import org.com.smartpayments.authenticator.core.ports.out.dto.UserProfileOutput;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("${server.api-suffix}")
public class UserController {
    private final RegisterUserUsecase registerUserUsecase;
    private final AuthUserUsecase authUserUsecase;
    private final UserProfileUsecase userProfileUsecase;
    private final UpdateUserProfileUsecase updateUserProfileUsecase;

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

    @GetMapping("/user/profile")
    public ResponseEntity<UserProfileOutput> userProfile(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        UserProfileOutput output = userProfileUsecase.execute(userId);
        return ResponseEntity.status(HttpStatus.OK).body(output);
    }

    @PutMapping("/user/profile")
    public ResponseEntity<UserProfileOutput> updateUserProfile(Authentication authentication, @RequestBody @Valid UpdateUserProfileInput input) {
        Long userId = (Long) authentication.getPrincipal();
        input.setUserId(userId);
        UserProfileOutput output = updateUserProfileUsecase.execute(input);
        return ResponseEntity.status(HttpStatus.OK).body(output);
    }

    @GetMapping("/user/internal")
    public ResponseEntity<String> findUserForInternalServices(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.status(HttpStatus.OK).body("OK:" + userId);
    }
}
