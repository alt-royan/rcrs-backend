package org.ultra.rcrs.catalogservice.service.write;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ultra.rcrs.catalogservice.dto.OtherArtistDto;
import org.ultra.rcrs.catalogservice.dto.request.ArtistIdDto;
import org.ultra.rcrs.catalogservice.dto.request.TrackUploadRequest;
import org.ultra.rcrs.catalogservice.model.write.ArtistToTrack;
import org.ultra.rcrs.catalogservice.model.write.OtherArtist;
import org.ultra.rcrs.catalogservice.model.write.Track;
import org.ultra.rcrs.catalogservice.repository.write.ArtistRepository;
import org.ultra.rcrs.catalogservice.repository.write.ArtistToTrackRepository;
import org.ultra.rcrs.catalogservice.repository.write.OtherArtistRepository;
import org.ultra.rcrs.catalogservice.repository.write.TrackRepository;
import org.ultra.rcrs.catalogservice.service.CdcService;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.exceptions.NotFoundException;
import org.ultra.rcrs.utils.Url62;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrackWriteService {

    private final TrackRepository trackRepository;
    private final OtherArtistRepository otherArtistRepository;
    private final ArtistToTrackRepository artistToTrackRepository;
    private final ArtistRepository artistRepository;
    private final CdcService cdcService;

    @Transactional
    public void createTracks(List<TrackUploadRequest> requests, UUID albumId) {
        requests.forEach(r -> this.createTrack(r, albumId));
    }

    @Transactional
    public void createTrack(TrackUploadRequest uploadRequest, UUID albumId) {
        UUID trackId = UUID.randomUUID();
        checkArtists(uploadRequest.getArtists());
        trackRepository.save(Track.builder()
                .id(trackId)
                .status(EntityStatus.CREATED)
                .title(uploadRequest.getTitle())
                .releaseDate(uploadRequest.getReleaseDate())
                .durationMs(0)
                .trackNumber(uploadRequest.getTrackNumber())
                .explicit(uploadRequest.getExplicit())
                .available(true)
                .albumId(albumId)
                .build());
        log.info("Track {} saved with id {}", uploadRequest.getTitle(), trackId);
        saveArtistsToTrack(uploadRequest.getArtists(), trackId);
        saveOthersToTrack(uploadRequest.getOthers(), trackId);
        cdcService.trackCreated(trackId, uploadRequest.getUid());
    }

    @Transactional
    public void deleteTrack(UUID trackId) {
        otherArtistRepository.deleteByTrackId(trackId);
        artistToTrackRepository.deleteByTrackId(trackId);
        trackRepository.deleteById(trackId);
        log.info("Track {} deleted", trackId);
        cdcService.trackDeleted(trackId);
    }

    private void checkArtists(List<ArtistIdDto> artists) {
        artists.forEach(a -> {
            var id = Url62.decode(a.getId());
            if (!artistRepository.existsById(id)) {
                throw new NotFoundException("Artist", id);
            }
        });
    }

    private void saveArtistsToTrack(List<ArtistIdDto> artists, UUID trackId) {
        artists.forEach(a -> {
            artistToTrackRepository.save(ArtistToTrack.builder()
                    .artistId(Url62.decode(a.getId()))
                    .trackId(trackId)
                    .artistRole(a.getRole())
                    .build());
            log.info("Artist {} with role {} attached to track {}", a.getId(), a.getRole(), trackId);
        });
    }

    private void saveOthersToTrack(List<OtherArtistDto> others, UUID trackId) {
        others.forEach(o -> {
            otherArtistRepository.save(new OtherArtist(o, trackId));
            log.info("OtherArtist {} saved for track {}", o.getName(), trackId);
        });
    }

    @Transactional
    public void updateStatus(UUID trackId, EntityStatus status) {
        int count = trackRepository.updateStatusByIds(List.of(trackId), status);
        log.info("Update track {} status to {}: {} rows updated", trackId, status, count);
    }

    @Transactional
    public void updateStatusForAllInAlbum(UUID albumId, EntityStatus status) {
        int count = trackRepository.updateStatusForAllInAlbum(albumId, status);
        log.info("Update status to {} for all tracks in album {}: {} rows updated", status, albumId, count);
    }

    @Transactional
    public void publishTrack(UUID id) {
        int c = trackRepository.updateStatusAndReleaseDate(id, EntityStatus.PUBLISHED, Instant.now());
        log.info("Track {} published", id);
        cdcService.trackUpserted(id);
    }

    @Transactional
    public void publishTracks(List<UUID> ids) {
        ids.forEach(this::publishTrack);
    }
}
