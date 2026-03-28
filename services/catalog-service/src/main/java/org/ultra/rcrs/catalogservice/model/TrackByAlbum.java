package org.ultra.rcrs.catalogservice.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import org.ultra.rcrs.catalogservice.model.key.TrackByAlbumKey;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Builder
@Table("tracks_by_album")
public class TrackByAlbum {

    @PrimaryKey
    private TrackByAlbumKey key;

    @Column("track_id")
    private UUID trackId;

    private String title;

    private Long duration;

    @Column("artist_ids")
    private Set<UUID> artistIds;

    @Column("image_url")
    private String imageUrl;

    @Column("created_at")
    @CreatedDate
    private Instant createdAt;

    @Column("last_modified_at")
    @LastModifiedDate
    private Instant lastModifiedAt;

}
