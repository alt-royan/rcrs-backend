package org.ultra.rcrs.catalogservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.catalogservice.dto.TrackDto;
import org.ultra.rcrs.catalogservice.dto.simplify.AlbumSimplifyDto;
import org.ultra.rcrs.catalogservice.model.track.Track;
import org.ultra.rcrs.catalogservice.repository.TrackRepository;
import org.ultra.rcrs.exceptions.NotFoundException;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TrackReadService {

    private final TrackRepository trackRepository;

    private final AlbumByTrackRepository albumByTrackRepository;

    public final ArtistReadService artistReadService;

    public Mono<TrackDto> getTrack(UUID trackId) {
        return trackRepository.findById(trackId)
                .switchIfEmpty(Mono.error(new NotFoundException("Track with id " + trackId + " was not found")))
                .zipWith(this.collectAlbumForTrack(trackId))
                .flatMap(tuple -> {
                    Track track = tuple.getT1();
                    AlbumSimplifyDto album = tuple.getT2();
                    return artistReadService.collectArtists(track.getArtistIds())
                            .map(artists -> new TrackDto(track, artists, album));
                });
    }

    private Mono<AlbumSimplifyDto> collectAlbumForTrack(UUID trackId) {
        return albumByTrackRepository.findByKeyTrackId(trackId).flatMap(album ->
                artistReadService.collectArtists(album.getArtistIds())
                        .map(artists -> new AlbumSimplifyDto(album, artists)));
    }

}
