package org.ultra.rcrs.catalogservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.catalogservice.dto.response.album.AlbumInTrack;
import org.ultra.rcrs.catalogservice.dto.response.artist.ArtistSimple;
import org.ultra.rcrs.catalogservice.dto.response.track.TrackInAlbum;
import org.ultra.rcrs.catalogservice.dto.response.track.TrackPage;
import org.ultra.rcrs.catalogservice.repository.TrackRepository;
import org.ultra.rcrs.catalogservice.service.operations.AlbumConverter;
import org.ultra.rcrs.catalogservice.service.operations.ArtistConverter;
import org.ultra.rcrs.catalogservice.service.operations.TrackConverter;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.exceptions.NotFoundException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TrackCrudService {

    private final TrackRepository trackRepository;
    private final AlbumConverter albumConverter;
    private final TrackConverter trackConverter;
    public final ArtistConverter artistConverter;

    public Mono<TrackPage> getTrack(UUID trackId, List<EntityStatus> statuses) {
        return trackRepository.findByIdAndStatusIn(trackId, statuses)
                .switchIfEmpty(Mono.error(new NotFoundException("Track with id " + trackId + " was not found")))
                .flatMap(track -> albumConverter.collectAlbumForTrack(track.getAlbumId(), statuses)
                        .zipWith(artistConverter.collectArtistsSimple(track.getMainArtists()))
                        .zipWith(artistConverter.collectArtistsSimple(track.getFeaturedArtists()))
                        .map(tuple -> {
                            AlbumInTrack album = tuple.getT1().getT1();
                            Set<ArtistSimple> mainArtists = tuple.getT1().getT2();
                            Set<ArtistSimple> featuredArtists = tuple.getT2();
                            return new TrackPage(track, album, mainArtists, featuredArtists);
                        }));
    }

    public Mono<List<TrackInAlbum>> getTracksForAlbum(UUID albumId, List<EntityStatus> statuses) {
        return trackConverter.collectTracksForAlbum(albumId, statuses);
    }

}
