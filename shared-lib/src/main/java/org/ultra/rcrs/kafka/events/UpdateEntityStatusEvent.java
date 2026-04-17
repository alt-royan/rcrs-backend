package org.ultra.rcrs.kafka.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.enums.EntityType;

@Data
@AllArgsConstructor
public class UpdateEntityStatusEvent {
    private String id;
    private EntityType entityType;
    private EntityStatus newStatus;

}
