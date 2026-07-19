package org.ultra.rcrs.metadata.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.ultra.rcrs.enums.AlbumType;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.enums.LifecycleStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "albums")
public class Album {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "lifecycle_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private LifecycleStatus lifecycleStatus;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private AlbumType type;

    @Column(name = "release_date")
    private OffsetDateTime releaseDate;

    @Column(name = "publish_timestamp")
    private OffsetDateTime publishTimestamp;

    @Column(name = "cover_s3_key")
    private String coverS3Key;

    @Column(name = "availability_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private EntityStatus availabilityStatus;
}
