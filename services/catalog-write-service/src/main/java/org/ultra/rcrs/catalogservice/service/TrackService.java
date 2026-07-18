package org.ultra.rcrs.catalogservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ultra.rcrs.catalogservice.dto.request.TrackUploadRequest;
import org.ultra.rcrs.catalogservice.model.Track;
import org.ultra.rcrs.catalogservice.repository.TrackRepository;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.enums.LifecycleStatus;
import org.ultra.rcrs.utils.Url62;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrackService {

    private final TrackRepository trackRepository;
    private final ArtistService artistService;
    private final CdcService cdcService;

    @Transactional
    public void createTrack(TrackUploadRequest uploadRequest) {
        Track track = trackRepository.save(Track.builder()
                .lifecycleStatus(LifecycleStatus.CREATED)
                .title(uploadRequest.getTitle())
                .releaseDate(uploadRequest.getReleaseDate())
                .publishTimestamp(uploadRequest.getPublishTimestamp())
                .durationMs(null)
                .trackNumber(uploadRequest.getTrackNumber())
                .explicit(uploadRequest.getExplicit())
                .availabilityStatus(EntityStatus.ACTIVE)
                .albumId(Url62.decode(uploadRequest.getAlbumId()))
                .build());
        log.info("Track {} saved with UUID: {}, public Id {}", track.getTitle(), track.getId(), Url62.encode(track.getId()));
        artistService.saveArtistsToTrack(uploadRequest.getArtists(), track.getId());
        artistService.saveOthersToTrack(uploadRequest.getOthers(), track.getId());
        cdcService.trackCreated(track.getId(), uploadRequest.getUid());
    }

    @Transactional
    public void deleteTrack(UUID trackId) {
        updateAvailability(EntityStatus.DELETED, trackId);
        cdcService.trackDeleted(trackId);
    }

    @Transactional
    public void hideTrack(UUID trackId) {
        updateAvailability(EntityStatus.HIDDEN, trackId);
        cdcService.trackHidden(trackId);
    }

    @Transactional
    public void updateLifecycleStatus(LifecycleStatus status, UUID trackId) {
        trackRepository.updateLifecycleStatusById(status, trackId);
        log.info("Track {} lifecycle_status updated to {}", trackId, status);
    }

    private void updateAvailability(EntityStatus status, UUID trackId) {
        trackRepository.updateAvailabilityStatusById(status, trackId);
        log.info("Track {} availability_status updated to {}", trackId, status);
    }
}
