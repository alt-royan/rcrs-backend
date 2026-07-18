package org.ultra.rcrs.catalogservice.service.publ;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.catalogservice.dto.AlbumPublicViewDto;
import org.ultra.rcrs.catalogservice.model.AlbumPublicDocument;
import org.ultra.rcrs.catalogservice.repository.AlbumDocumentRepository;
import org.ultra.rcrs.utils.S3Utils;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlbumPublicService {

    private final AlbumDocumentRepository albumDocumentRepository;
    private final S3Utils s3Utils;

    public Mono<AlbumPublicViewDto> getById(String id) {
        return albumDocumentRepository.findByIdForPublic(id)
                .map(this::toDto);
    }

    private AlbumPublicViewDto toDto(AlbumPublicDocument doc) {
        return AlbumPublicViewDto.builder()
                .id(doc.getId())
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
                        ? doc.getArtists().stream().map(a -> AlbumPublicViewDto.ArtistEmbed.builder()
                                .id(a.getId())
                                .name(a.getName())
                                .avatarUrl(s3Utils.parseUrl(a.getAvatarS3Key()))
                                .role(a.getRole())
                                .build()).collect(Collectors.toList())
                        : null)
                .build();
    }
}
