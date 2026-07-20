package org.ultra.rcrs.metadata.service.write;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.enums.LifecycleStatus;
import org.ultra.rcrs.events.track.*;
import org.ultra.rcrs.metadata.model.AlbumPublicDocument;
import org.ultra.rcrs.metadata.model.ArtistPublicDocument;
import org.ultra.rcrs.metadata.model.TrackPublicDocument;
import org.ultra.rcrs.metadata.repository.AlbumDocumentRepository;
import org.ultra.rcrs.metadata.repository.ArtistDocumentRepository;
import org.ultra.rcrs.metadata.repository.TrackDocumentRepository;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrackWriteService {

    private final TrackDocumentRepository trackDocumentRepository;
    private final AlbumDocumentRepository albumDocumentRepository;
    private final ArtistDocumentRepository artistDocumentRepository;

    public void handleTrackCreated(TrackCreatedEventOuterClass.TrackCreatedEvent event) {
        TrackPublicDocument doc = TrackPublicDocument.builder()
                .id(event.getId())
                .title(event.getTitle())
                .trackNumber(event.getTrackNumber())
                .durationMs(0)
                .releaseDate(null)
                .explicit(event.getExplicit())
                .availabilityStatus(EntityStatus.valueOf(event.getAvailabilityStatus().name()))
                .lifecycleStatus(LifecycleStatus.valueOf(event.getLifecycleStatus().name()))
                .build();

        trackDocumentRepository.save(doc)
                .doOnSuccess(d -> log.info("Saved track document: id={}", d.getId()))
                .doOnError(e -> log.error("Failed to save track document: id={}, error={}", event.getId(), e.getMessage()))
                .block();
    }

    public void handleTrackAddedToAlbum(TrackAddedToAlbumEventOuterClass.TrackAddedToAlbumEvent event) {
        trackDocumentRepository.findById(event.getTrackId())
                .zipWith(albumDocumentRepository.findById(event.getAlbumId()))
                .flatMap(tuple -> {
                    TrackPublicDocument trackDoc = tuple.getT1();
                    AlbumPublicDocument albumDoc = tuple.getT2();
                    trackDoc.setAlbum(TrackPublicDocument.AlbumEmbed.builder()
                            .id(albumDoc.getId())
                            .title(albumDoc.getTitle())
                            .coverS3Key(albumDoc.getCoverS3Key())
                            .build());
                    if (trackDoc.getReleaseDate() == null) {
                        trackDoc.setReleaseDate(albumDoc.getReleaseDate());
                    }
                    var trackMono = trackDocumentRepository.save(trackDoc);

                    var totalTracks = albumDoc.getTotalTracks() == null ? 0 : albumDoc.getTotalTracks();
                    albumDoc.setTotalTracks(totalTracks + 1);
                    var albumMono = albumDocumentRepository.save(albumDoc);

                    return trackMono.then(albumMono);
                })
                .doOnSuccess(d -> log.info("Added track to album: trackId={}, albumId={}", event.getTrackId(), event.getAlbumId()))
                .doOnError(e -> log.error("Failed to add track to album: trackId={}, albumId={}, error={}", event.getTrackId(), event.getAlbumId(), e.getMessage()))
                .block();
    }

    public void handleTrackDeleted(String id) {
        trackDocumentRepository.findById(id)
                .flatMap(doc -> {
                    doc.setAvailabilityStatus(EntityStatus.DELETED);
                    return trackDocumentRepository.save(doc);
                })
                .doOnSuccess(d -> log.info("Marked track as deleted: id={}", id))
                .doOnError(e -> log.error("Failed to delete track: id={}, error={}", id, e.getMessage()))
                .block();
    }

    public void handleTrackHidden(String id) {
        trackDocumentRepository.findById(id)
                .flatMap(doc -> {
                    doc.setAvailabilityStatus(EntityStatus.HIDDEN);
                    return trackDocumentRepository.save(doc);
                })
                .doOnSuccess(d -> log.info("Marked track as hidden: id={}", id))
                .doOnError(e -> log.error("Failed to hide track: id={}, error={}", id, e.getMessage()))
                .block();
    }

    public void handleTrackActivated(String id) {
        trackDocumentRepository.findById(id)
                .flatMap(doc -> {
                    doc.setAvailabilityStatus(EntityStatus.ACTIVE);
                    return trackDocumentRepository.save(doc);
                })
                .doOnSuccess(d -> log.info("Marked track as active: id={}", id))
                .doOnError(e -> log.error("Failed to activate track: id={}, error={}", id, e.getMessage()))
                .block();
    }

    public void handleTrackLifecycleStatusUpdated(TrackUpdateLifecycleStatusEventOuterClass.TrackUpdateLifecycleStatusEvent event) {
        trackDocumentRepository.findById(event.getId())
                .flatMap(doc -> {
                    doc.setLifecycleStatus(LifecycleStatus.valueOf(event.getLifecycleStatus().name()));
                    return trackDocumentRepository.save(doc);
                })
                .doOnSuccess(d -> log.info("Updated track lifecycle status: id={}, status={}", event.getId(), event.getLifecycleStatus()))
                .doOnError(e -> log.error("Failed to update track lifecycle status: id={}, error={}", event.getId(), e.getMessage()))
                .block();
    }

    public void handleArtistAddedToTrack(ArtistAddedToTrackEventOuterClass.ArtistAddedToTrackEvent event) {
        trackDocumentRepository.findById(event.getTrackId())
                .zipWith(artistDocumentRepository.findById(event.getArtistId()))
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
                .block();
    }

    public void handleArtistDeletedFromTrack(ArtistDeletedFromTrackEventOuterClass.ArtistDeletedFromTrackEvent event) {
        trackDocumentRepository.findById(event.getTrackId())
                .flatMap(doc -> {
                    if (doc.getArtists() != null) {
                        doc.getArtists().removeIf(a -> a.getId().equals(event.getArtistId()));
                    }
                    return trackDocumentRepository.save(doc);
                })
                .doOnSuccess(d -> log.info("Removed artist from track: artistId={}, trackId={}", event.getArtistId(), event.getTrackId()))
                .doOnError(e -> log.error("Failed to remove artist from track: artistId={}, trackId={}, error={}", event.getArtistId(), event.getTrackId(), e.getMessage()))
                .block();
    }

    public void handleOtherAddedToTrack(OtherAddedToTrackEventOuterClass.OtherAddedToTrackEvent event) {
        trackDocumentRepository.findById(event.getTrackId())
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
                .block();
    }

    public void handleOtherDeletedFromTrack(OtherDeletedFromTrackEventOuterClass.OtherDeletedFromTrackEvent event) {
        trackDocumentRepository.findById(event.getTrackId())
                .flatMap(doc -> {
                    if (doc.getOthers() != null) {
                        doc.getOthers().removeIf(o -> o.getId().equals(event.getOtherId()));
                    }
                    return trackDocumentRepository.save(doc);
                })
                .doOnSuccess(d -> log.info("Removed other from track: otherId={}, trackId={}", event.getOtherId(), event.getTrackId()))
                .doOnError(e -> log.error("Failed to remove other from track: otherId={}, trackId={}, error={}", event.getOtherId(), event.getTrackId(), e.getMessage()))
                .block();
    }
}
