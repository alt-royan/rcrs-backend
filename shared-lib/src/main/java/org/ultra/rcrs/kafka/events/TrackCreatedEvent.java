package org.ultra.rcrs.kafka.events;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
public class TrackCreatedEvent {

    private String uid;
    private UUID trackId;
    private Instant timestamp;

}
