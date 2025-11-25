package org.com.smartpayments.authenticator.core.domain.usecase.user.userProfile;

import lombok.RequiredArgsConstructor;
import org.com.smartpayments.authenticator.core.common.exception.UserNotFoundException;
import org.com.smartpayments.authenticator.core.domain.model.User;
import org.com.smartpayments.authenticator.core.ports.in.UsecasePort;
import org.com.smartpayments.authenticator.core.ports.out.dataProvider.ImageUploadDataProviderPort;
import org.com.smartpayments.authenticator.core.ports.out.dataProvider.UserDataProviderPort;
import org.com.smartpayments.authenticator.core.ports.out.dto.UserProfileOutput;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import static org.com.smartpayments.authenticator.core.common.constants.Constants.PROFILE_PICTURE_PRESIGNED_URL_EXP_DAYS;
import static org.com.smartpayments.authenticator.core.common.constants.Constants.USER_PROFILE_CACHE_LABEL;

@Component
@RequiredArgsConstructor
public class UserProfileUsecase implements UsecasePort<Long, UserProfileOutput> {
    private final static String UPLOAD_IMAGE_PATH = "user-profile-picture";

    private final UserDataProviderPort userDataProviderPort;
    private final ImageUploadDataProviderPort imageUploadDataProviderPort;

    @Value("${file.image.upload.bucket_name}")
    private String profileImageDestination;

    @Override
    @Cacheable(value = USER_PROFILE_CACHE_LABEL, key = "#input")
    public UserProfileOutput execute(Long input) {
        User user = userDataProviderPort.findActiveByIdWithDependencies(input)
            .orElseThrow(UserNotFoundException::new);

        user.setProfilePictureUrl(findProfilePictureUrl(user.getId()));

        return user.toProfileOutput();
    }

    private String findProfilePictureUrl(Long userId) {
        String key = String.format("%s/%s", UPLOAD_IMAGE_PATH, userId);
        return imageUploadDataProviderPort.findMostRecentFromDestination(profileImageDestination, key, PROFILE_PICTURE_PRESIGNED_URL_EXP_DAYS);
    }
}
