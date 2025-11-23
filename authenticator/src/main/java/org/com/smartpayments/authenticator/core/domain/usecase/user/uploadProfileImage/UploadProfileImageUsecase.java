package org.com.smartpayments.authenticator.core.domain.usecase.user.uploadProfileImage;

import lombok.RequiredArgsConstructor;
import org.com.smartpayments.authenticator.core.common.exception.UserNotFoundException;
import org.com.smartpayments.authenticator.core.domain.model.User;
import org.com.smartpayments.authenticator.core.domain.usecase.user.uploadProfileImage.exception.InvalidProfilePictureImageException;
import org.com.smartpayments.authenticator.core.ports.in.UsecaseVoidPort;
import org.com.smartpayments.authenticator.core.ports.in.dto.UploadProfileImageInput;
import org.com.smartpayments.authenticator.core.ports.out.dataProvider.ImageUploadDataProviderPort;
import org.com.smartpayments.authenticator.core.ports.out.dataProvider.UserDataProviderPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import static java.util.Objects.isNull;
import static org.springframework.util.ObjectUtils.isEmpty;

@Service
@RequiredArgsConstructor
public class UploadProfileImageUsecase implements UsecaseVoidPort<UploadProfileImageInput> {
    private final UserDataProviderPort userDataProviderPort;
    private final ImageUploadDataProviderPort imageUploadDataProviderPort;

    @Value("${file.image.upload.bucket_name}")
    private String profileImageDestination;

    @Override
    public void execute(UploadProfileImageInput input) {
        User user = findUser(input.getUserId());

        checkIsImage(input.getProfileImage());

        imageUploadDataProviderPort.uploadImage(profileImageDestination, user.getId().toString(), input.getProfileImage());
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
