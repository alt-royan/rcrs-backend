package org.ultra.rcrs.userservice.integration;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.ultra.rcrs.kafka.Topics;
import org.ultra.rcrs.userservice.repository.ProcessedEventRepository;
import org.ultra.rcrs.userservice.repository.UserRepository;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, topics = {
        Topics.IDENTITY_EVENTS_TOPIC
})
@DirtiesContext
public abstract class BaseIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16")
                    .withDatabaseName("postgres")
                    .withUsername("postgres")
                    .withPassword("postgres");

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add(
                "spring.datasource.url",
                postgres::getJdbcUrl
        );
        registry.add(
                "spring.datasource.username",
                postgres::getUsername
        );
        registry.add(
                "spring.datasource.password",
                postgres::getPassword
        );
        registry.add(
                "spring.liquibase.url",
                postgres::getJdbcUrl
        );
        registry.add(
                "spring.liquibase.user",
                postgres::getUsername
        );
        registry.add(
                "spring.liquibase.password",
                postgres::getPassword
        );
        registry.add(
                "spring.kafka.bootstrap-servers",
                () -> System.getProperty("spring.embedded.kafka.brokers")
        );
    }

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected ProcessedEventRepository processedEventRepository;

    @Autowired
    protected KafkaTemplate<String, String> kafkaTemplate;

    protected final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void cleanDb() {
        processedEventRepository.deleteAll();
        userRepository.deleteAll();
    }

    protected String buildKeycloakEvent(String id, String type, String userId,
                                        Map<String, String> details) throws Exception {
        Map<String, Object> event = Map.of(
                "id", id,
                "time", System.currentTimeMillis(),
                "type", type,
                "realmId", "master",
                "clientId", "rcrs-client",
                "userId", userId,
                "sessionId", UUID.randomUUID().toString(),
                "ipAddress", "127.0.0.1",
                "details", details != null ? details : Map.of()
        );
        return objectMapper.writeValueAsString(event);
    }

    protected void sendKeycloakEvent(String id, String type, String userId,
                                     Map<String, String> details) throws Exception {
        String json = buildKeycloakEvent(id, type, userId, details);
        kafkaTemplate.send(Topics.IDENTITY_EVENTS_TOPIC, userId, json).get();
    }

    protected void sendRegisterEvent(String keycloakId, String username, String email,
                                     String firstName, String lastName) throws Exception {
        sendKeycloakEvent(UUID.randomUUID().toString(), "REGISTER", keycloakId, Map.of(
                "preferred_username", username,
                "email", email,
                "first_name", firstName,
                "last_name", lastName,
                "email_verified", "true"
        ));
    }

    protected void sendUpdateProfileEvent(String keycloakId, String username, String email,
                                          String firstName, String lastName) throws Exception {
        sendKeycloakEvent(UUID.randomUUID().toString(), "UPDATE_PROFILE", keycloakId, Map.of(
                "preferred_username", username,
                "email", email,
                "first_name", firstName,
                "last_name", lastName,
                "email_verified", "true"
        ));
    }

    protected void sendDeleteAccountEvent(String keycloakId) throws Exception {
        sendKeycloakEvent(UUID.randomUUID().toString(), "DELETE_ACCOUNT", keycloakId, Map.of());
    }

    protected void waitForProcessing() throws Exception {
        Thread.sleep(3000);
    }
}
