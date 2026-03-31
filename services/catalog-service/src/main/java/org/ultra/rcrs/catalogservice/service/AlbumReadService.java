package org.ultra.rcrs.catalogservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.catalogservice.dto.AlbumDto;
import org.ultra.rcrs.catalogservice.dto.ItemListDto;
import org.ultra.rcrs.catalogservice.dto.simplify.AlbumSimplifyDto;
import org.ultra.rcrs.catalogservice.dto.simplify.ArtistSimplifyDto;
import org.ultra.rcrs.catalogservice.dto.simplify.TrackSimplifyDto;
import org.ultra.rcrs.catalogservice.model.Album;
import org.ultra.rcrs.catalogservice.repository.AlbumByArtistRepository;
import org.ultra.rcrs.catalogservice.repository.AlbumRepository;
import org.ultra.rcrs.catalogservice.repository.ArtistRepository;
import org.ultra.rcrs.catalogservice.repository.TrackByAlbumRepository;
import org.ultra.rcrs.exceptions.NotFoundException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AlbumReadService {
    private final AlbumRepository albumRepository;

    private final TrackByAlbumRepository trackByAlbumRepository;

    private final ArtistRepository artistRepository;

    private final AlbumByArtistRepository albumByArtistRepository;

    private final ArtistReadService artistReadService;

    public Mono<AlbumDto> getAlbum(UUID albumId) {
        return albumRepository.findById(albumId)
                .switchIfEmpty(Mono.error(new NotFoundException("Album with id " + albumId + " was not found")))
                .zipWith(this.collectTracksForAlbum(albumId))
                .flatMap(tuple -> {
                    Album album = tuple.getT1();
                    List<TrackSimplifyDto> tracks = tuple.getT2();
                    return artistReadService.collectArtists(album.getArtistIds())
                            .map(artists -> new AlbumDto(album, artists, tracks));
                });
    }

    public Mono<ItemListDto<AlbumSimplifyDto>> getAlbumsForArtist(UUID artistId) {
        return albumByArtistRepository.findByKeyArtistId(artistId)
                .switchIfEmpty(Mono.error(new NotFoundException("Album with artistId " + artistId + " was not found")))
                .flatMap(album ->
                        artistReadService.collectArtists(album.getArtistIds())
                                .map(artists -> new AlbumSimplifyDto(album, artists))
                ).collectList()
                .map(ItemListDto::new);
    }

    private Mono<List<TrackSimplifyDto>> collectTracksForAlbum(UUID albumId) {
        return trackByAlbumRepository.findByKeyAlbumId(albumId).flatMap(track -> Flux.fromIterable(track.getArtistIds())
                .flatMap(artistRepository::findById)
                .map(ArtistSimplifyDto::new)
                .collectList()
                .map(artists -> new TrackSimplifyDto(track, artists))
        ).collectList();
    }

}
