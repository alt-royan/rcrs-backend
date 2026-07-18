package org.ultra.rcrs.catalogservice.service.publ;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.catalogservice.dto.ArtistPublicViewDto;
import org.ultra.rcrs.catalogservice.model.ArtistPublicDocument;
import org.ultra.rcrs.catalogservice.repository.ArtistDocumentRepository;
import org.ultra.rcrs.utils.S3Utils;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArtistPublicService {

    private final ArtistDocumentRepository artistDocumentRepository;
    private final S3Utils s3Utils;

    public Mono<ArtistPublicViewDto> getById(String id) {
        return artistDocumentRepository.findByIdForPublic(id)
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
