package org.ultra.rcrs.catalogservice.model;

import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("artists_by_id")
public class ArtistById {

    @PrimaryKey
    @Column("artist_id")
    private UUID artistId;

    private String name;

    private String bio;

    private String country;

    @Column("created_at")
    private Instant createdAt;

    private String imageUrl;

}