package org.ultra.rcrs.metadata.model;

import jakarta.persistence.*;
import lombok.*;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.enums.LifecycleStatus;

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
