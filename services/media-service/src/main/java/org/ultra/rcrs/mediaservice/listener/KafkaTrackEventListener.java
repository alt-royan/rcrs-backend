package org.ultra.rcrs.mediaservice.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.ultra.rcrs.kafka.Topics;
import org.ultra.rcrs.events.StartTrackTranscodingEvent;
import org.ultra.rcrs.mediaservice.service.TranscodingService;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaTrackEventListener {

    private final ObjectMapper objectMapper;
    private final TranscodingService transcodingService;

    @KafkaListener(topics = Topics.MEDIA_START_TRACK_TRANSCODING_TOPIC, groupId = "my-group")
    public void handleTrackEvent(String message) {
        log.info("Received message {}", message);
        StartTrackTranscodingEvent event = objectMapper.readValue(message, StartTrackTranscodingEvent.class);
        if (!StringUtils.isEmpty(event.getTrackId()) && !StringUtils.isEmpty(event.getUid())) {
            transcodingService.transcode(event);
        }
    }
}
