package org.ultra.rcrs.catalogservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.catalogservice.dto.response.album.AlbumFullDto;
import org.ultra.rcrs.catalogservice.dto.response.album.AlbumStandaloneDto;
import org.ultra.rcrs.catalogservice.dto.response.artist.ArtistOnAlbumDto;
import org.ultra.rcrs.catalogservice.dto.response.artist.ArtistOnTrackDto;
import org.ultra.rcrs.catalogservice.dto.response.track.TrackInAlbumDto;
import org.ultra.rcrs.catalogservice.model.AlbumDocument;
import org.ultra.rcrs.catalogservice.model.TrackDocument;
import org.ultra.rcrs.catalogservice.repository.AlbumDocumentRepository;
import org.ultra.rcrs.catalogservice.repository.TrackDocumentRepository;
import org.ultra.rcrs.enums.ArtistRole;
import org.ultra.rcrs.enums.LifecycleStatus;
import org.ultra.rcrs.exceptions.NotFoundException;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AlbumReadService {

    private final AlbumDocumentRepository albumDocumentRepository;
    private final TrackDocumentRepository trackDocumentRepository;

    @Cacheable("albums")
    public Mono<AlbumFullDto> getAlbum(UUID albumId, List<LifecycleStatus> statuses) {
        return albumDocumentRepository.findById(albumId.toString())
                .switchIfEmpty(Mono.error(new NotFoundException("Album", albumId)))
                .flatMap(album -> {
                    List<String> statusNames = statuses.stream().map(Enum::name).toList();
                    return trackDocumentRepository.findAll()
                            .filter(t -> albumId.toString().equals(t.getAlbum() != null ? t.getAlbum().getId() : null))
                            .filter(t -> statusNames.isEmpty() || statusNames.contains(t.getStatus()))
                            .sort(java.util.Comparator.comparing(TrackDocument::getTrackNumber, java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())))
                            .map(this::toTrackInAlbumDto)
                            .collectList()
                            .map(tracks -> toFullDto(album, tracks));
                });
    }

    public Mono<List<AlbumStandaloneDto>> getAlbums(List<UUID> ids, List<LifecycleStatus> statuses) {
        List<String> stringIds = ids.stream().map(UUID::toString).toList();
        List<String> statusNames = statuses.stream().map(Enum::name).toList();
        return albumDocumentRepository.findAllByIdIn(stringIds)
                .filter(a -> statusNames.isEmpty() || statusNames.contains(a.getStatus()))
                .map(this::toStandaloneDto)
                .collectList()
                .map(list -> {
                    var map = list.stream().collect(Collectors.toMap(AlbumStandaloneDto::getId, a -> a));
                    return ids.stream().map(id -> map.get(id.toString())).toList();
                });
    }

    public Mono<List<TrackInAlbumDto>> getTracksInAlbum(UUID albumId, List<LifecycleStatus> statuses) {
        List<String> statusNames = statuses.stream().map(Enum::name).toList();
        return trackDocumentRepository.findAll()
                .filter(t -> albumId.toString().equals(t.getAlbum() != null ? t.getAlbum().getId() : null))
                .filter(t -> statusNames.isEmpty() || statusNames.contains(t.getStatus()))
                .sort(java.util.Comparator.comparing(TrackDocument::getTrackNumber, java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())))
                .map(this::toTrackInAlbumDto)
                .collectList();
    }

    private AlbumFullDto toFullDto(AlbumDocument a, List<TrackInAlbumDto> tracks) {
        return AlbumFullDto.builder()
                .id(a.getId())
                .status(a.getStatus() != null ? LifecycleStatus.valueOf(a.getStatus()) : null)
                .title(a.getTitle())
                .type(a.getType() != null ? org.ultra.rcrs.enums.AlbumType.valueOf(a.getType()) : null)
                .releaseDate(parseLocalDate(a.getReleaseDate()))
                .year(a.getYear())
                .totalTracks(a.getTotalTracks())
                .totalDurationMs(a.getTotalDurationMs())
                .coverUrl(a.getCoverUrl())
                .explicit(a.getExplicit())
                .available(a.getAvailable())
                .artists(toArtistOnAlbumDtos(a.getArtists()))
                .tracks(tracks)
                .build();
    }

    private AlbumStandaloneDto toStandaloneDto(AlbumDocument a) {
        return AlbumStandaloneDto.builder()
                .id(a.getId())
                .status(a.getStatus() != null ? LifecycleStatus.valueOf(a.getStatus()) : null)
                .title(a.getTitle())
                .type(a.getType() != null ? org.ultra.rcrs.enums.AlbumType.valueOf(a.getType()) : null)
                .releaseDate(parseLocalDate(a.getReleaseDate()))
                .year(a.getYear())
                .totalTracks(a.getTotalTracks())
                .totalDurationMs(a.getTotalDurationMs())
                .coverUrl(a.getCoverUrl())
                .explicit(a.getExplicit())
                .available(a.getAvailable())
                .artists(toArtistOnAlbumDtos(a.getArtists()))
                .build();
    }

    private TrackInAlbumDto toTrackInAlbumDto(TrackDocument t) {
        return TrackInAlbumDto.builder()
                .id(t.getId())
                .status(t.getStatus() != null ? LifecycleStatus.valueOf(t.getStatus()) : null)
                .title(t.getTitle())
                .durationMs(t.getDurationMs())
                .trackNumber(t.getTrackNumber())
                .explicit(t.getExplicit())
                .available(t.getAvailable())
                .artists(t.getArtists() != null
                        ? t.getArtists().stream().map(ar -> ArtistOnTrackDto.builder()
                        .id(ar.getId()).name(ar.getName()).avatarUrl(ar.getAvatarUrl())
                        .role(ar.getRole() != null ? ArtistRole.valueOf(ar.getRole()) : null)
                        .build()).toList()
                        : List.of())
                .build();
    }

    private List<ArtistOnAlbumDto> toArtistOnAlbumDtos(List<AlbumDocument.ArtistEmbed> artists) {
        if (artists == null) return List.of();
        return artists.stream().map(a -> ArtistOnAlbumDto.builder()
                .id(a.getId()).name(a.getName()).avatarUrl(a.getAvatarUrl())
                .role(a.getRole() != null ? ArtistRole.valueOf(a.getRole()) : null)
                .build()).toList();
    }

    private LocalDate parseLocalDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        try {
            return LocalDate.parse(dateStr.length() > 10 ? dateStr.substring(0, 10) : dateStr);
        } catch (Exception e) {
            return null;
        }
    }
}
