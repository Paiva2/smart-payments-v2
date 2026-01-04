package org.com.smartpayments.authenticator.core.domain.usecase.user.authUser;

import lombok.AllArgsConstructor;
import org.com.smartpayments.authenticator.core.common.exception.UserEmailNotActiveException;
import org.com.smartpayments.authenticator.core.common.exception.UserNotActiveException;
import org.com.smartpayments.authenticator.core.common.exception.UserNotFoundException;
import org.com.smartpayments.authenticator.core.domain.model.User;
import org.com.smartpayments.authenticator.core.domain.usecase.user.authUser.exception.WrongCredentialsException;
import org.com.smartpayments.authenticator.core.ports.in.UsecasePort;
import org.com.smartpayments.authenticator.core.ports.in.dto.AuthUserInput;
import org.com.smartpayments.authenticator.core.ports.out.dataProvider.UserDataProviderPort;
import org.com.smartpayments.authenticator.core.ports.out.dto.AuthUserOutput;
import org.com.smartpayments.authenticator.core.ports.out.utils.JwtUtilsPort;
import org.com.smartpayments.authenticator.core.ports.out.utils.PasswordUtilsPort;
import org.springframework.stereotype.Service;

import static java.util.Objects.isNull;

@Service
@AllArgsConstructor
public class AuthUserUsecase implements UsecasePort<AuthUserInput, AuthUserOutput> {
    private final static int AUTH_EXPIRATION_DAYS = 7;
    private final UserDataProviderPort userDataProviderPort;

    private final JwtUtilsPort jwtUtilsPort;
    private final PasswordUtilsPort passwordUtilsPort;

    @Override
    public AuthUserOutput execute(AuthUserInput input) {
        User user = findUser(input.getEmail());

        if (!user.getActive()) {
            throw new UserNotActiveException();
        }
        
        if (isNull(user.getEmailConfirmedAt())) {
            throw new UserEmailNotActiveException();
        }

        checkPassword(input.getPassword(), user.getPasswordHash());

        return new AuthUserOutput(jwtUtilsPort.generateAuthJwt(user.getId(), AUTH_EXPIRATION_DAYS));
    }

    private User findUser(String email) {
        return userDataProviderPort.findByEmail(email).orElseThrow(UserNotFoundException::new);
    }

    private void checkPassword(String password, String passwordHash) {
        if (!passwordUtilsPort.comparePassword(password, passwordHash)) {
            throw new WrongCredentialsException();
        }
    }
}
