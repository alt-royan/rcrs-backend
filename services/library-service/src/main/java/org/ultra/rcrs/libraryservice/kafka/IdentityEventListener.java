package org.ultra.rcrs.libraryservice.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.ultra.rcrs.kafka.Topics;
import org.ultra.rcrs.libraryservice.dto.KeycloakRawEvent;
import org.ultra.rcrs.libraryservice.service.LibraryPurgeService;

/**
 * Drops a user's whole library when their account is deleted.
 * <p>
 * This topic carries Keycloak's own JSON rather than the protobuf envelope every
 * other topic uses, so it needs the string container factory.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IdentityEventListener {

    private static final String ACCOUNT_DELETED = "DELETE_ACCOUNT";

    private final ObjectMapper objectMapper;
    private final LibraryPurgeService libraryPurgeService;

    @KafkaListener(
            topics = Topics.IDENTITY_EVENTS_TOPIC,
            groupId = "library-service-group",
            containerFactory = "stringContainerFactory")
    public void handleIdentityEvent(ConsumerRecord<String, String> record) {
        KeycloakRawEvent event;
        try {
            event = objectMapper.readValue(record.value(), KeycloakRawEvent.class);
        } catch (Exception ex) {
            // Malformed input will never parse, so retrying it would only block the
            // partition. Log and move on rather than letting it reach the DLQ forever.
            log.error("Skipping unparseable identity event: {}", ex.getMessage(), ex);
            return;
        }

        if (!ACCOUNT_DELETED.equals(event.getType())) {
            return;
        }
        if (event.getUserId() == null) {
            log.warn("Ignoring {} event {} with no userId", ACCOUNT_DELETED, event.getId());
            return;
        }

        // Deliberately not caught: a purge that fails is retried and then dead-lettered
        // by the shared error handler, because silently dropping it would leave a
        // deleted user's data behind.
        libraryPurgeService.purge(event.getUserId());
    }
}
