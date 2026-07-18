package org.ultra.rcrs.catalogservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.enums.LifecycleStatus;

import java.time.Instant;
import java.time.OffsetDateTime;
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
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "lifecycle_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private LifecycleStatus lifecycleStatus;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "release_date")
    private OffsetDateTime releaseDate;

    @Column(name = "publish_timestamp")
    private OffsetDateTime publishTimestamp;

    @Column(name = "duration_ms")
    private Integer durationMs;

    @Column(name = "track_number", nullable = false)
    private Integer trackNumber;

    @Column(name = "explicit", nullable = false)
    private Boolean explicit;

    @Column(name = "availability_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private EntityStatus availabilityStatus;

    @Column(name = "album_id", nullable = false)
    private UUID albumId;
}
