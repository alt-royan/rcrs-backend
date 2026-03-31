package org.ultra.rcrs.catalogservice.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import org.ultra.rcrs.enums.AlbumType;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Builder
@Table("albums_by_id")
public class AlbumById {

    @PrimaryKey
    @Column("album_id")
    private UUID albumId;

    @Column("artist_ids")
    private Set<UUID> artistIds;

    private String title;

    @Column("album_type")
    private AlbumType albumType;

    @Column("release_date")
    private LocalDate releaseDate;

    @Column("image_key")
    private String imageKey;

    @Column("total_tracks")
    private Integer totalTracks;

    @Column("created_at")
    @CreatedDate
    private Instant createdAt;

    @Column("last_modified_at")
    @LastModifiedDate
    private Instant lastModifiedAt;

}
