package org.ultra.rcrs.catalogservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.catalogservice.dto.OtherArtistDto;
import org.ultra.rcrs.catalogservice.dto.SocialLinkDto;
import org.ultra.rcrs.catalogservice.dto.response.album.AlbumSimpleDto;
import org.ultra.rcrs.catalogservice.dto.response.artist.ArtistOnTrackDto;
import org.ultra.rcrs.catalogservice.dto.response.track.TrackFullDto;
import org.ultra.rcrs.catalogservice.dto.response.track.TrackStandaloneDto;
import org.ultra.rcrs.catalogservice.model.TrackDocument;
import org.ultra.rcrs.catalogservice.repository.TrackDocumentRepository;
import org.ultra.rcrs.enums.ArtistRole;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.exceptions.NotFoundException;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TrackReadService {

    private final TrackDocumentRepository trackDocumentRepository;

    @Cacheable("tracks")
    public Mono<TrackFullDto> getTrack(UUID trackId, List<EntityStatus> statuses) {
        return trackDocumentRepository.findById(trackId.toString())
                .switchIfEmpty(Mono.error(new NotFoundException("Track", trackId)))
                .map(this::toFullDto);
    }

    public Mono<List<TrackStandaloneDto>> getTracks(List<UUID> ids, List<EntityStatus> statuses) {
        List<String> stringIds = ids.stream().map(UUID::toString).toList();
        List<String> statusNames = statuses.stream().map(Enum::name).toList();
        return trackDocumentRepository.findAllByIdIn(stringIds)
                .filter(t -> statusNames.isEmpty() || statusNames.contains(t.getStatus()))
                .map(this::toStandaloneDto)
                .collectList()
                .map(list -> {
                    var map = list.stream().collect(Collectors.toMap(TrackStandaloneDto::getId, t -> t));
                    return ids.stream().map(id -> map.get(id.toString())).toList();
                });
    }

    private TrackFullDto toFullDto(TrackDocument t) {
        return TrackFullDto.builder()
                .id(t.getId())
                .status(t.getStatus() != null ? EntityStatus.valueOf(t.getStatus()) : null)
                .title(t.getTitle())
                .releaseDate(parseLocalDate(t.getReleaseDate()))
                .durationMs(t.getDurationMs())
                .trackNumber(t.getTrackNumber())
                .explicit(t.getExplicit())
                .available(t.getAvailable())
                .album(t.getAlbum() != null ? AlbumSimpleDto.builder()
                        .id(t.getAlbum().getId())
                        .title(t.getAlbum().getTitle())
                        .coverUrl(t.getAlbum().getCoverUrl())
                        .build() : null)
                .artists(toArtistOnTrackDtos(t.getArtists()))
                .others(toOtherArtistDtos(t.getOthers()))
                .build();
    }

    private TrackStandaloneDto toStandaloneDto(TrackDocument t) {
        return TrackStandaloneDto.builder()
                .id(t.getId())
                .status(t.getStatus() != null ? EntityStatus.valueOf(t.getStatus()) : null)
                .title(t.getTitle())
                .releaseDate(parseLocalDate(t.getReleaseDate()))
                .durationMs(t.getDurationMs())
                .trackNumber(t.getTrackNumber())
                .explicit(t.getExplicit())
                .available(t.getAvailable())
                .album(t.getAlbum() != null ? AlbumSimpleDto.builder()
                        .id(t.getAlbum().getId())
                        .title(t.getAlbum().getTitle())
                        .coverUrl(t.getAlbum().getCoverUrl())
                        .build() : null)
                .artists(toArtistOnTrackDtos(t.getArtists()))
                .build();
    }

    private List<ArtistOnTrackDto> toArtistOnTrackDtos(List<TrackDocument.ArtistEmbed> artists) {
        if (artists == null) return List.of();
        return artists.stream().map(a -> ArtistOnTrackDto.builder()
                .id(a.getId()).name(a.getName()).avatarUrl(a.getAvatarUrl())
                .role(a.getRole() != null ? ArtistRole.valueOf(a.getRole()) : null)
                .build()).toList();
    }

    private List<OtherArtistDto> toOtherArtistDtos(List<TrackDocument.OtherArtistEmbed> others) {
        if (others == null) return List.of();
        return others.stream().map(o -> {
            Set<ArtistRole> roles = o.getRoles() != null
                    ? o.getRoles().stream().map(ArtistRole::valueOf).collect(Collectors.toSet())
                    : Set.of();
            List<SocialLinkDto> socialLinks = o.getSocialLinks() != null
                    ? o.getSocialLinks().stream().map(s -> new SocialLinkDto(s.getResourceName(), s.getUrl())).toList()
                    : List.of();
            OtherArtistDto dto = new OtherArtistDto();
            dto.setName(o.getName());
            dto.setRoles(roles);
            dto.setSocialLinks(socialLinks);
            return dto;
        }).toList();
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
