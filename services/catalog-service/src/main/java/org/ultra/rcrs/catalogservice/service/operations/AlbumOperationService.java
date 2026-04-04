package org.ultra.rcrs.catalogservice.service.operations;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.catalogservice.dto.simplify.SimpleAlbumMetadata;
import org.ultra.rcrs.catalogservice.model.album.AlbumByArtist;
import org.ultra.rcrs.catalogservice.repository.AlbumByArtistRepository;
import org.ultra.rcrs.catalogservice.repository.AlbumRepository;
import org.ultra.rcrs.enums.Order;
import org.ultra.rcrs.enums.ArtistRole;
import org.ultra.rcrs.exceptions.NotFoundException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AlbumOperationService {

    private final AlbumRepository albumRepository;
    private final AlbumByArtistRepository albumByArtistRepository;
    private final ArtistOperationService artistService;
    private final TrackOperationService trackService;


    public Mono<SimpleAlbumMetadata> collectAlbumForTrack(UUID albumId) {
        return albumRepository.findById(albumId).zipWith(artistService.collectArtistsSimpleForAlbum(albumId))
                .map(tuple -> new SimpleAlbumMetadata(tuple.getT1(), tuple.getT2()));
    }


    public Mono<List<SimpleAlbumMetadata>> getAlbumsForArtist(UUID artistId, Order order, ArtistRole artistRole) {
        return albumByArtistRepository.findByKeyArtistIdAndKeyArtistRole(artistId, artistRole)
                .collectList().map(list -> order == Order.ASC ? list.reversed() : list)
                .flatMapMany(Flux::fromIterable).flatMap(this::collectSimpleAlbumMetadata).collectList();
    }

    private Mono<SimpleAlbumMetadata> collectSimpleAlbumMetadata(AlbumByArtist albumByArtist) {
        var albumId = albumByArtist.getKey().getAlbumId();
        return albumRepository.findById(albumId).zipWith(artistService.collectArtistsSimpleForAlbum(albumId))
                .map(tuple -> new SimpleAlbumMetadata(tuple.getT1(), tuple.getT2()));
    }

    public Mono<Void> deleteAlbumCascadeById(UUID albumId) {
        return albumRepository.findById(albumId)
                .switchIfEmpty(Mono.error(new NotFoundException("Album with id " + albumId + " was not found")))
                .flatMap(album -> trackService.deleteAllTracksFromAlbum(album.getKey().getId())
                        .then(artistService.deleteArtistsForAlbum(albumId)
                                .map(artistIds -> albumByArtistRepository.deleteByKeyArtistIdAndKeyArtistRoleAndKeyAlbumId()))
                        .then(albumRepository.deleteById(albumId)));
    }


}
