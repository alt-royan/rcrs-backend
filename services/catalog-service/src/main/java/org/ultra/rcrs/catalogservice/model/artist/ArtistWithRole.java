package org.ultra.rcrs.catalogservice.model.artist;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.ultra.rcrs.enums.ArtistRole;

import java.util.UUID;

@AllArgsConstructor
@Getter
@Setter
public class ArtistWithRole {

    private UUID artistId;
    private ArtistRole artistRole;

    public boolean isMainArtist() {
        return ArtistRole.MAIN_ARTIST.equals(artistRole);
    }
}
