package org.com.smartpayments.authenticator.core.domain.usecase.user.resetPasswordPublic;

import lombok.AllArgsConstructor;
import org.com.smartpayments.authenticator.core.common.exception.PasswordResetTokenExpiredException;
import org.com.smartpayments.authenticator.core.common.exception.UserNotFoundException;
import org.com.smartpayments.authenticator.core.domain.model.User;
import org.com.smartpayments.authenticator.core.ports.in.UsecaseVoidPort;
import org.com.smartpayments.authenticator.core.ports.in.dto.ResetPasswordInput;
import org.com.smartpayments.authenticator.core.ports.out.dataProvider.UserDataProviderPort;
import org.com.smartpayments.authenticator.core.ports.out.utils.PasswordUtilsPort;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;

import static org.com.smartpayments.authenticator.core.common.constants.Constants.MAX_EXPIRATION_DAYS_PASSWORD_TOKEN;

@Service
@AllArgsConstructor
public class ResetPasswordPublicUsecase implements UsecaseVoidPort<ResetPasswordInput> {
    private final UserDataProviderPort userDataProviderPort;

    private final PasswordUtilsPort passwordUtilsPort;

    @Override
    public void execute(ResetPasswordInput input) {
        User user = findUser(input.getToken());

        validateTokenExpiration(user.getPasswordTokenSentAt());

        user.setPasswordHash(passwordUtilsPort.hashPassword(input.getPassword()));
        user.setPasswordToken(null);
        persistUser(user);
    }

    private User findUser(String passwordToken) {
        return userDataProviderPort.findActiveByPasswordToken(passwordToken)
            .orElseThrow(UserNotFoundException::new);
    }

    private void validateTokenExpiration(Date tokenSentAt) {
        Calendar limit = Calendar.getInstance();
        limit.setTime(new Date());
        limit.add(Calendar.DAY_OF_MONTH, -MAX_EXPIRATION_DAYS_PASSWORD_TOKEN);

        if (tokenSentAt.before(limit.getTime())) {
            throw new PasswordResetTokenExpiredException();
        }
    }

    private void persistUser(User user) {
        userDataProviderPort.persist(user);
    }
}
