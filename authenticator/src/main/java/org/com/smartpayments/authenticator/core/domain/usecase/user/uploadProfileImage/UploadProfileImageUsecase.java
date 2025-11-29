package org.com.smartpayments.authenticator.core.domain.usecase.user.uploadProfileImage;

import lombok.RequiredArgsConstructor;
import org.com.smartpayments.authenticator.core.common.exception.UserNotFoundException;
import org.com.smartpayments.authenticator.core.domain.model.User;
import org.com.smartpayments.authenticator.core.domain.usecase.user.uploadProfileImage.exception.InvalidProfilePictureImageException;
import org.com.smartpayments.authenticator.core.ports.in.UsecasePort;
import org.com.smartpayments.authenticator.core.ports.in.dto.UploadProfileImageInput;
import org.com.smartpayments.authenticator.core.ports.out.dataProvider.ImageUploadDataProviderPort;
import org.com.smartpayments.authenticator.core.ports.out.dataProvider.UserDataProviderPort;
import org.com.smartpayments.authenticator.core.ports.out.dto.UploadProfileImageOutput;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import static java.util.Objects.isNull;
import static org.com.smartpayments.authenticator.core.common.constants.Constants.*;
import static org.springframework.util.ObjectUtils.isEmpty;

@Service
@RequiredArgsConstructor
public class UploadProfileImageUsecase implements UsecasePort<UploadProfileImageInput, UploadProfileImageOutput> {
    private static final Integer FIVE_DAYS_IN_MINUTES_EXP_PRESIGNED_URL = 7200;

    private final UserDataProviderPort userDataProviderPort;
    private final ImageUploadDataProviderPort imageUploadDataProviderPort;

    @Value("${file.image.upload.bucket_name}")
    private String profileImageDestination;

    @Override
    @CacheEvict(value = USER_PROFILE_CACHE_LABEL, key = "#input.userId")
    public UploadProfileImageOutput execute(UploadProfileImageInput input) {
        User user = findUser(input.getUserId());

        checkIsImage(input.getProfileImage());

        String url = imageUploadDataProviderPort.uploadImage(
            profileImageDestination,
            UPLOAD_PROFILE_IMAGE_PATH,
            user.getId().toString(),
            DEFAULT_FILE_NAME_PROFILE_PICTURE,
            FIVE_DAYS_IN_MINUTES_EXP_PRESIGNED_URL,
            input.getProfileImage()
        );

        return new UploadProfileImageOutput(url);
    }

    private User findUser(Long userId) {
        return userDataProviderPort.findActiveById(userId).orElseThrow(UserNotFoundException::new);
    }

    private void checkIsImage(MultipartFile file) {
        if (isNull(file) || isEmpty(file.getContentType()) || !file.getContentType().startsWith("image/")) {
            throw new InvalidProfilePictureImageException("Invalid profile image file format!");
        }
    }
}
