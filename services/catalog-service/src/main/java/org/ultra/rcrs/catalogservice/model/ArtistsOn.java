package org.ultra.rcrs.catalogservice.model;

import lombok.Data;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.UserDefinedType;
import org.ultra.rcrs.enums.ArtistRole;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@UserDefinedType("artists_on")
public class ArtistsOn {

    @Column("main_artists")
    private Set<UUID> mainArtists = new HashSet<>();

    @Column("featured_artists")
    private Set<UUID> featuredArtists = new HashSet<>();

    public Set<UUID> getArtistsByRole(ArtistRole role) {
        if (ArtistRole.MAIN_ARTIST.equals(role)) {
            return mainArtists;
        } else if (ArtistRole.FEATURED_ARTIST.equals(role)) {
            return featuredArtists;
        } else {
            return null;
        }
    }
}
