package org.ultra.rcrs.catalogservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.catalogservice.dto.full.ArtistWithRoleMetadata;
import org.ultra.rcrs.catalogservice.dto.full.FullAlbumMetadata;
import org.ultra.rcrs.catalogservice.dto.request.AlbumCreateRequest;
import org.ultra.rcrs.catalogservice.dto.simplify.SimpleTrackMetadata;
import org.ultra.rcrs.catalogservice.model.album.Album;
import org.ultra.rcrs.catalogservice.repository.AlbumRepository;
import org.ultra.rcrs.catalogservice.service.operations.ArtistOperationService;
import org.ultra.rcrs.catalogservice.service.operations.TrackOperationService;
import org.ultra.rcrs.enums.TrackStatus;
import org.ultra.rcrs.exceptions.NotFoundException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AlbumCrudService {

    private final AlbumRepository albumRepository;

    private final ArtistOperationService artistService;

    private final TrackOperationService trackService;

    public Mono<FullAlbumMetadata> getAlbum(UUID albumId, boolean published) {
        Mono<Album> albumMono;
        if (published) {
            albumMono = albumRepository.findById(albumId)
                    .switchIfEmpty(Mono.error(new NotFoundException("Album with id " + albumId + " was not found")));
        } else {
            albumMono = albumRepository.findByKeyIdAndKeyStatus(albumId, TrackStatus.PUBLISHED)
                    .switchIfEmpty(Mono.error(new NotFoundException("Album with id " + albumId + " was not found")));
        }
        return albumMono.zipWith(artistService.collectArtistsWithRoleForAlbum(albumId))
                .zipWith(this.getTracksForAlbum(albumId))
                .map(tuple -> {
                    Album album = tuple.getT1().getT1();
                    List<ArtistWithRoleMetadata> artists = tuple.getT1().getT2();
                    List<SimpleTrackMetadata> tracks = tuple.getT2();
                    return new FullAlbumMetadata(album, artists, tracks);
                });
    }

    public Mono<List<SimpleTrackMetadata>> getTracksForAlbum(UUID albumId) {
        return trackService.getTracksForAlbum(albumId);
    }

    public Mono<FullAlbumMetadata> createAlbum(AlbumCreateRequest dto) {
        return albumRepository.save(new Album(dto)).flatMap(this::albumToDto);
    }

    public Mono<Void> deleteAlbumCascadeById(UUID albumId) {
        return albumRepository.findById(albumId)
                .switchIfEmpty(Mono.error(new NotFoundException("Album with id " + albumId + " was not found")))
                .flatMap(album -> trackService.deleteAllTracksFromAlbum(album.getKey().getId())
                        .then(deleteAlbumByArtists(album.getArtists(), albumId))
                        .then(albumRepository.deleteById(albumId)));
    }


}
