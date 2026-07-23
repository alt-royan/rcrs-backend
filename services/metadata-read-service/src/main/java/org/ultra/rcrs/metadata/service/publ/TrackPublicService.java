package org.ultra.rcrs.metadata.service.publ;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.exceptions.NotFoundException;
import org.ultra.rcrs.metadata.dto.TrackPublicStandaloneDto;
import org.ultra.rcrs.metadata.dto.TrackPublicViewDto;
import org.ultra.rcrs.metadata.model.TrackDocument;
import org.ultra.rcrs.metadata.repository.TrackDocumentRepository;
import org.ultra.rcrs.utils.S3Utils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrackPublicService {

    private final TrackDocumentRepository trackDocumentRepository;
    private final S3Utils s3Utils;

    @Cacheable("tracks-public")
    public Mono<TrackPublicViewDto> getById(String id) {
        return trackDocumentRepository.findByIdForPublic(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Track", id)))
                .map(this::toDto);
    }

    @Cacheable("tracks-by-album-public")
    public Flux<TrackPublicStandaloneDto> getAllByAlbumId(String albumId) {
        return trackDocumentRepository.findAllByAlbumIdForPublic(albumId, Sort.by("trackNumber"))
                .map(this::toStandaloneDto);
    }

    private TrackPublicViewDto toDto(TrackDocument doc) {
        return TrackPublicViewDto.builder()
                .id(doc.getId())
                .availabilityStatus(doc.getAvailabilityStatus())
                .title(doc.getTitle())
                .releaseDate(doc.getReleaseDate())
                .durationMs(doc.getDurationMs())
                .trackNumber(doc.getTrackNumber())
                .explicit(doc.getExplicit())
                .album(doc.getAlbum() != null
                        ? TrackPublicViewDto.AlbumEmbed.builder()
                                .id(doc.getAlbum().getId())
                                .title(doc.getAlbum().getTitle())
                                .coverUrl(s3Utils.parseUrl(doc.getAlbum().getCoverS3Key()))
                                .build()
                        : null)
                .artists(doc.getArtists() != null
                        ? doc.getArtists().stream().map(a -> TrackPublicViewDto.ArtistEmbed.builder()
                                .id(a.getId())
                                .name(a.getName())
                                .avatarUrl(s3Utils.parseUrl(a.getAvatarS3Key()))
                                .role(a.getRole())
                                .build()).collect(Collectors.toList())
                        : null)
                .others(doc.getOthers() != null
                        ? doc.getOthers().stream().map(o -> TrackPublicViewDto.OtherArtistEmbed.builder()
                                .id(o.getId())
                                .name(o.getName())
                                .roles(o.getRoles())
                                .socialLinks(o.getSocialLinks() != null
                                        ? o.getSocialLinks().stream().map(s -> TrackPublicViewDto.SocialLinkEmbed.builder()
                                                .resourceName(s.getResourceName())
                                                .url(s.getUrl())
                                                .build()).collect(Collectors.toList())
                                        : null)
                                .build()).collect(Collectors.toList())
                        : null)
                .build();
    }

    private TrackPublicStandaloneDto toStandaloneDto(TrackDocument doc) {
        return TrackPublicStandaloneDto.builder()
                .id(doc.getId())
                .availabilityStatus(doc.getAvailabilityStatus())
                .title(doc.getTitle())
                .durationMs(doc.getDurationMs())
                .trackNumber(doc.getTrackNumber())
                .explicit(doc.getExplicit())
                .artists(doc.getArtists() != null
                        ? doc.getArtists().stream().map(a -> TrackPublicStandaloneDto.ArtistEmbed.builder()
                                .id(a.getId())
                                .name(a.getName())
                                .avatarUrl(s3Utils.parseUrl(a.getAvatarS3Key()))
                                .role(a.getRole())
                                .build()).collect(Collectors.toList())
                        : null)
                .build();
    }
}
