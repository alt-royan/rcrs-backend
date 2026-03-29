package org.ultra.rcrs.catalogservice.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import org.ultra.rcrs.catalogservice.model.key.AlbumByArtistKey;
import org.ultra.rcrs.enums.AlbumGroup;
import org.ultra.rcrs.enums.AlbumType;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Builder
@Table("albums_by_artist")
public class AlbumByArtist {

    @PrimaryKey
    private AlbumByArtistKey key;

    private String title;

    @Column("album_type")
    private AlbumType albumType;

    @Column("album_group")
    private AlbumGroup albumGroup;

    @Column("artist_ids")
    private Set<UUID> artistIds;

    @Column("image_url")
    private String imageUrl;

    @Column("total_tracks")
    private Integer totalTracks;

    @Column("created_at")
    @CreatedDate
    private Instant createdAt;

    @Column("last_modified_at")
    @LastModifiedDate
    private Instant lastModifiedAt;

}