package org.ultra.rcrs.catalogservice.service.write;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.catalogservice.model.AlbumPublicDocument;
import org.ultra.rcrs.catalogservice.model.ArtistPublicDocument;
import org.ultra.rcrs.catalogservice.model.TrackPublicDocument;
import org.ultra.rcrs.catalogservice.repository.AlbumDocumentRepository;
import org.ultra.rcrs.catalogservice.repository.ArtistDocumentRepository;
import org.ultra.rcrs.catalogservice.repository.TrackDocumentRepository;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.enums.LifecycleStatus;
import org.ultra.rcrs.events.track.ArtistAddedToTrackEventOuterClass;
import org.ultra.rcrs.events.track.ArtistDeletedFromTrackEventOuterClass;
import org.ultra.rcrs.events.track.OtherAddedToTrackEventOuterClass;
import org.ultra.rcrs.events.track.OtherDeletedFromTrackEventOuterClass;
import org.ultra.rcrs.events.track.TrackCreatedEventOuterClass;
import org.ultra.rcrs.events.track.TrackAddedToAlbumEventOuterClass;
import org.ultra.rcrs.events.track.TrackUpdateLifecycleStatusEventOuterClass;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrackWriteService {

    private final TrackDocumentRepository trackDocumentRepository;
    private final AlbumDocumentRepository albumDocumentRepository;
    private final ArtistDocumentRepository artistDocumentRepository;

    public Mono<TrackPublicDocument> handleTrackCreated(TrackCreatedEventOuterClass.TrackCreatedEvent event) {
        TrackPublicDocument doc = TrackPublicDocument.builder()
                .id(event.getId())
                .title(event.getTitle())
                .trackNumber(event.getTrackNumber())
                .explicit(event.getExplicit())
                .availabilityStatus(EntityStatus.valueOf(event.getAvailabilityStatus().name()))
                .lifecycleStatus(LifecycleStatus.valueOf(event.getLifecycleStatus().name()))
                .build();

        return trackDocumentRepository.save(doc)
                .doOnSuccess(d -> log.info("Saved track document: id={}", d.getId()))
                .doOnError(e -> log.error("Failed to save track document: id={}, error={}", event.getId(), e.getMessage()));
    }

    public Mono<Void> handleTrackAddedToAlbum(TrackAddedToAlbumEventOuterClass.TrackAddedToAlbumEvent event) {
        return Mono.zip(
                        trackDocumentRepository.findById(event.getTrackId()),
                        albumDocumentRepository.findById(event.getAlbumId())
                )
                .flatMap(tuple -> {
                    TrackPublicDocument trackDoc = tuple.getT1();
                    AlbumPublicDocument albumDoc = tuple.getT2();
                    trackDoc.setAlbum(TrackPublicDocument.AlbumEmbed.builder()
                            .id(albumDoc.getId())
                            .title(albumDoc.getTitle())
                            .coverS3Key(albumDoc.getCoverS3Key())
                            .build());
                    return trackDocumentRepository.save(trackDoc);
                })
                .doOnSuccess(d -> log.info("Added track to album: trackId={}, albumId={}", event.getTrackId(), event.getAlbumId()))
                .doOnError(e -> log.error("Failed to add track to album: trackId={}, albumId={}, error={}", event.getTrackId(), event.getAlbumId(), e.getMessage()))
                .then();
    }

    public Mono<Void> handleTrackDeleted(String id) {
        return trackDocumentRepository.findById(id)
                .flatMap(doc -> {
                    doc.setAvailabilityStatus(EntityStatus.DELETED);
                    return trackDocumentRepository.save(doc);
                })
                .doOnSuccess(d -> log.info("Marked track as deleted: id={}", id))
                .doOnError(e -> log.error("Failed to delete track: id={}, error={}", id, e.getMessage()))
                .then();
    }

    public Mono<Void> handleTrackHidden(String id) {
        return trackDocumentRepository.findById(id)
                .flatMap(doc -> {
                    doc.setAvailabilityStatus(EntityStatus.HIDDEN);
                    return trackDocumentRepository.save(doc);
                })
                .doOnSuccess(d -> log.info("Marked track as hidden: id={}", id))
                .doOnError(e -> log.error("Failed to hide track: id={}, error={}", id, e.getMessage()))
                .then();
    }

    public Mono<Void> handleTrackLifecycleStatusUpdated(TrackUpdateLifecycleStatusEventOuterClass.TrackUpdateLifecycleStatusEvent event) {
        return trackDocumentRepository.findById(event.getId())
                .flatMap(doc -> {
                    doc.setLifecycleStatus(LifecycleStatus.valueOf(event.getLifecycleStatus().name()));
                    return trackDocumentRepository.save(doc);
                })
                .doOnSuccess(d -> log.info("Updated track lifecycle status: id={}, status={}", event.getId(), event.getLifecycleStatus()))
                .doOnError(e -> log.error("Failed to update track lifecycle status: id={}, error={}", event.getId(), e.getMessage()))
                .then();
    }

    public Mono<Void> handleArtistAddedToTrack(ArtistAddedToTrackEventOuterClass.ArtistAddedToTrackEvent event) {
        return Mono.zip(
                        trackDocumentRepository.findById(event.getTrackId()),
                        artistDocumentRepository.findById(event.getArtistId())
                )
                .flatMap(tuple -> {
                    TrackPublicDocument trackDoc = tuple.getT1();
                    ArtistPublicDocument artistDoc = tuple.getT2();
                    if (trackDoc.getArtists() == null) {
                        trackDoc.setArtists(new ArrayList<>());
                    }
                    trackDoc.getArtists().add(TrackPublicDocument.ArtistEmbed.builder()
                            .id(artistDoc.getId())
                            .name(artistDoc.getName())
                            .avatarS3Key(artistDoc.getAvatarS3Key())
                            .role(org.ultra.rcrs.enums.ArtistRole.valueOf(event.getRole().name()))
                            .build());
                    return trackDocumentRepository.save(trackDoc);
                })
                .doOnSuccess(d -> log.info("Added artist to track: artistId={}, trackId={}", event.getArtistId(), event.getTrackId()))
                .doOnError(e -> log.error("Failed to add artist to track: artistId={}, trackId={}, error={}", event.getArtistId(), event.getTrackId(), e.getMessage()))
                .then();
    }

    public Mono<Void> handleArtistDeletedFromTrack(ArtistDeletedFromTrackEventOuterClass.ArtistDeletedFromTrackEvent event) {
        return trackDocumentRepository.findById(event.getTrackId())
                .flatMap(doc -> {
                    if (doc.getArtists() != null) {
                        doc.getArtists().removeIf(a -> a.getId().equals(event.getArtistId()));
                    }
                    return trackDocumentRepository.save(doc);
                })
                .doOnSuccess(d -> log.info("Removed artist from track: artistId={}, trackId={}", event.getArtistId(), event.getTrackId()))
                .doOnError(e -> log.error("Failed to remove artist from track: artistId={}, trackId={}, error={}", event.getArtistId(), event.getTrackId(), e.getMessage()))
                .then();
    }

    public Mono<Void> handleOtherAddedToTrack(OtherAddedToTrackEventOuterClass.OtherAddedToTrackEvent event) {
        return trackDocumentRepository.findById(event.getTrackId())
                .flatMap(doc -> {
                    if (doc.getOthers() == null) {
                        doc.setOthers(new ArrayList<>());
                    }
                    doc.getOthers().add(TrackPublicDocument.OtherArtistEmbed.builder()
                            .id(event.getOtherId())
                            .name(event.getName())
                            .roles(event.getRolesList().stream()
                                    .map(r -> org.ultra.rcrs.enums.ArtistRole.valueOf(r.name()))
                                    .collect(Collectors.toList()))
                            .socialLinks(event.getSocialLinksList().stream()
                                    .map(sl -> ArtistPublicDocument.SocialLinkEmbed.builder()
                                            .resourceName(sl.getResourceName())
                                            .url(sl.getUrl())
                                            .build())
                                    .collect(Collectors.toList()))
                            .build());
                    return trackDocumentRepository.save(doc);
                })
                .doOnSuccess(d -> log.info("Added other to track: otherId={}, trackId={}", event.getOtherId(), event.getTrackId()))
                .doOnError(e -> log.error("Failed to add other to track: otherId={}, trackId={}, error={}", event.getOtherId(), event.getTrackId(), e.getMessage()))
                .then();
    }

    public Mono<Void> handleOtherDeletedFromTrack(OtherDeletedFromTrackEventOuterClass.OtherDeletedFromTrackEvent event) {
        return trackDocumentRepository.findById(event.getTrackId())
                .flatMap(doc -> {
                    if (doc.getOthers() != null) {
                        doc.getOthers().removeIf(o -> o.getId().equals(event.getOtherId()));
                    }
                    return trackDocumentRepository.save(doc);
                })
                .doOnSuccess(d -> log.info("Removed other from track: otherId={}, trackId={}", event.getOtherId(), event.getTrackId()))
                .doOnError(e -> log.error("Failed to remove other from track: otherId={}, trackId={}, error={}", event.getOtherId(), event.getTrackId(), e.getMessage()))
                .then();
    }
}
