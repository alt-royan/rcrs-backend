package org.ultra.rcrs.catalogservice.model.artist;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.UserDefinedType;
import org.ultra.rcrs.enums.ArtistRole;

import java.util.UUID;

@AllArgsConstructor
@Getter
@Setter
@UserDefinedType("artist_with_role")
public class ArtistWithRole {

    @Column("artist_id")
    private UUID artistId;

    @Column("artist_role")
    private ArtistRole artistRole;

    public boolean isMainArtist() {
        return ArtistRole.MAIN_ARTIST.equals(artistRole);
    }
}
