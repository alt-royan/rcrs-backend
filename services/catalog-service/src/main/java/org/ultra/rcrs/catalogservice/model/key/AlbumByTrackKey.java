package org.ultra.rcrs.catalogservice.model.key;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
@PrimaryKeyClass
public class AlbumByTrackKey {

    @PrimaryKeyColumn(
        name = "track_id",
        ordinal = 0,
        type = PrimaryKeyType.PARTITIONED
    )
    private UUID trackId;

    @PrimaryKeyColumn(
        name = "album_id",
        ordinal = 1,
        type = PrimaryKeyType.CLUSTERED
    )
    private UUID albumId;

}