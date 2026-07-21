package org.ultra.rcrs.metadata.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.enums.LifecycleStatus;
import org.ultra.rcrs.metadata.kafka.CatalogEventProducer;
import org.ultra.rcrs.metadata.repository.AlbumRepository;
import org.ultra.rcrs.metadata.repository.ArtistToAlbumRepository;
import org.ultra.rcrs.metadata.repository.TrackRepository;
import org.ultra.rcrs.utils.S3Utils;

import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlbumServiceStatusTransitionsTest {

    @Mock
    private AlbumRepository albumRepository;
    @Mock
    private TrackRepository trackRepository;
    @Mock
    private ArtistToAlbumRepository artistToAlbumRepository;
    @Mock
    private ArtistService artistService;
    @Mock
    private S3Utils s3Utils;
    @Mock
    private CatalogEventProducer catalogEventProducer;

    @InjectMocks
    private AlbumService albumService;

    @Test
    void activeToHidden() {
        UUID albumId = UUID.randomUUID();
        albumService.hideAlbum(albumId);
        verify(albumRepository).updateAvailabilityStatusById(EntityStatus.HIDDEN, albumId);
        verify(trackRepository).updateAvailabilityStatusByAlbumId(EntityStatus.HIDDEN, albumId);
        verify(catalogEventProducer).albumHidden(albumId);
    }

    @Test
    void activeToDeleted() {
        UUID albumId = UUID.randomUUID();
        albumService.markAlbumDelete(albumId);
        verify(albumRepository).updateAvailabilityStatusById(EntityStatus.DELETED, albumId);
        verify(trackRepository).updateAvailabilityStatusByAlbumId(EntityStatus.DELETED, albumId);
        verify(catalogEventProducer).albumDeleted(albumId);
    }

    @Test
    void hiddenToActive() {
        UUID albumId = UUID.randomUUID();
        albumService.activeAlbum(albumId);
        verify(albumRepository).updateAvailabilityStatusById(EntityStatus.ACTIVE, albumId);
        verify(trackRepository).updateAvailabilityStatusByAlbumId(EntityStatus.ACTIVE, albumId);
        verify(catalogEventProducer).albumActivated(albumId);
    }

    @Test
    void deletedToActive() {
        UUID albumId = UUID.randomUUID();
        albumService.activeAlbum(albumId);
        verify(albumRepository).updateAvailabilityStatusById(EntityStatus.ACTIVE, albumId);
        verify(trackRepository).updateAvailabilityStatusByAlbumId(EntityStatus.ACTIVE, albumId);
        verify(catalogEventProducer).albumActivated(albumId);
    }

    @Test
    void updateLifecycleStatus() {
        UUID albumId = UUID.randomUUID();
        albumService.updateLifecycleStatus(LifecycleStatus.READY, albumId);
        verify(albumRepository).updateLifecycleStatusById(LifecycleStatus.READY, albumId);
        verify(catalogEventProducer).updateAlbumLifecycleStatus(LifecycleStatus.READY, albumId);
    }

    @Test
    void updateLifecycleStatusPublished() {
        UUID albumId = UUID.randomUUID();
        albumService.updateLifecycleStatus(LifecycleStatus.PUBLISHED, albumId);
        verify(albumRepository).updateLifecycleStatusById(LifecycleStatus.PUBLISHED, albumId);
        verify(catalogEventProducer).updateAlbumLifecycleStatus(LifecycleStatus.PUBLISHED, albumId);
    }
}
