package org.ultra.rcrs.metadata.service.publ;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.exceptions.NotFoundException;
import org.ultra.rcrs.metadata.dto.ArtistPublicViewDto;
import org.ultra.rcrs.metadata.model.ArtistPublicDocument;
import org.ultra.rcrs.metadata.repository.ArtistDocumentRepository;
import org.ultra.rcrs.utils.S3Utils;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArtistPublicService {

    private final ArtistDocumentRepository artistDocumentRepository;
    private final S3Utils s3Utils;

    @Cacheable("artists-public")
    public Mono<ArtistPublicViewDto> getById(String id) {
        return artistDocumentRepository.findByIdForPublic(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Artist", id)))
                .map(this::toDto);
    }

    private ArtistPublicViewDto toDto(ArtistPublicDocument doc) {
        return ArtistPublicViewDto.builder()
                .id(doc.getId())
                .name(doc.getName())
                .avatarUrl(s3Utils.parseUrl(doc.getAvatarS3Key()))
                .socialLinks(doc.getSocialLinks() != null
                        ? doc.getSocialLinks().stream().map(s -> ArtistPublicViewDto.SocialLinkEmbed.builder()
                                .resourceName(s.getResourceName())
                                .url(s.getUrl())
                                .build()).collect(Collectors.toList())
                        : null)
                .tags(doc.getTags())
                .availabilityStatus(doc.getAvailabilityStatus())
                .build();
    }
}
