package org.com.smartpayments.authenticator.application.entrypoint.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.com.smartpayments.authenticator.core.domain.usecase.user.activeEmail.ActiveEmailUsecase;
import org.com.smartpayments.authenticator.core.domain.usecase.user.authUser.AuthUserUsecase;
import org.com.smartpayments.authenticator.core.domain.usecase.user.changeEmail.ChangeEmailUsecase;
import org.com.smartpayments.authenticator.core.domain.usecase.user.changePassword.ChangePasswordUsecase;
import org.com.smartpayments.authenticator.core.domain.usecase.user.forgotPassword.ForgotPasswordUsecase;
import org.com.smartpayments.authenticator.core.domain.usecase.user.registerUser.RegisterUserUsecase;
import org.com.smartpayments.authenticator.core.domain.usecase.user.resetPasswordPublic.ResetPasswordPublicUsecase;
import org.com.smartpayments.authenticator.core.domain.usecase.user.sendActiveEmail.SendActiveEmailUsecase;
import org.com.smartpayments.authenticator.core.domain.usecase.user.updateUserProfile.UpdateUserProfileUsecase;
import org.com.smartpayments.authenticator.core.domain.usecase.user.uploadProfileImage.UploadProfileImageUsecase;
import org.com.smartpayments.authenticator.core.domain.usecase.user.userProfile.UserProfileUsecase;
import org.com.smartpayments.authenticator.core.ports.in.dto.AuthUserInput;
import org.com.smartpayments.authenticator.core.ports.in.dto.ChangeEmailInput;
import org.com.smartpayments.authenticator.core.ports.in.dto.ChangePasswordInput;
import org.com.smartpayments.authenticator.core.ports.in.dto.ForgotPasswordInput;
import org.com.smartpayments.authenticator.core.ports.in.dto.RegisterUserInput;
import org.com.smartpayments.authenticator.core.ports.in.dto.ResetPasswordInput;
import org.com.smartpayments.authenticator.core.ports.in.dto.SendActiveEmailInput;
import org.com.smartpayments.authenticator.core.ports.in.dto.UpdateUserProfileInput;
import org.com.smartpayments.authenticator.core.ports.in.dto.UploadProfileImageInput;
import org.com.smartpayments.authenticator.core.ports.out.dto.AuthUserOutput;
import org.com.smartpayments.authenticator.core.ports.out.dto.ForgotPasswordOutput;
import org.com.smartpayments.authenticator.core.ports.out.dto.UserProfileOutput;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@AllArgsConstructor
@RequestMapping("${server.api-suffix}")
public class UserController {
    private final RegisterUserUsecase registerUserUsecase;
    private final AuthUserUsecase authUserUsecase;
    private final UserProfileUsecase userProfileUsecase;
    private final UpdateUserProfileUsecase updateUserProfileUsecase;
    private final ActiveEmailUsecase activeEmailUsecase;
    private final SendActiveEmailUsecase sendActiveEmailUsecase;
    private final ForgotPasswordUsecase forgotPasswordUsecase;
    private final ResetPasswordPublicUsecase resetPasswordPublicUsecase;
    private final UploadProfileImageUsecase uploadProfileImageUsecase;
    private final ChangePasswordUsecase changePasswordUsecase;
    private final ChangeEmailUsecase changeEmailUsecase;

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

    @PostMapping("/user/email_activation")
    public ResponseEntity<UserProfileOutput> sendEmailActivation(@RequestBody @Valid SendActiveEmailInput input) {
        sendActiveEmailUsecase.execute(input);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PutMapping("/user/email_activation/{token}")
    public ResponseEntity<String> activeEmail(@PathVariable("token") String token) {
        try {
            activeEmailUsecase.execute(token);
            return ResponseEntity.status(HttpStatus.OK).body("E-mail activated. You can close this window.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid link. This link is invalid or has been already used.");
        }
    }

    @PutMapping("/user/change_email")
    public ResponseEntity<Void> changeEmail(Authentication authentication, @RequestBody @Valid ChangeEmailInput input) {
        Long userId = (Long) authentication.getPrincipal();
        input.setUserId(userId);
        changeEmailUsecase.execute(input);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PutMapping("/user/forgot_password")
    public ResponseEntity<ForgotPasswordOutput> forgotPassword(@RequestBody @Valid ForgotPasswordInput input) {
        ForgotPasswordOutput output = forgotPasswordUsecase.execute(input);
        return ResponseEntity.status(HttpStatus.OK).body(output);
    }

    @PutMapping("/user/reset_password/{token}")
    public ResponseEntity<ForgotPasswordOutput> resetPassword(@PathVariable("token") String token, @RequestBody @Valid ResetPasswordInput input) {
        input.setToken(token);
        resetPasswordPublicUsecase.execute(input);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PutMapping("/user/change_password")
    public ResponseEntity<ForgotPasswordOutput> changePassword(Authentication authentication, @RequestBody @Valid ChangePasswordInput input) {
        Long userId = (Long) authentication.getPrincipal();
        input.setUserId(userId);
        changePasswordUsecase.execute(input);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping(path = "/user/profile_image", consumes = "multipart/form-data")
    public ResponseEntity<Void> updateUserProfile(Authentication authentication, @RequestParam("file") MultipartFile file) {
        Long userId = (Long) authentication.getPrincipal();
        uploadProfileImageUsecase.execute(new UploadProfileImageInput(userId, file));
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /*@GetMapping("/user/internal")
    public ResponseEntity<String> findUserForInternalServices(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.status(HttpStatus.OK).body("OK:" + userId);
    }*/
}
