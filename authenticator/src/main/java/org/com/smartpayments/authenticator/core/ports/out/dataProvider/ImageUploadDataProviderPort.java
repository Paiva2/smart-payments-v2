package org.com.smartpayments.authenticator.core.ports.out.dataProvider;

import org.springframework.web.multipart.MultipartFile;

public interface ImageUploadDataProviderPort {
    String uploadImage(String destination, String key, MultipartFile image);
}
