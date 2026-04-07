package org.ultra.rcrs.catalogservice.model.write;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
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

    @Id
    private ArtistToAlbumId id;

    @Column("artist_role")
    private ArtistRole artistRole;

    public record ArtistToAlbumId(@Column("artist_id") UUID artistId, @Column("album_id") UUID albumId){}

}