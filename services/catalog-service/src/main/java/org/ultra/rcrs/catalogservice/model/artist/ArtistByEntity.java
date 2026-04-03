package org.ultra.rcrs.catalogservice.model.artist;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.*;
import org.ultra.rcrs.enums.ArtistRole;
import org.ultra.rcrs.enums.EntityType;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@Table("artists_by_entity")
public class ArtistByEntity {

    @PrimaryKey
    private ArtistByEntityKey key;

    @Column("is_artist")
    private Boolean isArtist;

    @Column("artist_id")
    private UUID artistId;

    @Column("artist_role")
    private ArtistRole artistRole;

    @Column("artist_name")
    private String artistName;

    @Column("social_link")
    private String socialLink;

    @Column("artist_image_key")
    private String artistImageKey;


    @Getter
    @Setter
    @Builder
    @PrimaryKeyClass
    public static class ArtistByEntityKey {

        @PrimaryKeyColumn(
                name = "entity_type",
                ordinal = 0,
                type = PrimaryKeyType.PARTITIONED
        )
        private EntityType entityType;

        @PrimaryKeyColumn(
                name = "entity_id",
                ordinal = 1,
                type = PrimaryKeyType.PARTITIONED
        )
        private ArtistRole entityId;

    }

}