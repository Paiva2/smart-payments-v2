package org.com.smartpayments.subscription.integration.fixtures.bases;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.com.smartpayments.subscription.config.containers.Containers;
import org.com.smartpayments.subscription.core.ports.out.dataprovider.CacheDataProviderPort;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@AutoConfigureMockMvc
@SpringBootTest(
    properties = "spring.config.location=classpath:/application-test.yml",
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public abstract class IntegrationTestBase {
    @MockitoBean
    private CacheDataProviderPort cacheDataProviderPort;

    protected final ObjectMapper objectMapper = new ObjectMapper();
    protected final static WireMockServer wireMockServer = new WireMockServer();

    @Autowired
    private Flyway flyway;

    static {
        if (!wireMockServer.isRunning()) {
            wireMockServer.start();
        }
        if (!Containers.POSTGRES.isRunning()) {
            Containers.POSTGRES.start();
        }
        if (!Containers.KAFKA.isRunning()) {
            Containers.KAFKA.start();
        }
    }

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", Containers.POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", Containers.POSTGRES::getUsername);
        registry.add("spring.datasource.password", Containers.POSTGRES::getPassword);

        registry.add("spring.kafka.bootstrap-servers", Containers.KAFKA::getBootstrapServers);
        registry.add("spring.kafka.consumer.group-id", () -> "subscription_group-".concat(UUID.randomUUID().toString()));

        registry.add("external.payment-gateway.uri", () -> wireMockServer.baseUrl() + "/v3/");
        registry.add("external.authenticator.uri", () -> wireMockServer.baseUrl() + "/api/authenticator/");
    }

    @BeforeEach
    void setUp() {
        when(cacheDataProviderPort.existsByKey(anyString())).thenReturn(false);
        doNothing().when(cacheDataProviderPort).persist(anyString(), anyString(), any());

        flyway.clean();
        flyway.migrate();
    }
}