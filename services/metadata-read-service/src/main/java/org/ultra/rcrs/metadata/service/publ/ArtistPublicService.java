package org.ultra.rcrs.metadata.service.publ;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.exceptions.NotFoundException;
import org.ultra.rcrs.metadata.dto.ArtistPublicStandaloneDto;
import org.ultra.rcrs.metadata.dto.ArtistPublicViewDto;
import org.ultra.rcrs.metadata.model.ArtistDocument;
import org.ultra.rcrs.metadata.repository.ArtistDocumentRepository;
import org.ultra.rcrs.utils.ImageUtils;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArtistPublicService {

    private final ArtistDocumentRepository artistDocumentRepository;
    private final ImageUtils imageUtils;

    public Mono<ArtistPublicViewDto> getById(String id) {
        return artistDocumentRepository.findByIdForPublic(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Artist", id)))
                .map(this::toDto);
    }

    /**
     * Batch lookup for callers that hold a list of artist ids — a library screen,
     * a playlist listing — and would otherwise issue one request per artist.
     * Artists that are not publicly available are simply absent from the result.
     */
    public Mono<List<ArtistPublicStandaloneDto>> getAllByIds(List<String> ids) {
        return artistDocumentRepository.findAllByIdInForPublic(ids)
                .map(this::toStandaloneDto)
                .collectList();
    }

    private ArtistPublicViewDto toDto(ArtistDocument doc) {
        return ArtistPublicViewDto.builder()
                .id(doc.getId())
                .name(doc.getName())
                .avatar(imageUtils.parseUrls(doc.getAvatarS3Key()))
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

    private ArtistPublicStandaloneDto toStandaloneDto(ArtistDocument doc) {
        return ArtistPublicStandaloneDto.builder()
                .id(doc.getId())
                .name(doc.getName())
                .avatar(imageUtils.parseUrls(doc.getAvatarS3Key()))
                .tags(doc.getTags())
                .availabilityStatus(doc.getAvailabilityStatus())
                .build();
    }
}
