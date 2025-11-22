package org.com.smartpayments.authenticator.core.domain.usecase.user.activeEmail;

import lombok.AllArgsConstructor;
import org.com.smartpayments.authenticator.core.common.exception.UserNotActiveException;
import org.com.smartpayments.authenticator.core.common.exception.UserNotFoundException;
import org.com.smartpayments.authenticator.core.domain.model.User;
import org.com.smartpayments.authenticator.core.domain.usecase.user.activeEmail.exception.EmailTokenExpiredException;
import org.com.smartpayments.authenticator.core.domain.usecase.user.activeEmail.exception.UserEmailAlreadyActiveException;
import org.com.smartpayments.authenticator.core.ports.in.UsecaseVoidPort;
import org.com.smartpayments.authenticator.core.ports.out.dataProvider.UserDataProviderPort;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;

import static java.util.Objects.nonNull;

@Service
@AllArgsConstructor
public class ActiveEmailUsecase implements UsecaseVoidPort<String> {
    private final static Integer LINK_EXPIRATION_DAYS = 1;

    private final UserDataProviderPort userDataProviderPort;

    @Override
    public void execute(String input) {
        User user = findUserByEmailToken(input);

        if (!user.getActive()) {
            throw new UserNotActiveException();
        }

        if (nonNull(user.getEmailConfirmedAt())) {
            throw new UserEmailAlreadyActiveException();
        }

        validateTokenSentAtLimit(user.getEmailTokenSentAt());

        user.setEmailConfirmedAt(new Date());
        persistUser(user);
    }

    private User findUserByEmailToken(String email) {
        return userDataProviderPort.findByEmailToken(email).orElseThrow(UserNotFoundException::new);
    }

    private void validateTokenSentAtLimit(Date tokenSentAt) {
        Calendar limit = Calendar.getInstance();
        limit.add(Calendar.DAY_OF_MONTH, -LINK_EXPIRATION_DAYS);

        if (tokenSentAt.before(limit.getTime())) {
            throw new EmailTokenExpiredException();
        }
    }

    private void persistUser(User user) {
        userDataProviderPort.persist(user);
    }
}
