package org.ultra.rcrs.catalogservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.catalogservice.dto.AlbumDto;
import org.ultra.rcrs.catalogservice.dto.ItemListDto;
import org.ultra.rcrs.catalogservice.dto.request.AlbumCreateDto;
import org.ultra.rcrs.catalogservice.dto.simplify.AlbumSimplifyDto;
import org.ultra.rcrs.catalogservice.dto.simplify.TrackSimplifyDto;
import org.ultra.rcrs.catalogservice.model.album.Album;
import org.ultra.rcrs.catalogservice.model.album.AlbumByArtist;
import org.ultra.rcrs.catalogservice.model.artist.ArtistWithRole;
import org.ultra.rcrs.catalogservice.repository.AlbumByArtistRepository;
import org.ultra.rcrs.catalogservice.repository.AlbumRepository;
import org.ultra.rcrs.enums.AlbumsOrder;
import org.ultra.rcrs.enums.ArtistRole;
import org.ultra.rcrs.exceptions.NotFoundException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AlbumService {

    private final AlbumRepository albumRepository;

    private final AlbumByArtistRepository albumByArtistRepository;

    private final ArtistService artistService;

    private final TrackService trackService;

    public Mono<AlbumDto> getAlbum(UUID albumId) {
        return albumRepository.findById(albumId)
                .switchIfEmpty(Mono.error(new NotFoundException("Album with id " + albumId + " was not found")))
                .flatMap(album -> trackService.getTracksForAlbum(album)
                        .flatMap(tracks -> artistService.collectArtistsByRole(album.getArtists(), ArtistRole.MAIN_ARTIST)
                                .map(artists -> new AlbumDto(album, artists, tracks))));
    }

    public Mono<AlbumSimplifyDto> getAlbum(AlbumByArtist albumByArtist) {
        return albumRepository.findById(albumByArtist.getKey().getAlbumId())
                .switchIfEmpty(Mono.error(new NotFoundException("Album with id " + albumByArtist.getKey().getAlbumId() + " was not found")))
                .flatMap(album -> artistService.collectArtistsByRole(album.getArtists(), ArtistRole.MAIN_ARTIST)
                        .map(artists -> new AlbumSimplifyDto(album, artists)));
    }

    public Mono<ItemListDto<TrackSimplifyDto>> getTracksForAlbum(UUID albumId) {
        return albumRepository.findById(albumId)
                .switchIfEmpty(Mono.error(new NotFoundException("Album with id " + albumId + " was not found")))
                .flatMap(trackService::getTracksForAlbum);
    }


    public Mono<ItemListDto<AlbumSimplifyDto>> getAlbumsForArtist_Main(UUID artistId, AlbumsOrder order) {
        return this.getAlbumsForArtist(artistId, order, ArtistRole.MAIN_ARTIST);
    }

    public Mono<ItemListDto<AlbumSimplifyDto>> getAlbumsForArtist_AppearsOn(UUID artistId, AlbumsOrder order) {
        return this.getAlbumsForArtist(artistId, order, ArtistRole.APPEARS_ON);
    }

    public Mono<ItemListDto<AlbumSimplifyDto>> getAlbumsForArtist(UUID artistId, AlbumsOrder order, ArtistRole artistRole) {
        Flux<AlbumByArtist> result = artistRole == ArtistRole.MAIN_ARTIST ?
                albumByArtistRepository.findByArtistId_Main(artistId, order) :
                albumByArtistRepository.findByArtistId_AppearsOn(artistId, order);

        return result.flatMap(this::getAlbum)
                .collectList()
                .map(ItemListDto::new);
    }

    public Mono<Album> createAlbum(AlbumCreateDto dto) {
        return albumRepository.save(new Album(dto));
    }

    public Mono<Void> deleteAlbumCascadeById(UUID albumId) {
        return albumRepository.findById(albumId)
                .switchIfEmpty(Mono.error(new NotFoundException("Album with id " + albumId + " was not found")))
                .flatMap(album -> trackService.deleteAllTracksFromAlbum(album)
                        .then(deleteAlbumByArtists(album.getArtists(), albumId))
                        .then(albumRepository.deleteById(albumId)));
    }

    private Mono<Void> deleteAlbumByArtists(Collection<ArtistWithRole> artists, UUID albumId) {
        return Flux.fromIterable(artists)
                .flatMap(artistWithRole -> albumByArtistRepository.deleteByKeyArtistIdAndArtistRoleAndAlbumId(artistWithRole.getArtistId(), artistWithRole.getArtistRole(), albumId))
                .then();
    }

}
