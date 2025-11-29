package org.com.smartpayments.authenticator.integration.fixtures.bases;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;

import java.util.Random;

import static org.com.smartpayments.authenticator.core.common.constants.Constants.UPLOAD_PROFILE_IMAGE_PATH;
import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

@AutoConfigureMockMvc
@SpringBootTest(
    properties = "spring.config.location=classpath:/application-test.yml",
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public abstract class IntegrationTestBase {
    protected final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private Flyway flyway;

    @Autowired
    private S3Client s3Client;

    @Value("${file.image.upload.bucket_name}")
    protected String testBucketName;

    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
        .withDatabaseName("auth-db")
        .withUsername("test")
        .withPassword("test")
        .withCreateContainerCmdModifier(cmd -> cmd.withName("auth-db-pg-" + (new Random().nextInt() + Integer.MAX_VALUE)));

    public static final ConfluentKafkaContainer kafka = new ConfluentKafkaContainer("confluentinc/cp-kafka:7.8.0")
        .withCreateContainerCmdModifier(cmd -> cmd.withName("smart-payments-kafka-authenticator-" + (new Random().nextInt() + Integer.MAX_VALUE)));

    public static final LocalStackContainer localstack = new LocalStackContainer(DockerImageName.parse("localstack/localstack"))
        .withServices(S3)
        .withCreateContainerCmdModifier(cmd -> cmd.withName("auth-localstack-" + (new Random().nextInt() + Integer.MAX_VALUE)));

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);

        registry.add("aws.s3.endpoint", () -> localstack.getEndpointOverride(S3).toString());
        registry.add("aws.s3.region", localstack::getRegion);
        registry.add("aws.s3.access-key", localstack::getAccessKey);
        registry.add("aws.s3.secret-key", localstack::getSecretKey);
    }

    @BeforeAll
    static void beforeAll() {
        postgres.start();
        kafka.start();
        localstack.start();
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
        kafka.stop();
        localstack.stop();
    }

    @BeforeEach
    void setUp() {
        flyway.clean();
        flyway.migrate();

        s3Client.createBucket(CreateBucketRequest.builder()
            .bucket(testBucketName)
            .build()
        );
    }

    @AfterEach
    void tearDown() {
        ListObjectsV2Response list = s3Client.listObjectsV2(
            ListObjectsV2Request.builder()
                .bucket(testBucketName)
                .prefix(String.format("%s/%d", UPLOAD_PROFILE_IMAGE_PATH, 1L))
                .build()
        );

        list.contents().forEach(obj ->
            s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(testBucketName)
                .key(obj.key())
                .build()
            )
        );

        s3Client.deleteBucket(DeleteBucketRequest.builder()
            .bucket(testBucketName)
            .build()
        );
    }
}