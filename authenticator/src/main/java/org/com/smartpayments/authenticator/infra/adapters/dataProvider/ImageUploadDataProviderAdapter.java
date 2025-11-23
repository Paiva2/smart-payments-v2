package org.com.smartpayments.authenticator.infra.adapters.dataProvider;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.com.smartpayments.authenticator.core.common.exception.GenericException;
import org.com.smartpayments.authenticator.core.ports.out.dataProvider.ImageUploadDataProviderPort;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.InputStream;
import java.time.Duration;

@Slf4j
@Component
@AllArgsConstructor
public class ImageUploadDataProviderAdapter implements ImageUploadDataProviderPort {
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Override
    public String uploadImage(String destination, String key, MultipartFile image) {
        try {
            InputStream is = image.getInputStream();
            String fileExtension = FilenameUtils.getExtension(image.getOriginalFilename());
            String uploadKey = generateKey("user-profile-picture", image.getOriginalFilename(), key, fileExtension);
            final int fiveDaysInMinutesToPresignUrl = 7200;

            PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(destination)
                .key(uploadKey)
                .contentLength(image.getSize())
                .contentType(image.getContentType())
                .build();

            s3Client.putObject(objectRequest, RequestBody.fromInputStream(is, image.getSize()));

            return generatePresignedUrl(destination, uploadKey, fiveDaysInMinutesToPresignUrl);
        } catch (Exception e) {
            String message = "Error while sending image to upload provider!";
            log.error("{} {}", message, e.getMessage());
            throw new GenericException(message);
        }
    }

    private String generatePresignedUrl(String destination, String key, int expirationMinutes) {
        GetObjectRequest objectRequest = GetObjectRequest.builder()
            .bucket(destination)
            .key(key)
            .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(expirationMinutes))
            .getObjectRequest(objectRequest)
            .build();

        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);

        return presignedRequest.url().toExternalForm();
    }

    private String generateKey(String path, String fileName, String key, String contentType) {
        String randomSha = DigestUtils.md5Hex(key + fileName);
        return String.format("%s/%s/%s.%s", path, key, randomSha, contentType);
    }
}
