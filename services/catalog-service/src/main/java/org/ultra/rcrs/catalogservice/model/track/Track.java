package org.ultra.rcrs.catalogservice.model.track;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import org.ultra.rcrs.enums.ArtistRole;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Builder
@Table("tracks_by_id")
public class Track {

    @PrimaryKey
    @Column("track_id")
    private UUID trackId;

    private String title;

    @Column("duration_ms")
    private Long durationMs;

    @Column("album_id")
    private UUID albumId;

    @Column("track_number")
    private Integer trackNumber;

    @Column("artists")
    private Map<UUID, ArtistRole> artists;

}