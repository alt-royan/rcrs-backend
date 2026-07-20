package org.ultra.rcrs.metadata.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.enums.LifecycleStatus;
import org.ultra.rcrs.exceptions.NotFoundException;
import org.ultra.rcrs.metadata.dto.ArtistDto;
import org.ultra.rcrs.metadata.dto.OtherArtistDto;
import org.ultra.rcrs.metadata.dto.request.TrackUploadRequest;
import org.ultra.rcrs.metadata.kafka.CatalogEventProducer;
import org.ultra.rcrs.metadata.model.ArtistToTrack;
import org.ultra.rcrs.metadata.model.ArtistToTrackPK;
import org.ultra.rcrs.metadata.model.OtherArtist;
import org.ultra.rcrs.metadata.model.Track;
import org.ultra.rcrs.metadata.repository.ArtistToTrackRepository;
import org.ultra.rcrs.metadata.repository.OtherArtistRepository;
import org.ultra.rcrs.metadata.repository.TrackRepository;
import org.ultra.rcrs.utils.Url62;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrackService {

    private final TrackRepository trackRepository;
    private final ArtistToTrackRepository artistToTrackRepository;
    private final OtherArtistRepository otherArtistRepository;
    private final ArtistService artistService;
    private final AlbumService albumService;
    private final CatalogEventProducer catalogEventProducer;

    @Transactional
    public UUID createTrack(TrackUploadRequest uploadRequest) {
        UUID albumId = Url62.decode(uploadRequest.getAlbumId());
        if (!albumService.albumExists(albumId)) {
            throw new NotFoundException("Album", albumId);
        }

        Track track = trackRepository.save(Track.builder()
                .lifecycleStatus(LifecycleStatus.CREATED)
                .title(uploadRequest.getTitle())
                .durationMs(null)
                .trackNumber(uploadRequest.getTrackNumber())
                .explicit(uploadRequest.getExplicit())
                .availabilityStatus(EntityStatus.ACTIVE)
                .albumId(albumId)
                .build());
        log.info("Track {} saved with UUID: {}, public Id {}", track.getTitle(), track.getId(), Url62.encode(track.getId()));
        catalogEventProducer.trackCreated(track);
        catalogEventProducer.trackAddedToAlbum(track.getId(), albumId);

        return track.getId();
    }

    @Transactional
    public void markTrackDelete(UUID trackId) {
        updateAvailability(EntityStatus.DELETED, trackId);
        catalogEventProducer.trackDeleted(trackId);
    }

    @Transactional
    public void hideTrack(UUID trackId) {
        updateAvailability(EntityStatus.HIDDEN, trackId);
        catalogEventProducer.trackHidden(trackId);
    }

    @Transactional
    public void activeTrack(UUID trackId) {
        updateAvailability(EntityStatus.ACTIVE, trackId);
        catalogEventProducer.trackActivated(trackId);
    }

    @Transactional
    public void updateLifecycleStatus(LifecycleStatus status, UUID trackId) {
        trackRepository.updateLifecycleStatusById(status, trackId);
        log.info("Track {} lifecycle_status updated to {}", trackId, status);
        catalogEventProducer.updateTrackLifecycleStatus(status, trackId);
    }

    @Transactional
    public void addAllArtistToTrack(List<ArtistDto> artists, UUID trackId) {
        artists.forEach(a -> addArtistToTrack(a, trackId));
    }

    private void addArtistToTrack(ArtistDto artistDto, UUID trackUuid) {
        if (!trackExists(trackUuid)) {
            throw new NotFoundException("Track", trackUuid);
        }

        UUID artistUuid = Url62.decode(artistDto.getId());
        if (!artistService.artistExists(artistUuid)) {
            throw new NotFoundException("Artist", artistUuid);
        }
        var artist = artistToTrackRepository.save(ArtistToTrack.builder()
                .artistId(artistUuid)
                .trackId(trackUuid)
                .artistRole(artistDto.getRole())
                .build());

        log.info("Artist {} with role {} attached to track {}", artist, artistDto.getRole(), trackUuid);
        catalogEventProducer.artistAddedToTrack(artistUuid, trackUuid, artistDto.getRole());
    }

    @Transactional
    public void addAllOthersToTrack(List<OtherArtistDto> others, UUID trackId) {
        others.forEach(o -> addOtherToTrack(o, trackId));
    }

    private void addOtherToTrack(OtherArtistDto otherDto, UUID trackUuid) {
        if (!trackExists(trackUuid)) {
            throw new NotFoundException("Track", trackUuid);
        }

        var other = otherArtistRepository.save(new OtherArtist(otherDto, trackUuid));
        log.info("OtherArtist {} saved for track {}", other, trackUuid);
        catalogEventProducer.otherAddedToTrack(other, trackUuid);
    }

    @Transactional
    public void deleteAllArtistFromTrack(List<ArtistDto> artists, UUID trackId) {
        artists.forEach(a -> deleteArtistFromTrack(a, trackId));
    }

    private void deleteArtistFromTrack(ArtistDto artistDto, UUID trackId) {
        UUID artistUuid = Url62.decode(artistDto.getId());

        artistToTrackRepository.deleteById(new ArtistToTrackPK(artistUuid, trackId));

        log.info("Artist {} deleted from album {}", artistUuid, trackId);
        catalogEventProducer.artistDeletedFromTrack(artistUuid, trackId);
    }

    @Transactional
    public void deleteAllOthersFromTrack(List<OtherArtistDto> others, UUID trackId) {
        others.forEach(o -> deleteOtherFromTrack(Url62.decode(o.getId()), trackId));
    }

    private void deleteOtherFromTrack(UUID otherId, UUID trackId) {
        otherArtistRepository.deleteById(otherId);
        log.info("OtherArtist {} deleted from track {}", otherId, trackId);
        catalogEventProducer.otherDeletedFromTrack(otherId, trackId);
    }

    private boolean trackExists(UUID trackId) {
        return trackRepository.existsById(trackId);
    }

    private void updateAvailability(EntityStatus status, UUID trackId) {
        trackRepository.updateAvailabilityStatusById(status, trackId);
        log.info("Track {} availability_status updated to {}", trackId, status);
    }
}
