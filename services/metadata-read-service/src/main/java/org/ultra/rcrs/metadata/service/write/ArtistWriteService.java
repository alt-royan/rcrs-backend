package org.ultra.rcrs.metadata.service.write;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.metadata.model.ArtistPublicDocument;
import org.ultra.rcrs.metadata.repository.ArtistDocumentRepository;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.events.artist.ArtistCreatedEventOuterClass;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArtistWriteService {

    private final ArtistDocumentRepository artistDocumentRepository;

    public Mono<ArtistPublicDocument> handleArtistCreated(ArtistCreatedEventOuterClass.ArtistCreatedEvent event) {
        ArtistPublicDocument doc = ArtistPublicDocument.builder()
                .id(event.getId())
                .name(event.getName())
                .avatarS3Key(event.getAvatarS3Key())
                .socialLinks(event.getSocialLinksList().stream()
                        .map(sl -> ArtistPublicDocument.SocialLinkEmbed.builder()
                                .resourceName(sl.getResourceName())
                                .url(sl.getUrl())
                                .build())
                        .collect(Collectors.toList()))
                .tags(event.getTagsList())
                .availabilityStatus(EntityStatus.valueOf(event.getAvailabilityStatus().name()))
                .build();

        return artistDocumentRepository.save(doc)
                .doOnSuccess(d -> log.info("Saved artist document: id={}", d.getId()))
                .doOnError(e -> log.error("Failed to save artist document: id={}, error={}", event.getId(), e.getMessage()));
    }

    public Mono<Void> handleArtistDeleted(String id) {
        return artistDocumentRepository.findById(id)
                .flatMap(doc -> {
                    doc.setAvailabilityStatus(EntityStatus.DELETED);
                    return artistDocumentRepository.save(doc);
                })
                .doOnSuccess(d -> log.info("Marked artist as deleted: id={}", id))
                .doOnError(e -> log.error("Failed to delete artist: id={}, error={}", id, e.getMessage()))
                .then();
    }

    public Mono<Void> handleArtistHidden(String id) {
        return artistDocumentRepository.findById(id)
                .flatMap(doc -> {
                    doc.setAvailabilityStatus(EntityStatus.HIDDEN);
                    return artistDocumentRepository.save(doc);
                })
                .doOnSuccess(d -> log.info("Marked artist as hidden: id={}", id))
                .doOnError(e -> log.error("Failed to hide artist: id={}, error={}", id, e.getMessage()))
                .then();
    }
}
