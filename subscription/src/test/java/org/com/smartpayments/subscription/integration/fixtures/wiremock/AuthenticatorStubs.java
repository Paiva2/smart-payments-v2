package org.com.smartpayments.subscription.integration.fixtures.wiremock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.com.smartpayments.subscription.core.domain.model.User;
import org.com.smartpayments.subscription.core.ports.out.external.dto.UserAuthenticatorOutput;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

public class AuthenticatorStubs {
    private final static String authenticateUserPath = "/api/authenticator/user/internal";

    public static void mockAuthenticateUser(WireMockServer server, ObjectMapper objectMapper, User user) throws Exception {
        server.stubFor(WireMock.get(urlEqualTo(authenticateUserPath))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(objectMapper.writeValueAsString(UserAuthenticatorOutput.builder()
                        .id(user.getId())
                        .active(true)
                        .passwordHash("any_hash")
                        .roles(List.of("MEMBER"))
                        .build()
                    )
                )
            )
        );
    }
}
