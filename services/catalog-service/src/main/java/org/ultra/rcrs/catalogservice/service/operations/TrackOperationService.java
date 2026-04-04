package org.ultra.rcrs.catalogservice.service.operations;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.catalogservice.dto.simplify.SimpleTrackMetadata;
import org.ultra.rcrs.catalogservice.model.track.TrackByAlbum;
import org.ultra.rcrs.catalogservice.repository.TrackByAlbumRepository;
import org.ultra.rcrs.catalogservice.repository.TrackByArtistRepository;
import org.ultra.rcrs.catalogservice.repository.TrackRepository;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TrackOperationService {

    private final TrackRepository trackRepository;
    private final TrackByAlbumRepository trackByAlbumRepository;
    private final TrackByArtistRepository trackByArtistRepository;

    public final ArtistOperationService artistService;


    public Mono<List<SimpleTrackMetadata>> getTracksForAlbum(UUID albumId) {
        return trackByAlbumRepository.findByKeyAlbumId(albumId)
                .collectList()
                .flatMap(list -> {
                    var ids = list.stream().map(trackByAlbum -> trackByAlbum.getKey().getTrackId()).toList();

                    return trackRepository.findAllByKeyTrackIdIn(ids).zipWith(artistService.collectArtistsSimpleForTracks(ids))
                            .map(tuple -> {
                                var artistMap = tuple.getT2();
                                return tuple.getT1().stream().map(track -> new SimpleTrackMetadata(track, artistMap.get(track.getKey().getId()))).toList();
                            });
                });
    }

    public Mono<Void> deleteTrackById(UUID trackId) {
        return trackRepository.findById(trackId)
                .flatMap(track ->
                        trackByAlbumRepository.deleteByKeyAlbumIdAndKeyTrackNumberAndKeyTrackId(track.getAlbumId(), track.getTrackNumber(), trackId)
                                .then(artistService.deleteArtistsForTrack(trackId)
                                        .map(artistIds -> trackByArtistRepository.deleteAllByKeyArtistIdInAndKeyTrackId(artistIds, trackId))
                                )
                                .then(trackRepository.deleteById(trackId))
                );
    }

    public Mono<Void> deleteAllTracksFromAlbum(UUID albumId) {
        return trackByAlbumRepository.deleteAllByKeyAlbumId(albumId)
                .map(TrackByAlbum.TrackByAlbumKey::getTrackId).collectList()
                .flatMap(trackIds -> artistService.deleteArtistsForTracks(trackIds)
                        .flatMap(artistIds -> trackByArtistRepository.deleteAllByKeyArtistIdInAndKeyTrackIdIn(artistIds, trackIds))
                        .then(trackRepository.deleteAllById(trackIds))
                );

    }

}
