package org.ultra.rcrs.catalogservice.service.write;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.catalogservice.model.AlbumPublicDocument;
import org.ultra.rcrs.catalogservice.model.ArtistPublicDocument;
import org.ultra.rcrs.catalogservice.repository.AlbumDocumentRepository;
import org.ultra.rcrs.catalogservice.repository.ArtistDocumentRepository;
import org.ultra.rcrs.enums.ArtistRole;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.enums.LifecycleStatus;
import org.ultra.rcrs.events.album.AlbumCreatedEventOuterClass;
import org.ultra.rcrs.events.album.AlbumUpdateLifecycleStatusEventOuterClass;
import org.ultra.rcrs.events.album.ArtistAddedToAlbumEventOuterClass;
import org.ultra.rcrs.events.album.ArtistDeletedFromAlbumEventOuterClass;
import reactor.core.publisher.Mono;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlbumWriteService {

    private final AlbumDocumentRepository albumDocumentRepository;
    private final ArtistDocumentRepository artistDocumentRepository;

    public Mono<AlbumPublicDocument> handleAlbumCreated(AlbumCreatedEventOuterClass.AlbumCreatedEvent event) {
        AlbumPublicDocument doc = AlbumPublicDocument.builder()
                .id(event.getId())
                .title(event.getTitle())
                .type(org.ultra.rcrs.enums.AlbumType.valueOf(event.getType().name()))
                .releaseDate(event.hasReleaseDate()
                        ? java.time.Instant.ofEpochSecond(event.getReleaseDate().getSeconds(), event.getReleaseDate().getNanos()).toString()
                        : null)
                .coverS3Key(event.getCoverS3Key())
                .availabilityStatus(EntityStatus.valueOf(event.getAvailabilityStatus().name()))
                .lifecycleStatus(LifecycleStatus.valueOf(event.getLifecycleStatus().name()))
                .build();

        return albumDocumentRepository.save(doc)
                .doOnSuccess(d -> log.info("Saved album document: id={}", d.getId()))
                .doOnError(e -> log.error("Failed to save album document: id={}, error={}", event.getId(), e.getMessage()));
    }

    public Mono<Void> handleAlbumDeleted(String id) {
        return albumDocumentRepository.findById(id)
                .flatMap(doc -> {
                    doc.setAvailabilityStatus(EntityStatus.DELETED);
                    return albumDocumentRepository.save(doc);
                })
                .doOnSuccess(d -> log.info("Marked album as deleted: id={}", id))
                .doOnError(e -> log.error("Failed to delete album: id={}, error={}", id, e.getMessage()))
                .then();
    }

    public Mono<Void> handleAlbumHidden(String id) {
        return albumDocumentRepository.findById(id)
                .flatMap(doc -> {
                    doc.setAvailabilityStatus(EntityStatus.HIDDEN);
                    return albumDocumentRepository.save(doc);
                })
                .doOnSuccess(d -> log.info("Marked album as hidden: id={}", id))
                .doOnError(e -> log.error("Failed to hide album: id={}, error={}", id, e.getMessage()))
                .then();
    }

    public Mono<Void> handleAlbumLifecycleStatusUpdated(AlbumUpdateLifecycleStatusEventOuterClass.AlbumUpdateLifecycleStatusEvent event) {
        return albumDocumentRepository.findById(event.getId())
                .flatMap(doc -> {
                    doc.setLifecycleStatus(LifecycleStatus.valueOf(event.getLifecycleStatus().name()));
                    return albumDocumentRepository.save(doc);
                })
                .doOnSuccess(d -> log.info("Updated album lifecycle status: id={}, status={}", event.getId(), event.getLifecycleStatus()))
                .doOnError(e -> log.error("Failed to update album lifecycle status: id={}, error={}", event.getId(), e.getMessage()))
                .then();
    }

    public Mono<Void> handleArtistAddedToAlbum(ArtistAddedToAlbumEventOuterClass.ArtistAddedToAlbumEvent event) {
        return Mono.zip(
                        albumDocumentRepository.findById(event.getAlbumId()),
                        artistDocumentRepository.findById(event.getArtistId())
                )
                .flatMap(tuple -> {
                    AlbumPublicDocument albumDoc = tuple.getT1();
                    ArtistPublicDocument artistDoc = tuple.getT2();
                    if (albumDoc.getArtists() == null) {
                        albumDoc.setArtists(new ArrayList<>());
                    }
                    albumDoc.getArtists().add(AlbumPublicDocument.ArtistEmbed.builder()
                            .id(artistDoc.getId())
                            .name(artistDoc.getName())
                            .avatarS3Key(artistDoc.getAvatarS3Key())
                            .role(ArtistRole.valueOf(event.getRole().name()))
                            .build());
                    return albumDocumentRepository.save(albumDoc);
                })
                .doOnSuccess(d -> log.info("Added artist to album: artistId={}, albumId={}", event.getArtistId(), event.getAlbumId()))
                .doOnError(e -> log.error("Failed to add artist to album: artistId={}, albumId={}, error={}", event.getArtistId(), event.getAlbumId(), e.getMessage()))
                .then();
    }

    public Mono<Void> handleArtistDeletedFromAlbum(ArtistDeletedFromAlbumEventOuterClass.ArtistDeletedFromAlbumEvent event) {
        return albumDocumentRepository.findById(event.getAlbumId())
                .flatMap(doc -> {
                    if (doc.getArtists() != null) {
                        doc.getArtists().removeIf(a -> a.getId().equals(event.getArtistId()));
                    }
                    return albumDocumentRepository.save(doc);
                })
                .doOnSuccess(d -> log.info("Removed artist from album: artistId={}, albumId={}", event.getArtistId(), event.getAlbumId()))
                .doOnError(e -> log.error("Failed to remove artist from album: artistId={}, albumId={}, error={}", event.getArtistId(), event.getAlbumId(), e.getMessage()))
                .then();
    }
}
