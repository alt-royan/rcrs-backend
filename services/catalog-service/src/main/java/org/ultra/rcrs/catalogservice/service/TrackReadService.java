package org.ultra.rcrs.catalogservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.catalogservice.dto.TrackDto;
import org.ultra.rcrs.catalogservice.dto.simplify.AlbumSimplifyDto;
import org.ultra.rcrs.catalogservice.model.TrackById;
import org.ultra.rcrs.catalogservice.repository.AlbumByTrackRepository;
import org.ultra.rcrs.catalogservice.repository.TrackByIdRepository;
import org.ultra.rcrs.exceptions.NotFoundException;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TrackReadService {

    @Value
    private final TrackByIdRepository trackByIdRepository;

    private final AlbumByTrackRepository albumByTrackRepository;

    public final ArtistReadService artistReadService;

    public Mono<TrackDto> getTrack(UUID trackId) {
        return trackByIdRepository.findById(trackId)
                .switchIfEmpty(Mono.error(new NotFoundException("Track with id " + trackId + " was not found")))
                .zipWith(this.collectAlbumForTrack(trackId))
                .flatMap(tuple -> {
                    TrackById track = tuple.getT1();
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
