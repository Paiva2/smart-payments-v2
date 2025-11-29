package org.com.smartpayments.authenticator.integration.core.domain.usecase.user.updateUserProfile;

import com.jayway.jsonpath.JsonPath;
import org.com.smartpayments.authenticator.core.domain.enums.ERole;
import org.com.smartpayments.authenticator.core.domain.model.User;
import org.com.smartpayments.authenticator.core.ports.in.dto.UpdateUserProfileInput;
import org.com.smartpayments.authenticator.integration.fixtures.bases.IntegrationTestBase;
import org.com.smartpayments.authenticator.integration.utils.TestDateUtils;
import org.com.smartpayments.authenticator.integration.utils.UserUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UpdateUserProfileIntegrationTest extends IntegrationTestBase {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserUtils userUtils;

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
    @DisplayName("Should update user profile infos")
    public void updateUserProfile() throws Exception {
        UpdateUserProfileInput input = input();

        mockMvc.perform(put(apiSuffix + "/user/profile")
                .header("Authorization", authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input))
            ).andExpect(status().isOk())
            .andExpect(jsonPath("$.id").isNotEmpty())
            .andExpect(jsonPath("$.id").value(user.getId()))
            .andExpect(jsonPath("$.updatedAt").isNotEmpty())
            .andExpect(jsonPath("$.cpfCnpj").value(user.getCpfCnpj())) // not updated
            .andExpect(jsonPath("$.type").value(user.getType().toString())) // not updated
            .andExpect(jsonPath("$.profilePictureUrl").isEmpty())
            .andExpect(jsonPath("$.firstName").value(input.getFirstName()))
            .andExpect(jsonPath("$.lastName").value(input.getLastName()))
            .andExpect(result -> {
                Date resultDate = TestDateUtils.convertStringDateToDate(
                    JsonPath.read(result.getResponse().getContentAsString(), "$.birthdate"),
                    "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"
                );

                assertEquals(
                    TestDateUtils.convertBirthdateToString(resultDate),
                    input.getBirthdate()
                );
            })

            .andExpect(jsonPath("$.address.street").value(input.getAddress().getStreet()))
            .andExpect(jsonPath("$.address.neighborhood").value(input.getAddress().getNeighborhood()))
            .andExpect(jsonPath("$.address.number").value(input.getAddress().getNumber()))
            .andExpect(jsonPath("$.address.zipcode").value(input.getAddress().getZipcode()))
            .andExpect(jsonPath("$.address.city").value(input.getAddress().getCity()))
            .andExpect(jsonPath("$.address.complement").value(input.getAddress().getComplement()))
            .andExpect(jsonPath("$.address.state").value(user.getAddress().getState().toString())); // not updated
    }

    @Test
    @DisplayName("Should NOT update values that are not nullable if not provided")
    public void updateUserProfileProvidedParams() throws Exception {
        UpdateUserProfileInput input = input();
        // some null
        input.setFirstName(null);
        input.setPhone(null);
        input.setBirthdate(null);
        // some empty
        input.setCpfCnpj("");
        input.getAddress().setStreet("");

        // blankable params, but if null is provided then should not change
        input.getAddress().setComplement("");

        mockMvc.perform(put(apiSuffix + "/user/profile")
                .header("Authorization", authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input))
            ).andExpect(status().isOk())
            .andExpect(jsonPath("$.id").isNotEmpty())
            .andExpect(jsonPath("$.id").value(user.getId()))
            .andExpect(jsonPath("$.updatedAt").isNotEmpty())
            .andExpect(jsonPath("$.type").value(user.getType().toString()))
            .andExpect(jsonPath("$.profilePictureUrl").isEmpty())
            .andExpect(jsonPath("$.lastName").value(input.getLastName()))

            .andExpect(jsonPath("$.address.neighborhood").value(input.getAddress().getNeighborhood()))
            .andExpect(jsonPath("$.address.number").value(input.getAddress().getNumber()))
            .andExpect(jsonPath("$.address.zipcode").value(input.getAddress().getZipcode()))
            .andExpect(jsonPath("$.address.city").value(input.getAddress().getCity()))
            .andExpect(jsonPath("$.address.state").value(user.getAddress().getState().toString()))

            // values provided as empty/null on input
            .andExpect(jsonPath("$.cpfCnpj").value(user.getCpfCnpj()))
            .andExpect(jsonPath("$.firstName").value(user.getFirstName()))
            .andExpect(jsonPath("$.phone").value(user.getPhone()))
            .andExpect(result -> {
                Date resultDate = TestDateUtils.convertStringDateToDate(
                    JsonPath.read(result.getResponse().getContentAsString(), "$.birthdate"),
                    "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"
                );

                assertEquals(
                    TestDateUtils.convertBirthdateToString(resultDate),
                    TestDateUtils.convertBirthdateToString(user.getBirthdate())
                );
            })
            .andExpect(jsonPath("$.address.street").value(user.getAddress().getStreet()))
            .andExpect(jsonPath("$.address.complement").value(""));
    }


    private UpdateUserProfileInput input() {
        return UpdateUserProfileInput.builder()
            .firstName("updated_first_name")
            .lastName("updated_last_name")
            .birthdate("1997-10-11")
            .phone("11111111111")
            .address(UpdateUserProfileInput.Address.builder()
                .street("updated_street")
                .neighborhood("updated_neighborhood")
                .number("updated_number")
                .zipcode("updated_zipcode")
                .city("updated_city")
                .complement("updated_complement")
                .build()
            ).build();
    }

}
