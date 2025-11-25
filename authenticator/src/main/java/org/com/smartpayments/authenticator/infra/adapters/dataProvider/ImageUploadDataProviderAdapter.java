package org.com.smartpayments.authenticator.infra.adapters.dataProvider;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.com.smartpayments.authenticator.core.common.exception.GenericException;
import org.com.smartpayments.authenticator.core.ports.out.dataProvider.ImageUploadDataProviderPort;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.InputStream;
import java.time.Duration;
import java.util.List;

import static java.util.Objects.isNull;

@Slf4j
@Component
@AllArgsConstructor
public class ImageUploadDataProviderAdapter implements ImageUploadDataProviderPort {
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Override
    public String uploadImage(String destination, String path, String key, String fileName, int urlExpInDays, MultipartFile image) {
        try {
            InputStream is = image.getInputStream();
            String fileNameToUse = isNull(fileName) ? image.getOriginalFilename() : fileName;
            String uploadKey = generateKey(path, fileNameToUse, key);

            PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(destination)
                .key(uploadKey)
                .contentLength(image.getSize())
                .contentType(image.getContentType())
                .build();

            s3Client.putObject(objectRequest, RequestBody.fromInputStream(is, image.getSize()));

            return generatePresignedUrl(destination, uploadKey, urlExpInDays);
        } catch (Exception e) {
            String message = "Error while sending image to upload provider!";
            log.error("{} {}", message, e.getMessage());
            throw new GenericException(message);
        }
    }

    @Override
    public String generatePresignedUrl(String destination, String key, int expirationMinutes) {
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

    @Override
    public String findMostRecentFromDestination(String destination, String key, int expirationMinutes) {
        ListObjectsV2Request objectsV2Request = ListObjectsV2Request.builder()
            .bucket(destination)
            .prefix(key)
            .maxKeys(1)
            .build();

        ListObjectsV2Response listResponse = s3Client.listObjectsV2(objectsV2Request);

        List<S3Object> objects = listResponse.contents();

        if (!objects.isEmpty()) {
            return generatePresignedUrl(destination, objects.getFirst().key(), expirationMinutes);
        }

        return "";
    }

    private String generateKey(String path, String fileName, String key) {
        String randomSha = DigestUtils.md5Hex(key + fileName);
        return String.format("%s/%s/%s", path, key, randomSha);
    }
}
