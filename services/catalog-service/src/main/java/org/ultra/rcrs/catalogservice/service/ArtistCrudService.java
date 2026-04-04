package org.ultra.rcrs.catalogservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.catalogservice.dto.ArtistMetadataAbstract;
import org.ultra.rcrs.catalogservice.dto.full.FullArtistMetadata;
import org.ultra.rcrs.catalogservice.dto.request.ArtistRegisterRequest;
import org.ultra.rcrs.catalogservice.dto.simplify.SimpleAlbumMetadata;
import org.ultra.rcrs.catalogservice.model.artist.Artist;
import org.ultra.rcrs.catalogservice.repository.ArtistRepository;
import org.ultra.rcrs.catalogservice.service.operations.AlbumOperationService;
import org.ultra.rcrs.enums.Order;
import org.ultra.rcrs.enums.ArtistRole;
import org.ultra.rcrs.exceptions.NotFoundException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ArtistCrudService {

    private final ArtistRepository artistRepository;
    private final AlbumOperationService albumService;

    public Mono<FullArtistMetadata> getArtist(UUID artistId) {
        return artistRepository.findById(artistId)
                .switchIfEmpty(Mono.error(new NotFoundException("Artist with id " + artistId + " was not found")))
                .map(FullArtistMetadata::new);
    }

    public Mono<List<SimpleAlbumMetadata>> getAlbumsForArtist(UUID artistId, Order order, ArtistRole artistRole) {
        return albumService.getAlbumsForArtist(artistId, order, artistRole);
    }

    public Mono<ArtistMetadataAbstract> registerNewArtist(ArtistRegisterRequest dto) {
        var artist = Artist.builder()
                .name(dto.getName())
                .socialLink(dto.getSocialLink())
                .avatarKey(dto.getAvatarKey())
                .build();

        artist.setId(UUID.randomUUID());

        return artistRepository.insert(artist).map(FullArtistMetadata::new);
    }


}
