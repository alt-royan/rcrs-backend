package org.ultra.rcrs.catalogservice.service.read;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.catalogservice.dto.response.album.AlbumOfArtistDto;
import org.ultra.rcrs.catalogservice.dto.response.artist.ArtistDto;
import org.ultra.rcrs.catalogservice.repository.read.ArtistAlbumViewRepository;
import org.ultra.rcrs.catalogservice.repository.read.ArtistViewRepository;
import org.ultra.rcrs.catalogservice.service.ArtistConverter;
import org.ultra.rcrs.catalogservice.utils.S3Utils;
import org.ultra.rcrs.enums.AlbumType;
import org.ultra.rcrs.enums.ArtistRole;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.exceptions.NotFoundException;
import org.ultra.rcrs.utils.Url62;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ArtistReadService {

    private final ArtistViewRepository artistRepository;
    private final ArtistAlbumViewRepository artistAlbumViewRepository;
    private final ArtistConverter artistConverter;
    private final S3Utils s3Utils;

    @Cacheable("artists")
    public Mono<ArtistDto> getArtist(UUID artistId) {
        return artistRepository.findById(artistId)
                .switchIfEmpty(Mono.error(new NotFoundException("Artist", artistId)))
                .map(artistConverter::toDto);
    }

    public Mono<List<AlbumOfArtistDto>> getAlbumsForArtist(UUID artistId, List<EntityStatus> statuses, ArtistRole role, AlbumType type, Sort.Direction direction) {
        return artistAlbumViewRepository.findAllByArtist(artistId, statuses, role, type, direction)
                .map(a -> AlbumOfArtistDto.builder()
                        .id(Url62.encode(a.getAlbumId()))
                        .artistRole(a.getArtistRole())
                        .status(a.getStatus())
                        .title(a.getTitle())
                        .type(a.getType())
                        .releaseDate(a.getReleaseDate())
                        .year(a.getYear())
                        .totalTracks(a.getTotalTracks())
                        .totalDurationMs(a.getTotalDurationMs())
                        .coverUrl(s3Utils.parseUrl(a.getCoverS3Key()))
                        .explicit(a.getExplicit())
                        .available(a.getAvailable())
                        .artists(artistConverter.onAlbumToDto(a.getArtists()))
                        .build()
                )
                .collectList();
    }
}
