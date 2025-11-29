package org.com.smartpayments.authenticator.integration.core.domain.usecase.user.userProfile;

import org.com.smartpayments.authenticator.core.domain.enums.ERole;
import org.com.smartpayments.authenticator.core.domain.model.User;
import org.com.smartpayments.authenticator.core.ports.out.dataProvider.ImageUploadDataProviderPort;
import org.com.smartpayments.authenticator.integration.fixtures.bases.IntegrationTestBase;
import org.com.smartpayments.authenticator.integration.utils.UserUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;

import static org.com.smartpayments.authenticator.core.common.constants.Constants.DEFAULT_FILE_NAME_PROFILE_PICTURE;
import static org.com.smartpayments.authenticator.core.common.constants.Constants.UPLOAD_PROFILE_IMAGE_PATH;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserProfileIntegrationTest extends IntegrationTestBase {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserUtils userUtils;

    @Autowired
    private ImageUploadDataProviderPort imageUploadDataProviderPort;

    @Value("${server.api-suffix}")
    private String apiSuffix;

    private User user;

    private String authToken;

    @BeforeEach
    public void beforeEach() {
        user = userUtils.createUser(ERole.MEMBER, true, true);
        authToken = "Bearer " + userUtils.generateAuthToken(user.getId());
    }

    @Test
    @DisplayName("Should get user profile")
    public void getUserProfile() throws Exception {
        mockMvc.perform(get(apiSuffix + "/user/profile")
                .header("Authorization", authToken)
            ).andExpect(status().isOk())
            .andExpect(jsonPath("$.id").isNotEmpty())
            .andExpect(jsonPath("$.id").value(user.getId()))
            .andExpect(jsonPath("$.email").value(user.getEmail()))
            .andExpect(jsonPath("$.firstName").value(user.getFirstName()))
            .andExpect(jsonPath("$.lastName").value(user.getLastName()))
            .andExpect(jsonPath("$.cpfCnpj").value(user.getCpfCnpj()))
            .andExpect(jsonPath("$.active").isNotEmpty())
            .andExpect(jsonPath("$.emailConfirmedAt").isNotEmpty())
            .andExpect(jsonPath("$.address").isNotEmpty())
            .andExpect(jsonPath("$.roles").isNotEmpty());
    }

    @Test
    @DisplayName("Should get user profile with profile picture url")
    public void getUserProfileWithProfilePictureUrl() throws Exception {
        File file = new ClassPathResource("static/images/dummy-image.png").getFile();
        MultipartFile multipartFile = new MockMultipartFile(file.getName(), new FileInputStream(file));

        imageUploadDataProviderPort.uploadImage(
            this.testBucketName,
            UPLOAD_PROFILE_IMAGE_PATH,
            user.getId().toString(),
            DEFAULT_FILE_NAME_PROFILE_PICTURE,
            5,
            multipartFile
        );

        mockMvc.perform(get(apiSuffix + "/user/profile")
                .header("Authorization", authToken)
            ).andExpect(status().isOk())
            .andExpect(jsonPath("$.profilePictureUrl").isNotEmpty());
    }
}
