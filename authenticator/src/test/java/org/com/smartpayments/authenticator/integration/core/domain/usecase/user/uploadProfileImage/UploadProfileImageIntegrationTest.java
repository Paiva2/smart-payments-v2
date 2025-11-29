package org.com.smartpayments.authenticator.integration.core.domain.usecase.user.uploadProfileImage;

import org.com.smartpayments.authenticator.core.domain.enums.ERole;
import org.com.smartpayments.authenticator.core.domain.model.User;
import org.com.smartpayments.authenticator.integration.fixtures.bases.IntegrationTestBase;
import org.com.smartpayments.authenticator.integration.utils.UserUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.io.FileInputStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UploadProfileImageIntegrationTest extends IntegrationTestBase {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserUtils userUtils;

    @Value("${server.api-suffix}")
    private String apiSuffix;

    private String authToken;

    @BeforeEach
    public void beforeEach() {
        User user = userUtils.createUser(ERole.MEMBER, true, true);

        authToken = "Bearer " + userUtils.generateAuthToken(user.getId());
    }

    @Test
    @DisplayName("Should upload the profile image")
    public void uploadProfileImage() throws Exception {
        File file = new ClassPathResource("static/images/dummy-image.png").getFile();
        MockMultipartFile multipartFile = new MockMultipartFile(
            "file",
            "dummy-image.png",
            MediaType.IMAGE_PNG_VALUE,
            new FileInputStream(file)
        );

        String url = apiSuffix + "/user/profile_image";

        mockMvc.perform(multipart(url)
                .file(multipartFile)
                .header("Authorization", authToken)
            ).andExpect(status().isOk())
            .andExpect(jsonPath("$.url").isNotEmpty());
    }
}
