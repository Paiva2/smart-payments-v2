package org.com.smartpayments.authenticator.core.domain.usecase.user.changeEmail;

import lombok.AllArgsConstructor;
import org.com.smartpayments.authenticator.core.common.exception.GenericException;
import org.com.smartpayments.authenticator.core.common.exception.UserEmailNotActiveException;
import org.com.smartpayments.authenticator.core.common.exception.UserNotFoundException;
import org.com.smartpayments.authenticator.core.domain.model.User;
import org.com.smartpayments.authenticator.core.domain.usecase.user.registerUser.exception.EmailAlreadyUsedException;
import org.com.smartpayments.authenticator.core.domain.usecase.user.sendActiveEmail.SendActiveEmailUsecase;
import org.com.smartpayments.authenticator.core.ports.in.UsecaseVoidPort;
import org.com.smartpayments.authenticator.core.ports.in.dto.ChangeEmailInput;
import org.com.smartpayments.authenticator.core.ports.in.dto.SendActiveEmailInput;
import org.com.smartpayments.authenticator.core.ports.out.dataProvider.UserDataProviderPort;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Objects;

import static java.util.Objects.isNull;

@Service
@AllArgsConstructor
public class ChangeEmailUsecase implements UsecaseVoidPort<ChangeEmailInput> {
    private final UserDataProviderPort userDataProviderPort;
    private final SendActiveEmailUsecase sendActiveEmailUsecase;

    @Override
    public void execute(ChangeEmailInput input) {
        User user = findUser(input.getUserId());

        if (isNull(user.getEmailConfirmedAt())) {
            throw new UserEmailNotActiveException();
        }

        if (Objects.equals(user.getEmail(), input.getNewEmail().toLowerCase(Locale.ROOT))) {
            throw new GenericException("New e-mail is already equals current e-mail!");
        }

        checkNewEmailUsed(input.getNewEmail());

        user.setEmailConfirmedAt(null);
        user.setEmail(input.getNewEmail().toLowerCase(Locale.ROOT));

        sendActiveEmailUsecase.execute(new SendActiveEmailInput(user, user.getEmail()));
    }

    private User findUser(Long userId) {
        return userDataProviderPort.findActiveById(userId).orElseThrow(UserNotFoundException::new);
    }

    private void checkNewEmailUsed(String newEmail) {
        userDataProviderPort.findByEmail(newEmail)
            .ifPresent(user -> {
                throw new EmailAlreadyUsedException();
            });
    }
}
