package org.com.smartpayments.authenticator.core.domain.usecase.user.updateUserProfile;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.smartpayments.authenticator.core.common.exception.GenericCpfCnpjInvalidException;
import org.com.smartpayments.authenticator.core.common.exception.GenericException;
import org.com.smartpayments.authenticator.core.common.exception.UserEmailNotActiveException;
import org.com.smartpayments.authenticator.core.common.exception.UserNotFoundException;
import org.com.smartpayments.authenticator.core.domain.enums.EUserType;
import org.com.smartpayments.authenticator.core.domain.model.User;
import org.com.smartpayments.authenticator.core.ports.in.UsecasePort;
import org.com.smartpayments.authenticator.core.ports.in.dto.UpdateUserProfileInput;
import org.com.smartpayments.authenticator.core.ports.out.dataProvider.UserDataProviderPort;
import org.com.smartpayments.authenticator.core.ports.out.dto.UserProfileOutput;
import org.com.smartpayments.authenticator.core.ports.out.utils.PersonalDocumentUtilsPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import static java.util.Objects.isNull;
import static org.springframework.util.ObjectUtils.isEmpty;

@Slf4j
@Service
@AllArgsConstructor
public class UpdateUserProfileUsecase implements UsecasePort<UpdateUserProfileInput, UserProfileOutput> {
    private final UserDataProviderPort userDataProviderPort;

    private final PersonalDocumentUtilsPort personalDocumentUtilsPort;

    @Override
    @Transactional
    public UserProfileOutput execute(UpdateUserProfileInput input) {
        User user = findUser(input.getUserId());

        if (isNull(user.getEmailConfirmedAt())) {
            throw new UserEmailNotActiveException();
        }

        updateUser(input, user);
        user = persistUser(user);

        return user.toProfileOutput();
    }

    private User findUser(Long userId) {
        return userDataProviderPort.findActiveById(userId).orElseThrow(UserNotFoundException::new);
    }

    private Date parseDate(String birthdate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            return sdf.parse(birthdate);
        } catch (Exception e) {
            String message = "Error while changing birthdate!";
            log.error("{} {}", message, e.getMessage());
            throw new GenericException(message);
        }
    }

    private void validateDocument(UpdateUserProfileInput input) {
        if (Objects.equals(input.getType(), EUserType.NATURAL) && !personalDocumentUtilsPort.isValidCpf(input.getCpfCnpj())) {
            throw new GenericCpfCnpjInvalidException("Invalid Cpf!");
        }

        if (Objects.equals(input.getType(), EUserType.LEGAL) && !personalDocumentUtilsPort.isValidCnpj(input.getCpfCnpj())) {
            throw new GenericCpfCnpjInvalidException("Invalid Cnpj!");
        }
    }

    private void updateUser(UpdateUserProfileInput input, User user) {
        user.setFirstName(isEmpty(input.getFirstName()) ? user.getFirstName() : input.getFirstName());
        user.setLastName(isEmpty(input.getLastName()) ? user.getLastName() : input.getLastName());
        user.setBirthdate(isEmpty(input.getBirthdate()) ? user.getBirthdate() : parseDate(input.getBirthdate()));
        user.setType(isEmpty(input.getType()) ? user.getType() : input.getType());
        user.setPhone(isEmpty(input.getPhone()) ? user.getPhone() : input.getPhone());

        if (!isEmpty(input.getCpfCnpj()) && !Objects.equals(input.getCpfCnpj().replaceAll("\\D", ""), user.getCpfCnpj())) {
            user.setCpfCnpj(input.getCpfCnpj());
        }

        validateDocument(input);

        updateAddress(input, user);
    }

    private void updateAddress(UpdateUserProfileInput input, User user) {
        user.getAddress().setStreet(isEmpty(input.getAddress().getStreet()) ? user.getAddress().getStreet() : input.getAddress().getStreet());
        user.getAddress().setNeighborhood(isEmpty(input.getAddress().getNeighborhood()) ? user.getAddress().getNeighborhood() : input.getAddress().getNeighborhood());
        user.getAddress().setNumber(isEmpty(input.getAddress().getNumber()) ? user.getAddress().getNumber() : input.getAddress().getNumber());
        user.getAddress().setZipcode(isEmpty(input.getAddress().getZipcode()) ? user.getAddress().getZipcode() : input.getAddress().getZipcode());
        user.getAddress().setCity(isEmpty(input.getAddress().getCity()) ? user.getAddress().getCity() : input.getAddress().getCity());
        user.getAddress().setState(isEmpty(input.getAddress().getState()) ? user.getAddress().getState() : input.getAddress().getState());
        user.getAddress().setComplement(isNull(input.getAddress().getComplement()) ? user.getAddress().getComplement() : input.getAddress().getComplement());
    }

    private User persistUser(User user) {
        return userDataProviderPort.persist(user);
    }
}
