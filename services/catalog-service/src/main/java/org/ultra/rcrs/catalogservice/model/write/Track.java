package org.ultra.rcrs.catalogservice.model.write;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.ultra.rcrs.enums.LifecycleStatus;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@Table("tracks")
public class Track {

    private UUID id;

    private LifecycleStatus status;

    private String title;

    @Column("release_date")
    private Instant releaseDate;

    @Column("duration_ms")
    private Integer durationMs;

    @Column("track_number")
    private Integer trackNumber;

    @Column("explicit")
    private Boolean explicit;

    @Column("available")
    private Boolean available;

    @Column("album_id")
    private UUID albumId;

}