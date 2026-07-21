package org.ultra.rcrs.metadata.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ultra.rcrs.enums.AlbumType;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.enums.LifecycleStatus;
import org.ultra.rcrs.metadata.dto.request.AlbumUploadRequest;
import org.ultra.rcrs.metadata.kafka.CatalogEventProducer;
import org.ultra.rcrs.metadata.model.Album;
import org.ultra.rcrs.metadata.repository.AlbumRepository;
import org.ultra.rcrs.metadata.repository.ArtistToAlbumRepository;
import org.ultra.rcrs.metadata.repository.TrackRepository;
import org.ultra.rcrs.utils.S3Utils;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlbumServiceTest {

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
    void createAlbum() {
        var request = new AlbumUploadRequest();
        request.setTitle("Test Album");
        request.setType(AlbumType.FULL);
        request.setReleaseDate(LocalDateTime.of(2026, 1, 15, 0, 0));
        request.setPublishTimestamp(null);
        request.setCoverUri("s3://bucket/cover.jpg");

        UUID generatedId = UUID.randomUUID();
        var savedAlbum = Album.builder()
                .id(generatedId)
                .title("Test Album")
                .type(AlbumType.FULL)
                .lifecycleStatus(LifecycleStatus.CREATED)
                .availabilityStatus(EntityStatus.ACTIVE)
                .coverS3Key("cover.jpg")
                .releaseDate(LocalDateTime.of(2026, 1, 15, 0, 0))
                .build();

        when(s3Utils.parseKey("s3://bucket/cover.jpg")).thenReturn("cover.jpg");
        when(albumRepository.save(any(Album.class))).thenReturn(savedAlbum);

        UUID result = albumService.createAlbum(request);

        assertEquals(generatedId, result);

        ArgumentCaptor<Album> albumCaptor = ArgumentCaptor.forClass(Album.class);
        verify(albumRepository).save(albumCaptor.capture());
        Album captured = albumCaptor.getValue();
        assertEquals("Test Album", captured.getTitle());
        assertEquals(AlbumType.FULL, captured.getType());
        assertEquals(LifecycleStatus.CREATED, captured.getLifecycleStatus());
        assertEquals(EntityStatus.ACTIVE, captured.getAvailabilityStatus());
        assertEquals("cover.jpg", captured.getCoverS3Key());

        verify(catalogEventProducer).albumCreated(savedAlbum);
        verifyNoMoreInteractions(catalogEventProducer);
    }

    @Test
    void createAlbumWithoutCover() {
        var request = new AlbumUploadRequest();
        request.setTitle("No Cover");
        request.setType(AlbumType.SINGLE);
        request.setReleaseDate(LocalDateTime.now());

        UUID generatedId = UUID.randomUUID();
        var savedAlbum = Album.builder()
                .id(generatedId)
                .title("No Cover")
                .type(AlbumType.SINGLE)
                .lifecycleStatus(LifecycleStatus.CREATED)
                .availabilityStatus(EntityStatus.ACTIVE)
                .build();

        when(s3Utils.parseKey(null)).thenReturn(null);
        when(albumRepository.save(any(Album.class))).thenReturn(savedAlbum);

        UUID result = albumService.createAlbum(request);

        assertEquals(generatedId, result);
        verify(catalogEventProducer).albumCreated(savedAlbum);
    }

    @Test
    void albumExistsReturnsTrue() {
        UUID id = UUID.randomUUID();
        when(albumRepository.existsById(id)).thenReturn(true);
        assertTrue(albumService.albumExists(id));
    }

    @Test
    void albumExistsReturnsFalse() {
        UUID id = UUID.randomUUID();
        when(albumRepository.existsById(id)).thenReturn(false);
        assertFalse(albumService.albumExists(id));
    }

    @Test
    void markAlbumDelete() {
        UUID albumId = UUID.randomUUID();
        albumService.markAlbumDelete(albumId);
        verify(albumRepository).updateAvailabilityStatusById(EntityStatus.DELETED, albumId);
        verify(trackRepository).updateAvailabilityStatusByAlbumId(EntityStatus.DELETED, albumId);
        verify(catalogEventProducer).albumDeleted(albumId);
    }
}
