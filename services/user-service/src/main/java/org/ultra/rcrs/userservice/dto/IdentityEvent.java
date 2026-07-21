package org.ultra.rcrs.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IdentityEvent {

    private String eventId;
    private IdentityEventType eventType;
    private int schemaVersion;
    private String source;
    private Instant occurredAt;
    private IdentityEventPayload payload;
}
