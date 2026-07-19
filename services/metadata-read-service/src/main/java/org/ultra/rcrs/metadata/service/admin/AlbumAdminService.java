package org.ultra.rcrs.metadata.service.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.metadata.dto.AlbumAdminViewDto;
import org.ultra.rcrs.metadata.model.AlbumPublicDocument;
import org.ultra.rcrs.metadata.repository.AlbumDocumentRepository;
import org.ultra.rcrs.utils.S3Utils;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlbumAdminService {

    private final AlbumDocumentRepository albumDocumentRepository;
    private final S3Utils s3Utils;

    @Cacheable("albums-admin")
    public Mono<AlbumAdminViewDto> getById(String id) {
        return albumDocumentRepository.findByIdForAdmin(id)
                .map(this::toDto);
    }

    private AlbumAdminViewDto toDto(AlbumPublicDocument doc) {
        return AlbumAdminViewDto.builder()
                .id(doc.getId())
                .lifecycleStatus(doc.getLifecycleStatus())
                .availabilityStatus(doc.getAvailabilityStatus())
                .title(doc.getTitle())
                .type(doc.getType())
                .releaseDate(doc.getReleaseDate())
                .year(doc.getYear())
                .totalTracks(doc.getTotalTracks())
                .totalDurationMs(doc.getTotalDurationMs())
                .coverUrl(s3Utils.parseUrl(doc.getCoverS3Key()))
                .explicit(doc.getExplicit())
                .artists(doc.getArtists() != null
                        ? doc.getArtists().stream().map(a -> AlbumAdminViewDto.ArtistEmbed.builder()
                                .id(a.getId())
                                .name(a.getName())
                                .avatarUrl(s3Utils.parseUrl(a.getAvatarS3Key()))
                                .role(a.getRole())
                                .build()).collect(Collectors.toList())
                        : null)
                .build();
    }
}
