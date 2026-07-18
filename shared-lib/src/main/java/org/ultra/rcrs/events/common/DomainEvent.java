package org.ultra.rcrs.events.common;

import java.time.OffsetDateTime;
import java.util.UUID;

public class DomainEvent<T> {

    private UUID eventId;

    private String eventType;

    private int version;

    private String aggregateType;

    private UUID aggregateId;

    private OffsetDateTime occurredAt;

    private String producer;

    private T payload;
}