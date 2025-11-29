package org.com.smartpayments.authenticator.integration.core.domain.usecase.user.registerUser;

import com.github.javafaker.Faker;
import org.com.smartpayments.authenticator.core.domain.enums.EBrState;
import org.com.smartpayments.authenticator.core.domain.enums.ERole;
import org.com.smartpayments.authenticator.core.domain.enums.EUserType;
import org.com.smartpayments.authenticator.core.domain.model.Address;
import org.com.smartpayments.authenticator.core.domain.model.User;
import org.com.smartpayments.authenticator.core.domain.model.UserRole;
import org.com.smartpayments.authenticator.core.ports.in.dto.RegisterUserInput;
import org.com.smartpayments.authenticator.infra.persistence.repository.AddressRepository;
import org.com.smartpayments.authenticator.infra.persistence.repository.UserRepository;
import org.com.smartpayments.authenticator.infra.persistence.repository.UserRoleRepository;
import org.com.smartpayments.authenticator.integration.fixtures.bases.IntegrationTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.com.smartpayments.authenticator.integration.constants.TestConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RegisterUserIntegrationTest extends IntegrationTestBase {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @MockitoSpyBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value("${spring.kafka.topics.mail-sender}")
    private String SEND_EMAIL_TOPIC;

    @Value("${server.api-suffix}")
    private String apiSuffix;

    @Test
    @DisplayName("Should register an user")
    public void registerUser() throws Exception {
        RegisterUserInput input = input();

        mockMvc.perform(post(apiSuffix + "/user/register").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(input))
        ).andExpect(status().isCreated());

        Optional<User> user = userRepository.findByEmail(input.getEmail());

        assertTrue(user.isPresent());
        assertEquals(input.getEmail(), user.get().getEmail());
        assertNotNull(user.get().getId());
        assertNotNull(user.get().getEmailToken());
        assertNotNull(user.get().getEmailTokenSentAt());

        Optional<Address> address = addressRepository.findByUserId(user.get().getId());

        assertTrue(address.isPresent());
        assertNotNull(address.get().getId());
        assertEquals(input.getAddress().getStreet(), address.get().getStreet());

        List<UserRole> userRoles = userRoleRepository.findByUserId(user.get().getId());

        assertFalse(userRoles.isEmpty());
        assertNotNull(userRoles.getFirst());
        assertEquals(ERole.MEMBER, userRoles.getFirst().getRole().getName());

        verify(kafkaTemplate, times(1)).send(eq(SEND_EMAIL_TOPIC), anyString());
    }

    @Test
    @DisplayName("Should verify e-mail format")
    public void registerUserEmailFormat() throws Exception {
        RegisterUserInput input = input();
        input.setEmail("invalid_email");

        mockMvc.perform(post(apiSuffix + "/user/register").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(input))
        ).andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Should verify password format")
    public void registerUserPasswordFormat() throws Exception {
        RegisterUserInput input = input();
        input.setPassword("invalid_password");

        mockMvc.perform(post(apiSuffix + "/user/register").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input))
            ).andExpect(status().is4xxClientError())
            .andExpect(jsonPath("$.message.password[0]").value("Password must have one special character, one upper letter and at least 6 characters!"));
    }

    @Test
    @DisplayName("Should verify phone length")
    public void registerUserPhoneLength() throws Exception {
        RegisterUserInput input = input();
        input.setPhone("1");

        mockMvc.perform(post(apiSuffix + "/user/register").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input))
            ).andExpect(status().is4xxClientError())
            .andExpect(jsonPath("$.message").value("Invalid phone. Phone must have 11 digits!"));
    }

    @Test
    @DisplayName("Should throw error if user has less than 18yo")
    public void registerUserBirthdateError() throws Exception {
        RegisterUserInput input = input();
        input.setBirthdate(new SimpleDateFormat("dd-MM-yyyy").format(new Date()));

        mockMvc.perform(post(apiSuffix + "/user/register").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input))
            ).andExpect(status().is4xxClientError())
            .andExpect(jsonPath("$.message").value("Invalid birthdate. User must have at least 18 years old!"));
    }

    @Test
    @DisplayName("Should throw error if provided cpf is invalid")
    public void registerUserCpfError() throws Exception {
        RegisterUserInput input = input();
        input.setCpfCnpj("invalid_cpf");

        mockMvc.perform(post(apiSuffix + "/user/register").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input))
            ).andExpect(status().is4xxClientError())
            .andExpect(jsonPath("$.message").value("Invalid document. Invalid Cpf!"));
    }

    @Test
    @DisplayName("Should throw error if provided cnpj is invalid")
    public void registerUserCnpjError() throws Exception {
        RegisterUserInput input = input();
        input.setCpfCnpj("invalid_cnpj");
        input.setType(EUserType.LEGAL);

        mockMvc.perform(post(apiSuffix + "/user/register").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input))
            ).andExpect(status().is4xxClientError())
            .andExpect(jsonPath("$.message").value("Invalid document. Invalid Cnpj!"));
    }

    private RegisterUserInput input() {
        Faker faker = new Faker();

        return RegisterUserInput.builder()
            .firstName(faker.name().fullName())
            .lastName(faker.name().lastName())
            .email(faker.internet().emailAddress())
            .birthdate(RANDOM_VALID_BIRTHDATE)
            .password(RANDOM_VALID_PASSWORD)
            .cpfCnpj(RANDOM_VALID_CPF)
            .type(EUserType.NATURAL)
            .phone(RANDOM_VALID_PHONE_NUMBER)
            .address(RegisterUserInput.Address.builder()
                .street(faker.address().streetName())
                .neighborhood("test_neighborhood")
                .number(faker.address().streetAddressNumber())
                .zipcode(faker.numerify("#####-###"))
                .city(faker.address().city())
                .state(EBrState.SP)
                .complement("test_complement")
                .build()
            ).build();
    }

}