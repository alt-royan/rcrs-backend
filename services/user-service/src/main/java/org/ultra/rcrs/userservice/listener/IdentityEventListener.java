package org.ultra.rcrs.userservice.listener;

import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.ultra.rcrs.kafka.Topics;
import org.ultra.rcrs.userservice.dto.IdentityEvent;
import org.ultra.rcrs.userservice.dto.KeycloakRawEvent;
import org.ultra.rcrs.userservice.mapper.KeycloakEventMapper;
import org.ultra.rcrs.userservice.model.ProcessedEvent;
import org.ultra.rcrs.userservice.repository.ProcessedEventRepository;
import org.ultra.rcrs.userservice.service.UserService;

import java.time.Instant;

@Component
@Slf4j
@RequiredArgsConstructor
public class IdentityEventListener {

    private final UserService userService;
    private final KeycloakEventMapper keycloakEventMapper;
    private final ProcessedEventRepository processedEventRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = Topics.IDENTITY_EVENTS_TOPIC, groupId = "user-service-group", containerFactory = "stringContainerFactory")
    @Transactional
    public void handleIdentityEvent(ConsumerRecord<String, String> record) {
        try {
            log.info("Received identity event: key={}", record.key());

            KeycloakRawEvent rawEvent = objectMapper.readValue(record.value(), KeycloakRawEvent.class);

            if (processedEventRepository.existsByEventId(rawEvent.getId())) {
                log.debug("Duplicate event ignored: eventId={}", rawEvent.getId());
                return;
            }

            IdentityEvent event = keycloakEventMapper.toIdentityEvent(rawEvent);
            if (event == null) {
                log.debug("Unmapped event type ignored: type={}", rawEvent.getType());
                return;
            }

            userService.handleEvent(event);

            processedEventRepository.save(ProcessedEvent.builder()
                    .eventId(rawEvent.getId())
                    .eventType(rawEvent.getType())
                    .processedAt(Instant.now())
                    .build());

        } catch (Exception e) {
            log.error("Failed to process identity event: {}", e.getMessage(), e);
        }
    }
}
