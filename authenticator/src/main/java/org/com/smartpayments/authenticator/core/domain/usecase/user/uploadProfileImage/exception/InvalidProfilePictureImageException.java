package org.com.smartpayments.authenticator.core.domain.usecase.user.uploadProfileImage.exception;

import org.com.smartpayments.authenticator.core.common.exception.base.BadRequestException;

public class InvalidProfilePictureImageException extends BadRequestException {
    public InvalidProfilePictureImageException(String message) {
        super(message);
    }
}
