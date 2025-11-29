package org.com.smartpayments.authenticator.integration.fixtures.bases;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.com.smartpayments.authenticator.config.containers.Containers;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;

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

    static {
        if (!Containers.POSTGRES.isRunning()) {
            Containers.POSTGRES.start();
        }

        if (!Containers.KAFKA.isRunning()) {
            Containers.KAFKA.start();
        }

        if (!Containers.LOCALSTACK.isRunning()) {
            Containers.LOCALSTACK.start();
        }
    }

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", Containers.POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", Containers.POSTGRES::getUsername);
        registry.add("spring.datasource.password", Containers.POSTGRES::getPassword);

        registry.add("spring.kafka.bootstrap-servers", Containers.KAFKA::getBootstrapServers);

        registry.add("aws.s3.endpoint", () -> Containers.LOCALSTACK.getEndpointOverride(S3).toString());
        registry.add("aws.s3.region", Containers.LOCALSTACK::getRegion);
        registry.add("aws.s3.access-key", Containers.LOCALSTACK::getAccessKey);
        registry.add("aws.s3.secret-key", Containers.LOCALSTACK::getSecretKey);
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