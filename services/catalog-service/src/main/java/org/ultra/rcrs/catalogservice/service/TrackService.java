package org.ultra.rcrs.catalogservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.catalogservice.dto.ItemListDto;
import org.ultra.rcrs.catalogservice.dto.TrackDto;
import org.ultra.rcrs.catalogservice.dto.simplify.AlbumSimplifyDto;
import org.ultra.rcrs.catalogservice.dto.simplify.TrackSimplifyDto;
import org.ultra.rcrs.catalogservice.model.album.Album;
import org.ultra.rcrs.catalogservice.model.artist.ArtistWithRole;
import org.ultra.rcrs.catalogservice.model.track.TrackByAlbum;
import org.ultra.rcrs.catalogservice.model.track.TrackByArtist;
import org.ultra.rcrs.catalogservice.repository.AlbumRepository;
import org.ultra.rcrs.catalogservice.repository.TrackByAlbumRepository;
import org.ultra.rcrs.catalogservice.repository.TrackByArtistRepository;
import org.ultra.rcrs.catalogservice.repository.TrackRepository;
import org.ultra.rcrs.enums.ArtistRole;
import org.ultra.rcrs.exceptions.NotFoundException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TrackService {

    private final TrackRepository trackRepository;
    private final TrackByAlbumRepository trackByAlbumRepository;
    private final TrackByArtistRepository trackByArtistRepository;

    private final AlbumRepository albumRepository;
    public final ArtistService artistService;

    public Mono<TrackDto> getTrack(UUID trackId) {
        return trackRepository.findById(trackId)
                .switchIfEmpty(Mono.error(new NotFoundException("Track with id " + trackId + " was not found")))
                .flatMap(track -> collectAlbumForTrack(track.getAlbumId())
                        .zipWith(artistService.collectAllArtists(track.getArtists()))
                        .map(tuple -> new TrackDto(track, tuple.getT2(), tuple.getT1())));
    }

    public Mono<TrackSimplifyDto> getTrack(TrackByAlbum trackByAlbum) {
        return trackRepository.findById(trackByAlbum.getKey().getTrackId())
                .flatMap(track -> artistService.collectAllArtists(track.getArtists())
                        .map(artists -> new TrackSimplifyDto(track, artists)));
    }

    public Mono<TrackSimplifyDto> getTrack(TrackByArtist trackByArtist) {
        return trackRepository.findById(trackByArtist.getKey().getTrackId())
                .flatMap(track -> artistService.collectAllArtists(track.getArtists())
                        .map(artists -> new TrackSimplifyDto(track, artists)));
    }

    public Mono<ItemListDto<TrackSimplifyDto>> getTracksForAlbum(Album album) {
        return trackByAlbumRepository.findByKeyAlbumId(album.getAlbumId())
                .flatMap(this::getTrack)
                .collectList()
                .map(ItemListDto::new);
    }

    public Mono<Void> deleteTrackById(UUID trackId) {
        return trackRepository.findById(trackId)
                .flatMap(track ->
                        deleteTrackByAlbum(track.getAlbumId(), trackId)
                                .then(deleteTrackByArtists(track.getArtists(), trackId))
                                .then(trackRepository.deleteById(trackId))
                );
    }

    public Mono<Void> deleteAllTracksFromAlbum(Album album) {
        return trackByAlbumRepository.findByKeyAlbumId(album.getAlbumId())
                .flatMap(trackByAlbum -> deleteTrackById(trackByAlbum.getKey().getTrackId()))
                .then();
    }

    private Mono<Void> deleteTrackByAlbum(UUID albumId, UUID trackId) {
        return trackByAlbumRepository.deleteByKeyAlbumIdAndTrackId(albumId, trackId);
    }

    private Mono<Void> deleteTrackByArtists(Collection<ArtistWithRole> artists, UUID trackId) {
        return Flux.fromIterable(artists)
                .flatMap(artistWithRole -> trackByArtistRepository.deleteByKeyArtistIdAndArtistRoleAndTrackId(artistWithRole.getArtistId(), artistWithRole.getArtistRole(), trackId))
                .then();
    }

    private Mono<AlbumSimplifyDto> collectAlbumForTrack(UUID albumId) {
        return albumRepository.findById(albumId).flatMap(album ->
                artistService.collectArtistsByRole(album.getArtists(), ArtistRole.MAIN_ARTIST)
                        .map(artists -> new AlbumSimplifyDto(album, artists)));
    }
}
