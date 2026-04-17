package org.ultra.rcrs.kafka.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.ultra.rcrs.enums.EntityType;

@Data
@AllArgsConstructor
public class StartReindexEvent {
    private EntityType entityType;
    private int batchSize;
}
