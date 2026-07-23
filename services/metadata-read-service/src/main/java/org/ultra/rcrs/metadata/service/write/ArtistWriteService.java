package org.ultra.rcrs.metadata.service.write;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.metadata.model.ArtistDocument;
import org.ultra.rcrs.metadata.repository.ArtistDocumentRepository;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.events.artist.ArtistCreatedEventOuterClass;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArtistWriteService {

    private final ArtistDocumentRepository artistDocumentRepository;

    public void handleArtistCreated(ArtistCreatedEventOuterClass.ArtistCreatedEvent event) {
        ArtistDocument doc = ArtistDocument.builder()
                .id(event.getId())
                .name(event.getName())
                .avatarS3Key(event.getAvatarS3Key())
                .socialLinks(event.getSocialLinksList().stream()
                        .map(sl -> ArtistDocument.SocialLinkEmbed.builder()
                                .resourceName(sl.getResourceName())
                                .url(sl.getUrl())
                                .build())
                        .collect(Collectors.toList()))
                .tags(event.getTagsList())
                .availabilityStatus(EntityStatus.valueOf(event.getAvailabilityStatus().name()))
                .build();

        artistDocumentRepository.save(doc)
                .doOnSuccess(d -> log.info("Saved artist document: id={}", d.getId()))
                .doOnError(e -> log.error("Failed to save artist document: id={}, error={}", event.getId(), e.getMessage()))
                .block();
    }

    public void handleArtistDeleted(String id) {
        artistDocumentRepository.findById(id)
                .flatMap(doc -> {
                    doc.setAvailabilityStatus(EntityStatus.DELETED);
                    return artistDocumentRepository.save(doc);
                })
                .doOnSuccess(d -> log.info("Marked artist as deleted: id={}", id))
                .doOnError(e -> log.error("Failed to delete artist: id={}, error={}", id, e.getMessage()))
                .block();
    }

    public void handleArtistHidden(String id) {
        artistDocumentRepository.findById(id)
                .flatMap(doc -> {
                    doc.setAvailabilityStatus(EntityStatus.HIDDEN);
                    return artistDocumentRepository.save(doc);
                })
                .doOnSuccess(d -> log.info("Marked artist as hidden: id={}", id))
                .doOnError(e -> log.error("Failed to hide artist: id={}, error={}", id, e.getMessage()))
                .block();
    }

    public void handleArtistActivated(String id) {
        artistDocumentRepository.findById(id)
                .flatMap(doc -> {
                    doc.setAvailabilityStatus(EntityStatus.ACTIVE);
                    return artistDocumentRepository.save(doc);
                })
                .doOnSuccess(d -> log.info("Marked artist as active: id={}", id))
                .doOnError(e -> log.error("Failed to activate artist: id={}, error={}", id, e.getMessage()))
                .block();
    }

    public void handleArtistTrueDeleted(String id) {
        artistDocumentRepository.deleteById(id)
                .doOnSuccess(v -> log.info("Permanently deleted artist document: id={}", id))
                .doOnError(e -> log.error("Failed to permanently delete artist: id={}, error={}", id, e.getMessage()))
                .block();
    }
}
