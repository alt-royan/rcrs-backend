package org.ultra.rcrs.metadata.kafka.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.ultra.rcrs.metadata.service.StatusService;
import org.ultra.rcrs.enums.EntityType;
import org.ultra.rcrs.kafka.Topics;
import org.ultra.rcrs.events.UpdateEntityStatusEvent;
import org.ultra.rcrs.utils.Url62;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaEventListener {

    private final tools.jackson.databind.ObjectMapper objectMapper;
    private final StatusService statusService;

    @KafkaListener(topics = Topics.CATALOG_UPDATE_ENTITY_STATUS_TOPIC, groupId = "write-group")
    public void handleUpdateStatusEvent(String message) {
        log.info("Received message {}", message);
        UpdateEntityStatusEvent event = objectMapper.readValue(message, UpdateEntityStatusEvent.class);
        var status = event.getNewStatus();
        if (EntityType.TRACK.equals(event.getEntityType()) && !StringUtils.isEmpty(event.getId())) {
            statusService.updateTrackStatus(Url62.decode(event.getId()), status);
        }
    }
}
