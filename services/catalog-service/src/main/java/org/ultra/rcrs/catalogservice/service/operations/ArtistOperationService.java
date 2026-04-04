package org.ultra.rcrs.catalogservice.service.operations;

import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.catalogservice.dto.full.ArtistWithRoleMetadata;
import org.ultra.rcrs.catalogservice.dto.simplify.SimpleArtistMetadata;
import org.ultra.rcrs.enums.ArtistRole;
import org.ultra.rcrs.enums.EntityType;
import reactor.core.publisher.Mono;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ArtistOperationService {

    private final ArtistByEntityRepository artistByEntityRepository;

    public Mono<List<SimpleArtistMetadata>> collectArtistsSimpleForAlbum(@Nonnull UUID albumId) {
        Objects.requireNonNull(albumId, "albumId must not be null here");

        return artistByEntityRepository.findAllByKeyEntityTypeAndKeyEntityId(EntityType.ALBUM, albumId)
                .filter(artist -> ArtistRole.MAIN_ARTIST.equals(artist.getArtistRole()))
                .map(SimpleArtistMetadata::new)
                .collectList();
    }

    public Mono<List<ArtistWithRoleMetadata>> collectArtistsWithRoleForAlbum(@Nonnull UUID albumId) {
        Objects.requireNonNull(albumId, "albumId must not be null here");

        return artistByEntityRepository.findAllByKeyEntityTypeAndKeyEntityId(EntityType.ALBUM, albumId)
                .map(ArtistWithRoleMetadata::new)
                .collectList();
    }

    public Mono<List<UUID>> deleteArtistsForAlbum(@Nonnull UUID albumId) {
        Objects.requireNonNull(albumId, "albumId must not be null here");

        return artistByEntityRepository.deleteAllByKeyEntityTypeAndKeyEntityIdIn(EntityType.ALBUM, List.of(albumId))
                .filter(ArtistByEntity::getIsArtist)
                .map(ArtistByEntity::getArtistId)
                .collectList();
    }

    public Mono<List<UUID>> deleteArtistsForTrack(UUID trackId) {
        deleteArtistsForTracks(List.of(trackId));
    }

    public Mono<List<UUID>> deleteArtistsForTracks(@Nonnull List<UUID> trackIds) {
        Objects.requireNonNull(trackIds, "trackId must not be null here");

        return artistByEntityRepository.deleteAllByKeyEntityTypeAndKeyEntityIdIn(EntityType.TRACK, trackIds)
                .filter(ArtistByEntity::getIsArtist)
                .map(ArtistByEntity::getArtistId)
                .collectList();
    }

    public Mono<List<SimpleArtistMetadata>> collectArtistsSimpleForTrack(@Nonnull UUID trackId) {
        Objects.requireNonNull(trackId, "trackId must not be null here");

        return artistByEntityRepository.findAllByKeyEntityTypeAndKeyEntityId(EntityType.TRACK, trackId)
                .filter(artist -> ArtistRole.MAIN_ARTIST.equals(artist.getArtistRole()) || ArtistRole.FEATURED_ARTIST.equals(artist.getArtistRole()))
                .map(SimpleArtistMetadata::new)
                .collectList();
    }

    public Mono<Map<UUID, List<SimpleArtistMetadata>>> collectArtistsSimpleForTracks(@Nonnull List<UUID> trackIds) {
        Objects.requireNonNull(trackIds, "trackIds must not be null here");

        return artistByEntityRepository.findAllByKeyEntityTypeAndKeyEntityIdIn(EntityType.TRACK, trackIds)
                .map(list -> {
                    Map<UUID, List<SimpleArtistMetadata>> map = new HashMap<>();
                    for (ArtistByEntity artist : list) {
                        var id = artist.getKey().getEntityId();
                        if (map.containsKey(id)) {
                            map.get(id).add(new SimpleArtistMetadata(artist));
                        } else {
                            List<SimpleArtistMetadata> newList = new LinkedList<>();
                            newList.add(new SimpleArtistMetadata(artist));
                            map.put(id, newList);
                        }
                    }
                    return map;
                });
    }

    public Mono<List<ArtistWithRoleMetadata>> collectArtistsWithRoleForTrack(@Nonnull UUID trackId) {
        Objects.requireNonNull(trackId, "trackId must not be null here");

        return artistByEntityRepository.findAllByKeyEntityTypeAndKeyEntityId(EntityType.TRACK, trackId)
                .map(ArtistWithRoleMetadata::new)
                .collectList();
    }

}
