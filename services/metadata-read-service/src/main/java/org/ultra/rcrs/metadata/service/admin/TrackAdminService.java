package org.ultra.rcrs.metadata.service.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.metadata.dto.TrackAdminStandaloneDto;
import org.ultra.rcrs.metadata.dto.TrackAdminViewDto;
import org.ultra.rcrs.metadata.model.TrackPublicDocument;
import org.ultra.rcrs.metadata.repository.TrackDocumentRepository;
import org.ultra.rcrs.utils.S3Utils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrackAdminService {

    private final TrackDocumentRepository trackDocumentRepository;
    private final S3Utils s3Utils;

    @Cacheable("tracks-admin")
    public Mono<TrackAdminViewDto> getById(String id) {
        return trackDocumentRepository.findByIdForAdmin(id)
                .map(this::toDto);
    }

    @Cacheable("tracks-by-album-admin")
    public Flux<TrackAdminStandaloneDto> getAllByAlbumId(String albumId) {
        return trackDocumentRepository.findAllByAlbumIdForAdmin(albumId)
                .map(this::toStandaloneDto);
    }

    private TrackAdminViewDto toDto(TrackPublicDocument doc) {
        return TrackAdminViewDto.builder()
                .id(doc.getId())
                .lifecycleStatus(doc.getLifecycleStatus())
                .availabilityStatus(doc.getAvailabilityStatus())
                .title(doc.getTitle())
                .releaseDate(doc.getReleaseDate())
                .durationMs(doc.getDurationMs())
                .trackNumber(doc.getTrackNumber())
                .explicit(doc.getExplicit())
                .album(doc.getAlbum() != null
                        ? TrackAdminViewDto.AlbumEmbed.builder()
                                .id(doc.getAlbum().getId())
                                .title(doc.getAlbum().getTitle())
                                .coverUrl(s3Utils.parseUrl(doc.getAlbum().getCoverS3Key()))
                                .build()
                        : null)
                .artists(doc.getArtists() != null
                        ? doc.getArtists().stream().map(a -> TrackAdminViewDto.ArtistEmbed.builder()
                                .id(a.getId())
                                .name(a.getName())
                                .avatarUrl(s3Utils.parseUrl(a.getAvatarS3Key()))
                                .role(a.getRole())
                                .build()).collect(Collectors.toList())
                        : null)
                .others(doc.getOthers() != null
                        ? doc.getOthers().stream().map(o -> TrackAdminViewDto.OtherArtistEmbed.builder()
                                .id(o.getId())
                                .name(o.getName())
                                .roles(o.getRoles())
                                .socialLinks(o.getSocialLinks() != null
                                        ? o.getSocialLinks().stream().map(s -> TrackAdminViewDto.SocialLinkEmbed.builder()
                                                .resourceName(s.getResourceName())
                                                .url(s.getUrl())
                                                .build()).collect(Collectors.toList())
                                        : null)
                                .build()).collect(Collectors.toList())
                        : null)
                .build();
    }

    private TrackAdminStandaloneDto toStandaloneDto(TrackPublicDocument doc) {
        return TrackAdminStandaloneDto.builder()
                .id(doc.getId())
                .lifecycleStatus(doc.getLifecycleStatus())
                .availabilityStatus(doc.getAvailabilityStatus())
                .title(doc.getTitle())
                .durationMs(doc.getDurationMs())
                .trackNumber(doc.getTrackNumber())
                .explicit(doc.getExplicit())
                .artists(doc.getArtists() != null
                        ? doc.getArtists().stream().map(a -> TrackAdminStandaloneDto.ArtistEmbed.builder()
                                .id(a.getId())
                                .name(a.getName())
                                .avatarUrl(s3Utils.parseUrl(a.getAvatarS3Key()))
                                .role(a.getRole())
                                .build()).collect(Collectors.toList())
                        : null)
                .build();
    }
}
