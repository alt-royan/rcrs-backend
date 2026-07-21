package org.ultra.rcrs.userservice.listener;

import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.ultra.rcrs.kafka.Topics;
import org.ultra.rcrs.userservice.dto.KeycloakRegistrationEvent;
import org.ultra.rcrs.userservice.service.UserService;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserRegistrationListener {

    private final UserService userService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = Topics.USER_REGISTRATION_TOPIC, groupId = "user-service-group", containerFactory = "stringContainerFactory")
    public void handleRegistrationEvent(ConsumerRecord<String, String> record) {
        try {
            log.info("Received user registration event: key={}", record.key());
            KeycloakRegistrationEvent event = objectMapper.readValue(record.value(), KeycloakRegistrationEvent.class);

            userService.handleRegistration(
                    event.getSub(),
                    event.getPreferredUsername(),
                    event.getEmail(),
                    event.getGivenName(),
                    event.getFamilyName()
            );
        } catch (Exception e) {
            log.error("Failed to process user registration event: {}", e.getMessage(), e);
        }
    }
}
