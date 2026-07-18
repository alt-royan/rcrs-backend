package org.ultra.rcrs.catalogservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ultra.rcrs.catalogservice.dto.request.AlbumUploadRequest;
import org.ultra.rcrs.catalogservice.dto.request.ArtistIdDto;
import org.ultra.rcrs.catalogservice.dto.response.IdResponse;
import org.ultra.rcrs.catalogservice.model.Album;
import org.ultra.rcrs.catalogservice.model.ArtistToAlbum;
import org.ultra.rcrs.catalogservice.model.Track;
import org.ultra.rcrs.catalogservice.repository.AlbumRepository;
import org.ultra.rcrs.catalogservice.repository.ArtistRepository;
import org.ultra.rcrs.catalogservice.repository.ArtistToAlbumRepository;
import org.ultra.rcrs.catalogservice.repository.TrackRepository;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.enums.LifecycleStatus;
import org.ultra.rcrs.utils.S3Utils;
import org.ultra.rcrs.utils.Url62;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final ArtistService artistService;
    private final S3Utils s3Utils;
    private final CdcService cdcService;

    @Transactional
    public void createAlbum(AlbumUploadRequest request) {
        Album album = albumRepository.save(Album.builder()
                .lifecycleStatus(LifecycleStatus.CREATED)
                .title(request.getTitle())
                .type(request.getType())
                .releaseDate(request.getReleaseDate())
                .coverS3Key(s3Utils.parseKey(request.getCoverUri()))
                .availabilityStatus(EntityStatus.ACTIVE)
                .build());

        log.info("Album {} saved with id {}", request.getTitle(), album.getId());
        artistService.saveArtistsToAlbum(request.getArtists(), album.getId());
        cdcService.albumCreated(album.getId());
    }

    @Transactional
    public void deleteAlbum(UUID albumId) {
        updateAvailability(EntityStatus.DELETED, albumId);
        cdcService.albumDeleted(albumId);
    }

    @Transactional
    public void hideAlbum(UUID albumId) {
        updateAvailability(EntityStatus.HIDDEN, albumId);
        cdcService.albumHidden(albumId);
    }

    @Transactional
    public void updateLifecycleStatus(LifecycleStatus status, UUID albumId) {
        albumRepository.updateLifecycleStatusById(status, albumId);
        log.info("Album {} lifecycle_status updated to {}", albumId, status);
    }

    private void updateAvailability(EntityStatus status, UUID albumId) {
        albumRepository.updateAvailabilityStatusById(status, albumId);
        log.info("Album {} availability_status updated to {}", albumId, status);
    }

}
