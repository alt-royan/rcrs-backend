package org.ultra.rcrs.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.ultra.rcrs.enums.LifecycleStatus;
import org.ultra.rcrs.enums.EntityType;

@Data
@AllArgsConstructor
public class UpdateEntityStatusEvent {
    private String id;
    private EntityType entityType;
    private LifecycleStatus newStatus;

}
