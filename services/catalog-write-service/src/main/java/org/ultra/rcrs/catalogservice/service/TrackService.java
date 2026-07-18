package org.ultra.rcrs.catalogservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ultra.rcrs.catalogservice.dto.OtherArtistDto;
import org.ultra.rcrs.catalogservice.dto.request.ArtistDto;
import org.ultra.rcrs.catalogservice.dto.request.TrackUploadRequest;
import org.ultra.rcrs.catalogservice.kafka.CatalogEventProducer;
import org.ultra.rcrs.catalogservice.model.ArtistToTrack;
import org.ultra.rcrs.catalogservice.model.ArtistToTrackPK;
import org.ultra.rcrs.catalogservice.model.OtherArtist;
import org.ultra.rcrs.catalogservice.model.Track;
import org.ultra.rcrs.catalogservice.repository.ArtistToTrackRepository;
import org.ultra.rcrs.catalogservice.repository.OtherArtistRepository;
import org.ultra.rcrs.catalogservice.repository.TrackRepository;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.enums.LifecycleStatus;
import org.ultra.rcrs.exceptions.NotFoundException;
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
    private final CatalogEventProducer catalogEventProducer;

    @Transactional
    public Track createTrack(TrackUploadRequest uploadRequest) {
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
        catalogEventProducer.trackCreated(track);

        addAllArtistToTrack(uploadRequest.getArtists(), track.getId());
        addAllOthersToTrack(uploadRequest.getOthers(), track.getId());

        return track;
    }

    @Transactional
    public void deleteTrack(UUID trackId) {
        updateAvailability(EntityStatus.DELETED, trackId);
        catalogEventProducer.trackDeleted(trackId);
    }

    @Transactional
    public void hideTrack(UUID trackId) {
        updateAvailability(EntityStatus.HIDDEN, trackId);
        catalogEventProducer.trackHidden(trackId);
    }

    @Transactional
    public void updateLifecycleStatus(LifecycleStatus status, UUID trackId) {
        trackRepository.updateLifecycleStatusById(status, trackId);
        log.info("Track {} lifecycle_status updated to {}", trackId, status);
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
