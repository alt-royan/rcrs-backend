package org.ultra.rcrs.catalogservice.model.write;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.ultra.rcrs.enums.EntityStatus;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tracks")
public class Track {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private EntityStatus status;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "release_date")
    private Instant releaseDate;

    @Column(name = "duration_ms")
    private Integer durationMs;

    @Column(name = "track_number", nullable = false)
    private Integer trackNumber;

    @Column(name = "explicit", nullable = false)
    private Boolean explicit;

    @Column(name = "available", nullable = false)
    private Boolean available;

    @Column(name = "album_id", nullable = false)
    private UUID albumId;
}
