package org.com.smartpayments.authenticator.core.domain.usecase.user.userProfile;

import lombok.AllArgsConstructor;
import org.com.smartpayments.authenticator.core.common.exception.UserNotFoundException;
import org.com.smartpayments.authenticator.core.domain.model.User;
import org.com.smartpayments.authenticator.core.ports.in.UsecasePort;
import org.com.smartpayments.authenticator.core.ports.out.dataProvider.UserDataProviderPort;
import org.com.smartpayments.authenticator.core.ports.out.dto.UserProfileOutput;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class UserProfileUsecase implements UsecasePort<Long, UserProfileOutput> {
    private final UserDataProviderPort userDataProviderPort;

    @Override
    public UserProfileOutput execute(Long input) {
        User user = userDataProviderPort.findActiveByIdWithDependencies(input)
            .orElseThrow(UserNotFoundException::new);

        return user.toProfileOutput();
    }
}
