package org.ultra.rcrs.metadata.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.enums.LifecycleStatus;
import org.ultra.rcrs.exceptions.NotFoundException;
import org.ultra.rcrs.metadata.dto.ArtistDto;
import org.ultra.rcrs.metadata.dto.request.AlbumUploadRequest;
import org.ultra.rcrs.metadata.kafka.CatalogEventProducer;
import org.ultra.rcrs.metadata.model.Album;
import org.ultra.rcrs.metadata.model.ArtistToAlbum;
import org.ultra.rcrs.metadata.model.ArtistToAlbumPK;
import org.ultra.rcrs.metadata.repository.AlbumRepository;
import org.ultra.rcrs.metadata.repository.ArtistRepository;
import org.ultra.rcrs.metadata.repository.ArtistToAlbumRepository;
import org.ultra.rcrs.metadata.repository.TrackRepository;
import org.ultra.rcrs.utils.S3Utils;
import org.ultra.rcrs.utils.Url62;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final ArtistRepository artistRepository;
    private final TrackRepository trackRepository;
    private final TrackService trackService;
    private final ArtistToAlbumRepository artistToAlbumRepository;
    private final S3Utils s3Utils;
    private final CatalogEventProducer catalogEventProducer;

    @Transactional
    public UUID createAlbum(AlbumUploadRequest request) {
        Album album = albumRepository.save(Album.builder()
                .lifecycleStatus(LifecycleStatus.CREATED)
                .title(request.getTitle())
                .type(request.getType())
                .releaseDate(request.getReleaseDate())
                .publishTimestamp(request.getPublishTimestamp())
                .coverS3Key(s3Utils.parseKey(request.getCoverUri()))
                .availabilityStatus(EntityStatus.ACTIVE)
                .build());

        log.info("Album {} saved with UUID: {}, public Id {}", request.getTitle(), album.getId(), Url62.encode(album.getId()));
        catalogEventProducer.albumCreated(album);

        return album.getId();
    }

    @Transactional
    public List<UUID> findAllIdsByArtist(UUID artistId) {
        return albumRepository.findAllIdsByArtist(artistId);
    }

    @Transactional
    public void markAlbumDelete(UUID albumId) {
        var tracks = trackService.findAllIdsByAlbum(albumId);
        tracks.forEach(trackService::markTrackDelete);

        updateAvailability(EntityStatus.DELETED, albumId);
        log.info("Album {} and all related tracks marked as DELETED", albumId);
    }

    @Transactional
    public void hideAlbum(UUID albumId) {
        var tracks = trackService.findAllIdsByAlbum(albumId);
        tracks.forEach(trackService::hideTrack);

        updateAvailability(EntityStatus.HIDDEN, albumId);
        log.info("Album {} and all related tracks marked as HIDDEN", albumId);
    }

    @Transactional
    public void activeAlbum(UUID albumId) {
        var tracks = trackService.findAllIdsByAlbum(albumId);
        tracks.forEach(trackService::activeTrack);

        updateAvailability(EntityStatus.ACTIVE, albumId);
        log.info("Album {} and all related tracks marked as ACTIVE", albumId);
    }

    @Transactional
    public void checkAlbumReady(UUID albumId) {
        long tracksInAlbum = trackRepository.countByAlbumId(albumId);
        long readyTracksInAlbum = trackRepository.countByAlbumIdAndAvailabilityStatus(albumId, LifecycleStatus.READY);
        if (tracksInAlbum == readyTracksInAlbum) {
            updateLifecycleStatus(LifecycleStatus.READY, albumId);
        }
    }

    @Transactional
    public void updateLifecycleStatus(LifecycleStatus status, UUID albumId) {
        albumRepository.updateLifecycleStatusById(status, albumId);
        log.info("Album {} lifecycle_status updated to {}", albumId, status);
        catalogEventProducer.updateAlbumLifecycleStatus(status, albumId);
    }

    @Transactional
    public void addAllArtistToAlbum(List<ArtistDto> artists, UUID albumId) {
        artists.forEach(a -> addArtistToAlbum(a, albumId));
    }

    private void addArtistToAlbum(ArtistDto artistDto, UUID albumUuid) {
        if (!albumExists(albumUuid)) {
            throw new NotFoundException("Album", albumUuid);
        }

        UUID artistUuid = Url62.decode(artistDto.getId());
        if (!artistRepository.existsById(artistUuid)) {
            throw new NotFoundException("Artist", artistUuid);
        }

        var artist = artistToAlbumRepository.save(ArtistToAlbum.builder()
                .artistId(artistUuid)
                .albumId(albumUuid)
                .artistRole(artistDto.getRole())
                .build());

        log.info("Artist {} with role {} attached to album {}", artist, artistDto.getRole(), albumUuid);
        catalogEventProducer.artistAddedToAlbum(artistUuid, albumUuid, artistDto.getRole());
    }

    @Transactional
    public void deleteAllArtistFromAlbum(List<ArtistDto> artists, UUID albumId) {
        artists.forEach(a -> deleteArtistFromAlbum(a, albumId));
    }

    private void deleteArtistFromAlbum(ArtistDto artistDto, UUID albumUuid) {
        UUID artistUuid = Url62.decode(artistDto.getId());

        artistToAlbumRepository.deleteById(new ArtistToAlbumPK(artistUuid, albumUuid));

        log.info("Artist {} deleted from album {}", artistUuid, albumUuid);
        catalogEventProducer.artistDeletedFromAlbum(artistUuid, albumUuid);
    }

    private void updateAvailability(EntityStatus status, UUID albumId) {
        albumRepository.updateAvailabilityStatusById(status, albumId);
        switch (status) {
            case ACTIVE -> catalogEventProducer.albumActivated(albumId);
            case HIDDEN -> catalogEventProducer.albumHidden(albumId);
            case DELETED -> catalogEventProducer.albumDeleted(albumId);
        }
        log.info("Album {} availability_status updated to {}", albumId, status);
    }

    public boolean albumExists(UUID albumId) {
        return albumRepository.existsById(albumId);
    }
}
