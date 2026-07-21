package org.ultra.rcrs.metadata.service.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.exceptions.NotFoundException;
import org.ultra.rcrs.metadata.dto.ArtistAdminViewDto;
import org.ultra.rcrs.metadata.model.ArtistPublicDocument;
import org.ultra.rcrs.metadata.repository.ArtistDocumentRepository;
import org.ultra.rcrs.utils.S3Utils;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArtistAdminService {

    private final ArtistDocumentRepository artistDocumentRepository;
    private final S3Utils s3Utils;

    @Cacheable("artists-admin")
    public Mono<ArtistAdminViewDto> getById(String id) {
        return artistDocumentRepository.findByIdForAdmin(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Artist", id)))
                .map(this::toDto);
    }

    private ArtistAdminViewDto toDto(ArtistPublicDocument doc) {
        return ArtistAdminViewDto.builder()
                .id(doc.getId())
                .name(doc.getName())
                .avatarUrl(s3Utils.parseUrl(doc.getAvatarS3Key()))
                .socialLinks(doc.getSocialLinks() != null
                        ? doc.getSocialLinks().stream().map(s -> ArtistAdminViewDto.SocialLinkEmbed.builder()
                                .resourceName(s.getResourceName())
                                .url(s.getUrl())
                                .build()).collect(Collectors.toList())
                        : null)
                .tags(doc.getTags())
                .availabilityStatus(doc.getAvailabilityStatus())
                .build();
    }
}
