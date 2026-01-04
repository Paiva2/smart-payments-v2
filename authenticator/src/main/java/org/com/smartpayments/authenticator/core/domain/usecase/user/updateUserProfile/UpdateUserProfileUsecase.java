package org.com.smartpayments.authenticator.core.domain.usecase.user.updateUserProfile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.com.smartpayments.authenticator.core.common.exception.GenericCpfCnpjInvalidException;
import org.com.smartpayments.authenticator.core.common.exception.GenericException;
import org.com.smartpayments.authenticator.core.common.exception.UserEmailNotActiveException;
import org.com.smartpayments.authenticator.core.common.exception.UserNotFoundException;
import org.com.smartpayments.authenticator.core.domain.enums.EUserType;
import org.com.smartpayments.authenticator.core.domain.model.User;
import org.com.smartpayments.authenticator.core.ports.in.UsecasePort;
import org.com.smartpayments.authenticator.core.ports.in.dto.UpdateUserProfileInput;
import org.com.smartpayments.authenticator.core.ports.out.dataProvider.ImageUploadDataProviderPort;
import org.com.smartpayments.authenticator.core.ports.out.dataProvider.UserDataProviderPort;
import org.com.smartpayments.authenticator.core.ports.out.dto.AsyncMessageOutput;
import org.com.smartpayments.authenticator.core.ports.out.dto.AsyncUpdateUserOutput;
import org.com.smartpayments.authenticator.core.ports.out.dto.UserProfileOutput;
import org.com.smartpayments.authenticator.core.ports.out.utils.MessageUtilsPort;
import org.com.smartpayments.authenticator.core.ports.out.utils.PersonalDocumentUtilsPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

import static java.util.Objects.isNull;
import static org.com.smartpayments.authenticator.core.common.constants.Constants.PROFILE_PICTURE_PRESIGNED_URL_EXP_DAYS;
import static org.com.smartpayments.authenticator.core.common.constants.Constants.USER_PROFILE_CACHE_LABEL;
import static org.springframework.util.ObjectUtils.isEmpty;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateUserProfileUsecase implements UsecasePort<UpdateUserProfileInput, UserProfileOutput> {
    private final static ObjectMapper mapper = new ObjectMapper();

    private final static String UPLOAD_IMAGE_PATH = "user-profile-picture";

    private final UserDataProviderPort userDataProviderPort;

    private final PersonalDocumentUtilsPort personalDocumentUtilsPort;
    private final ImageUploadDataProviderPort imageUploadDataProviderPort;

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final MessageUtilsPort messageUtilsPort;

    @Value("${file.image.upload.bucket_name}")
    private String profileImageDestination;

    @Value("${spring.kafka.topics.update-user}")
    private String userUpdateTopic;

    @Override
    @Transactional
    @CacheEvict(value = USER_PROFILE_CACHE_LABEL, key = "#input.userId")
    public UserProfileOutput execute(UpdateUserProfileInput input) {
        User user = findUser(input.getUserId());

        if (isNull(user.getEmailConfirmedAt())) {
            throw new UserEmailNotActiveException();
        }

        updateUser(input, user);
        user = persistUser(user);

        user.setProfilePictureUrl(findProfilePictureUrl(user.getId()));

        UserProfileOutput output = user.toProfileOutput();

        sendMessageUpdate(output);

        return output;
    }

    private User findUser(Long userId) {
        return userDataProviderPort.findActiveById(userId).orElseThrow(UserNotFoundException::new);
    }

    private Date parseDate(String birthdate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false);
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

    private String findProfilePictureUrl(Long userId) {
        String key = String.format("%s/%s", UPLOAD_IMAGE_PATH, userId);
        return imageUploadDataProviderPort.findMostRecentFromDestination(profileImageDestination, key, PROFILE_PICTURE_PRESIGNED_URL_EXP_DAYS);
    }

    private AsyncUpdateUserOutput mountAsyncUpdateUserOutput(UserProfileOutput userProfileOutput) {
        return AsyncUpdateUserOutput.builder()
            .id(userProfileOutput.getId())
            .firstName(userProfileOutput.getFirstName())
            .lastName(userProfileOutput.getLastName())
            .email(userProfileOutput.getEmail())
            .cpfCnpj(userProfileOutput.getCpfCnpj())
            .type(userProfileOutput.getType())
            .ddi(userProfileOutput.getDdi())
            .phone(userProfileOutput.getPhone())
            .birthdate(userProfileOutput.getBirthdate())
            .active(userProfileOutput.getActive())
            .emailConfirmedAt(userProfileOutput.getEmailConfirmedAt())
            .address(AsyncUpdateUserOutput.AsyncAddressOutput.builder()
                .street(userProfileOutput.getAddress().getStreet())
                .neighborhood(userProfileOutput.getAddress().getNeighborhood())
                .number(userProfileOutput.getAddress().getNumber())
                .zipcode(userProfileOutput.getAddress().getZipcode())
                .complement(userProfileOutput.getAddress().getComplement())
                .city(userProfileOutput.getAddress().getCity())
                .state(userProfileOutput.getAddress().getState())
                .country(userProfileOutput.getAddress().getCountry()
                ).build()
            ).build();
    }

    private void sendMessageUpdate(UserProfileOutput userProfileOutput) {
        String issuer = "AUTHENTICATOR";

        try {
            AsyncUpdateUserOutput updateUser = mountAsyncUpdateUserOutput(userProfileOutput);

            AsyncMessageOutput<AsyncUpdateUserOutput> asyncMessage = new AsyncMessageOutput<>(
                messageUtilsPort.generateMessageHash(issuer),
                new Date(),
                issuer,
                updateUser
            );

            kafkaTemplate.send(userUpdateTopic, userProfileOutput.getId().toString(), mapper.writeValueAsString(asyncMessage));
        } catch (JsonProcessingException exception) {
            String message = "Error while sending user update message: {}";
            log.error(message, exception.getMessage(), exception);
            throw new GenericException("Error while updating user!");
        }
    }
}
