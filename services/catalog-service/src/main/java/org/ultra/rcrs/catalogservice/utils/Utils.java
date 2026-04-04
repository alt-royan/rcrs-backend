package org.ultra.rcrs.catalogservice.utils;

import org.ultra.rcrs.catalogservice.model.ArtistsOn;
import org.ultra.rcrs.enums.ArtistRole;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Utils {

    public static Set<UUID> flatArtists(ArtistsOn artists) {
        return Stream.concat(artists.getFeaturedArtists().stream(), artists.getMainArtists().stream())
                .collect(Collectors.toSet());
    }

    public static Map<UUID, ArtistRole> artistsToMap(ArtistsOn artists) {
        Map<UUID, ArtistRole> map = new HashMap<>();
        artists.getFeaturedArtists().forEach(uuid -> map.put(uuid, ArtistRole.FEATURED_ARTIST));
        artists.getMainArtists().forEach(uuid -> map.put(uuid, ArtistRole.MAIN_ARTIST));
        return map;
    }
}
