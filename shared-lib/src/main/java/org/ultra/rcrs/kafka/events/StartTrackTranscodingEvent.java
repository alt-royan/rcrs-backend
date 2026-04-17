package org.ultra.rcrs.kafka.events;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
public class StartTrackTranscodingEvent {

    private String uid;
    private String trackId;
    private Instant timestamp;

}
