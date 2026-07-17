package org.ultra.rcrs.catalogservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.catalogservice.dto.SocialLinkDto;
import org.ultra.rcrs.catalogservice.dto.response.album.AlbumOfArtistDto;
import org.ultra.rcrs.catalogservice.dto.response.artist.ArtistDto;
import org.ultra.rcrs.catalogservice.dto.response.artist.ArtistStandaloneDto;
import org.ultra.rcrs.catalogservice.model.AlbumDocument;
import org.ultra.rcrs.catalogservice.model.ArtistDocument;
import org.ultra.rcrs.catalogservice.repository.AlbumDocumentRepository;
import org.ultra.rcrs.catalogservice.repository.ArtistDocumentRepository;
import org.ultra.rcrs.enums.AlbumType;
import org.ultra.rcrs.enums.ArtistRole;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.exceptions.NotFoundException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ArtistReadService {

    private final ArtistDocumentRepository artistDocumentRepository;
    private final AlbumDocumentRepository albumDocumentRepository;

    @Cacheable("artists")
    public Mono<ArtistDto> getArtist(UUID artistId) {
        return artistDocumentRepository.findById(artistId.toString())
                .switchIfEmpty(Mono.error(new NotFoundException("Artist", artistId)))
                .map(a -> ArtistDto.builder()
                        .id(a.getId())
                        .name(a.getName())
                        .avatarUrl(a.getAvatarUrl())
                        .socialLinks(a.getSocialLinks() != null
                                ? a.getSocialLinks().stream()
                                .map(s -> new SocialLinkDto(s.getResourceName(), s.getUrl()))
                                .toList()
                                : List.of())
                        .build());
    }

    public Mono<List<ArtistStandaloneDto>> getArtists(List<UUID> ids) {
        List<String> stringIds = ids.stream().map(UUID::toString).toList();
        return artistDocumentRepository.findAllByIdIn(stringIds)
                .map(a -> ArtistStandaloneDto.builder()
                        .id(a.getId())
                        .name(a.getName())
                        .avatarUrl(a.getAvatarUrl())
                        .build())
                .collectList()
                .map(list -> {
                    var map = list.stream().collect(java.util.stream.Collectors.toMap(ArtistStandaloneDto::getId, a -> a));
                    return ids.stream().map(id -> map.get(id.toString())).toList();
                });
    }

    public Mono<List<AlbumOfArtistDto>> getAlbumsForArtist(UUID artistId, List<EntityStatus> statuses, ArtistRole role, AlbumType type, Sort.Direction direction) {
        direction = direction == null ? Sort.Direction.DESC : direction;
        List<String> statusNames = statuses.stream().map(Enum::name).toList();
        return albumDocumentRepository.findByArtistsIdAndStatusIn(artistId.toString(), statusNames, Sort.by(direction, "releaseDate"))
                .map(a -> {
                    var albumType = a.getType() != null ? AlbumType.valueOf(a.getType()) : AlbumType.ALBUM;
                    if (type != null && !type.equals(albumType)) return null;

                    ArtistRole artistRole = null;
                    if (a.getArtists() != null) {
                        artistRole = a.getArtists().stream()
                                .filter(ar -> artistId.toString().equals(ar.getId()))
                                .map(ar -> ar.getRole() != null ? ArtistRole.valueOf(ar.getRole()) : null)
                                .findFirst().orElse(null);
                    }
                    if (role != null && !role.equals(artistRole)) return null;

                    return AlbumOfArtistDto.builder()
                            .id(a.getId())
                            .artistRole(artistRole)
                            .status(a.getStatus() != null ? EntityStatus.valueOf(a.getStatus()) : null)
                            .title(a.getTitle())
                            .type(albumType)
                            .releaseDate(a.getReleaseDate() != null && !a.getReleaseDate().isEmpty()
                                    ? java.time.LocalDate.parse(a.getReleaseDate().length() > 10 ? a.getReleaseDate().substring(0, 10) : a.getReleaseDate()) : null)
                            .year(a.getYear())
                            .totalTracks(a.getTotalTracks())
                            .totalDurationMs(a.getTotalDurationMs())
                            .coverUrl(a.getCoverUrl())
                            .explicit(a.getExplicit())
                            .available(a.getAvailable())
                            .artists(a.getArtists() != null
                                    ? a.getArtists().stream().map(ar -> org.ultra.rcrs.catalogservice.dto.response.artist.ArtistOnAlbumDto.builder()
                                    .id(ar.getId()).name(ar.getName()).avatarUrl(ar.getAvatarUrl())
                                    .role(ar.getRole() != null ? ArtistRole.valueOf(ar.getRole()) : null)
                                    .build()).toList()
                                    : List.of())
                            .build();
                })
                .filter(java.util.Objects::nonNull)
                .collectList();
    }
}
