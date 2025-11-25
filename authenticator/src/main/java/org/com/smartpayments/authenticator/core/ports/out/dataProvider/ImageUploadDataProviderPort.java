package org.com.smartpayments.authenticator.core.ports.out.dataProvider;

import org.springframework.web.multipart.MultipartFile;

public interface ImageUploadDataProviderPort {
    String uploadImage(String destination, String path, String key, String fileName, int urlExpInDays, MultipartFile image);

    String generatePresignedUrl(String destination, String key, int expirationMinutes);

    String findMostRecentFromDestination(String destination, String key, int expirationMinutes);
}
