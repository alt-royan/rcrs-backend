package org.ultra.rcrs.catalogservice.service.operations;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.catalogservice.dto.response.track.TrackInAlbum;
import org.ultra.rcrs.catalogservice.repository.TrackByAlbumRepository;
import org.ultra.rcrs.enums.EntityStatus;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TrackConverter {

    private final TrackByAlbumRepository trackByAlbumRepository;
    private final ArtistConverter artistConverter;

    public Mono<List<TrackInAlbum>> collectTracksForAlbum(UUID albumId, List<EntityStatus> trackStatuses) {
        return trackByAlbumRepository.findAll(albumId, trackStatuses)
                .flatMap(track ->
                        artistConverter.collectArtistsSimple(track.getMainArtists())
                                .zipWith(artistConverter.collectArtistsSimple(track.getFeaturedArtists()))
                                .map(tuple -> new TrackInAlbum(track, tuple.getT1(), tuple.getT2()))
                ).collectList();
    }

}
