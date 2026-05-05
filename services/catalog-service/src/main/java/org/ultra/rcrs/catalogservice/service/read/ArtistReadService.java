package org.ultra.rcrs.catalogservice.service.read;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.catalogservice.dto.response.album.AlbumOfArtistDto;
import org.ultra.rcrs.catalogservice.dto.response.artist.ArtistDto;
import org.ultra.rcrs.catalogservice.dto.response.artist.ArtistStandaloneDto;
import org.ultra.rcrs.catalogservice.model.read.ArtistView;
import org.ultra.rcrs.catalogservice.repository.read.ArtistAlbumViewRepository;
import org.ultra.rcrs.catalogservice.repository.read.ArtistViewRepository;
import org.ultra.rcrs.catalogservice.service.AlbumConverter;
import org.ultra.rcrs.catalogservice.service.ArtistConverter;
import org.ultra.rcrs.enums.AlbumType;
import org.ultra.rcrs.enums.ArtistRole;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.exceptions.NotFoundException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ArtistReadService {

    private final ArtistViewRepository artistRepository;
    private final ArtistAlbumViewRepository artistAlbumViewRepository;
    private final ArtistConverter artistConverter;
    private final AlbumConverter albumConverter;

    @Cacheable("artists")
    public Mono<ArtistDto> getArtist(UUID artistId) {
        return artistRepository.findById(artistId)
                .switchIfEmpty(Mono.error(new NotFoundException("Artist", artistId)))
                .map(artistConverter::toDto);
    }

    public Mono<List<ArtistStandaloneDto>> getArtists(List<UUID> ids) {
        return artistRepository.findAllById(ids)
                .collect(Collectors.toMap(ArtistView::getId, Function.identity()))
                .map(m -> ids.stream()
                        .map(m::get)
                        .toList())
                .map(l -> l.stream().map(artistConverter::toStandaloneDto).toList());
    }

    public Mono<List<AlbumOfArtistDto>> getAlbumsForArtist(UUID artistId, List<EntityStatus> statuses, ArtistRole role, AlbumType type, Sort.Direction direction) {
        return artistAlbumViewRepository.findAllByArtist(artistId, statuses, role, type, direction)
                .map(albumConverter::toOfArtistDto)
                .collectList();
    }
}
