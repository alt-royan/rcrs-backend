package org.ultra.rcrs.metadata.service.write;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.enums.ArtistRole;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.enums.LifecycleStatus;
import org.ultra.rcrs.events.album.AlbumCreatedEventOuterClass;
import org.ultra.rcrs.events.album.AlbumUpdateLifecycleStatusEventOuterClass;
import org.ultra.rcrs.events.album.ArtistAddedToAlbumEventOuterClass;
import org.ultra.rcrs.events.album.ArtistDeletedFromAlbumEventOuterClass;
import org.ultra.rcrs.metadata.model.AlbumDocument;
import org.ultra.rcrs.metadata.model.ArtistDocument;
import org.ultra.rcrs.metadata.repository.AlbumDocumentRepository;
import org.ultra.rcrs.metadata.repository.ArtistDocumentRepository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlbumWriteService {

    private final AlbumDocumentRepository albumDocumentRepository;
    private final ArtistDocumentRepository artistDocumentRepository;

    public void handleAlbumCreated(AlbumCreatedEventOuterClass.AlbumCreatedEvent event) {
        LocalDateTime releaseDate = event.hasReleaseDate()
                ? LocalDateTime.ofEpochSecond(event.getReleaseDate().getSeconds(), event.getReleaseDate().getNanos(), ZoneOffset.UTC)
                : LocalDateTime.now();
        AlbumDocument doc = AlbumDocument.builder()
                .id(event.getId())
                .title(event.getTitle())
                .type(org.ultra.rcrs.enums.AlbumType.valueOf(event.getType().name()))
                .releaseDate(releaseDate)
                .year(releaseDate.getYear())
                .totalTracks(0)
                .totalDurationMs(0)
                .coverS3Key(event.getCoverS3Key())
                .availabilityStatus(EntityStatus.valueOf(event.getAvailabilityStatus().name()))
                .lifecycleStatus(LifecycleStatus.valueOf(event.getLifecycleStatus().name()))
                .build();

        albumDocumentRepository.save(doc)
                .doOnSuccess(d -> log.info("Saved album document: id={}", d.getId()))
                .doOnError(e -> log.error("Failed to save album document: id={}, error={}", event.getId(), e.getMessage()))
                .block();
    }

    public void handleAlbumDeleted(String id) {
        albumDocumentRepository.findById(id)
                .flatMap(doc -> {
                    doc.setAvailabilityStatus(EntityStatus.DELETED);
                    return albumDocumentRepository.save(doc);
                })
                .doOnSuccess(d -> log.info("Marked album as deleted: id={}", id))
                .doOnError(e -> log.error("Failed to delete album: id={}, error={}", id, e.getMessage()))
                .block();
    }

    public void handleAlbumHidden(String id) {
        albumDocumentRepository.findById(id)
                .flatMap(doc -> {
                    doc.setAvailabilityStatus(EntityStatus.HIDDEN);
                    return albumDocumentRepository.save(doc);
                })
                .doOnSuccess(d -> log.info("Marked album as hidden: id={}", id))
                .doOnError(e -> log.error("Failed to hide album: id={}, error={}", id, e.getMessage()))
                .block();
    }

    public void handleAlbumActivated(String id) {
        albumDocumentRepository.findById(id)
                .flatMap(doc -> {
                    doc.setAvailabilityStatus(EntityStatus.ACTIVE);
                    return albumDocumentRepository.save(doc);
                })
                .doOnSuccess(d -> log.info("Marked album as active: id={}", id))
                .doOnError(e -> log.error("Failed to activate album: id={}, error={}", id, e.getMessage()))
                .block();
    }

    public void handleAlbumLifecycleStatusUpdated(AlbumUpdateLifecycleStatusEventOuterClass.AlbumUpdateLifecycleStatusEvent event) {
        albumDocumentRepository.findById(event.getId())
                .flatMap(doc -> {
                    doc.setLifecycleStatus(LifecycleStatus.valueOf(event.getLifecycleStatus().name()));
                    return albumDocumentRepository.save(doc);
                })
                .doOnSuccess(d -> log.info("Updated album lifecycle status: id={}, status={}", event.getId(), event.getLifecycleStatus()))
                .doOnError(e -> log.error("Failed to update album lifecycle status: id={}, error={}", event.getId(), e.getMessage()))
                .block();
    }

    public void handleArtistAddedToAlbum(ArtistAddedToAlbumEventOuterClass.ArtistAddedToAlbumEvent event) {
        albumDocumentRepository.findById(event.getAlbumId())
                .zipWith(artistDocumentRepository.findById(event.getArtistId()))
                .flatMap(tuple -> {
                    AlbumDocument albumDoc = tuple.getT1();
                    ArtistDocument artistDoc = tuple.getT2();
                    if (albumDoc.getArtists() == null) {
                        albumDoc.setArtists(new ArrayList<>());
                    }
                    albumDoc.getArtists().add(AlbumDocument.ArtistEmbed.builder()
                            .id(artistDoc.getId())
                            .name(artistDoc.getName())
                            .avatarS3Key(artistDoc.getAvatarS3Key())
                            .role(ArtistRole.valueOf(event.getRole().name()))
                            .build());
                    return albumDocumentRepository.save(albumDoc);
                })
                .doOnSuccess(d -> log.info("Added artist to album: artistId={}, albumId={}", event.getArtistId(), event.getAlbumId()))
                .doOnError(e -> log.error("Failed to add artist to album: artistId={}, albumId={}, error={}", event.getArtistId(), event.getAlbumId(), e.getMessage()))
                .block();
    }

    public void handleArtistDeletedFromAlbum(ArtistDeletedFromAlbumEventOuterClass.ArtistDeletedFromAlbumEvent event) {
        albumDocumentRepository.findById(event.getAlbumId())
                .flatMap(doc -> {
                    if (doc.getArtists() != null) {
                        doc.getArtists().removeIf(a -> a.getId().equals(event.getArtistId()));
                    }
                    return albumDocumentRepository.save(doc);
                })
                .doOnSuccess(d -> log.info("Removed artist from album: artistId={}, albumId={}", event.getArtistId(), event.getAlbumId()))
                .doOnError(e -> log.error("Failed to remove artist from album: artistId={}, albumId={}, error={}", event.getArtistId(), event.getAlbumId(), e.getMessage()))
                .block();
    }

    public void handleAlbumTrueDeleted(String id) {
        albumDocumentRepository.deleteById(id)
                .doOnSuccess(v -> log.info("Permanently deleted album document: id={}", id))
                .doOnError(e -> log.error("Failed to permanently delete album: id={}, error={}", id, e.getMessage()))
                .block();
    }
}
