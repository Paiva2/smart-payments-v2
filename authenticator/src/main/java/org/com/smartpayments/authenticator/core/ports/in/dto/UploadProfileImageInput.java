package org.com.smartpayments.authenticator.core.ports.in.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadProfileImageInput {
    private Long userId;

    private MultipartFile profileImage;
}
