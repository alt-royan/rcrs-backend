package org.ultra.rcrs.catalogservice.model.write;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.ultra.rcrs.enums.ArtistRole;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@Table("artist_to_album")
public class ArtistToAlbum {

    @Column("artist_id")
    private UUID artistId;

    @Column("album_id")
    private UUID albumId;

    @Column("artist_role")
    private ArtistRole artistRole;

}