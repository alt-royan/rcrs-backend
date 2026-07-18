package org.ultra.rcrs.catalogservice.service.publ;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.catalogservice.dto.TrackPublicStandaloneDto;
import org.ultra.rcrs.catalogservice.dto.TrackPublicViewDto;
import org.ultra.rcrs.catalogservice.model.TrackPublicDocument;
import org.ultra.rcrs.catalogservice.repository.TrackDocumentRepository;
import org.ultra.rcrs.utils.S3Utils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrackPublicService {

    private final TrackDocumentRepository trackDocumentRepository;
    private final S3Utils s3Utils;

    public Mono<TrackPublicViewDto> getById(String id) {
        return trackDocumentRepository.findByIdForPublic(id)
                .map(this::toDto);
    }

    public Flux<TrackPublicStandaloneDto> getByAlbumId(String albumId) {
        return trackDocumentRepository.findByAlbumIdForPublic(albumId)
                .map(this::toStandaloneDto);
    }

    private TrackPublicViewDto toDto(TrackPublicDocument doc) {
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

    private TrackPublicStandaloneDto toStandaloneDto(TrackPublicDocument doc) {
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
