package org.ultra.rcrs.metadata.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ultra.rcrs.enums.AlbumType;
import org.ultra.rcrs.enums.ArtistRole;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.enums.LifecycleStatus;
import org.ultra.rcrs.metadata.dto.ArtistDto;
import org.ultra.rcrs.metadata.dto.request.AlbumUploadRequest;
import org.ultra.rcrs.metadata.kafka.CatalogEventProducer;
import org.ultra.rcrs.metadata.model.Album;
import org.ultra.rcrs.metadata.model.ArtistToAlbum;
import org.ultra.rcrs.metadata.model.ArtistToAlbumPK;
import org.ultra.rcrs.metadata.repository.AlbumRepository;
import org.ultra.rcrs.metadata.repository.ArtistToAlbumRepository;
import org.ultra.rcrs.utils.S3Utils;
import org.ultra.rcrs.utils.Url62;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.anyOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlbumServiceTest {

    @Mock
    private AlbumRepository albumRepository;
    @Mock
    private TrackService trackService;
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
    void createAlbum_savesAlbumAndEmitsEvent() {
        AlbumUploadRequest request = new AlbumUploadRequest();
        request.setTitle("Test Album");
        request.setType(AlbumType.FULL);
        request.setReleaseDate(LocalDateTime.of(2025, 1, 15, 0, 0));
        request.setPublishTimestamp(OffsetDateTime.now());
        request.setCoverUri("s3://bucket/cover.jpg");

        Album albumExpected = Album.builder()
                .id(UUID.randomUUID())
                .lifecycleStatus(LifecycleStatus.CREATED)
                .title(request.getTitle())
                .type(request.getType())
                .releaseDate(request.getReleaseDate())
                .publishTimestamp(request.getPublishTimestamp())
                .coverS3Key(s3Utils.parseKey(request.getCoverUri()))
                .availabilityStatus(EntityStatus.ACTIVE)
                .build();

        when(albumRepository.save(any(Album.class))).thenAnswer(i -> {
            var albumSaved = (Album) i.getArguments()[0];
            albumSaved.setId(albumExpected.getId());
            return albumSaved;
        });
        ArgumentCaptor<Album> albumCaptor = ArgumentCaptor.forClass(Album.class);

        UUID result = albumService.createAlbum(request);
        assertThat(result).isEqualTo(albumExpected.getId());

        verify(albumRepository).save(albumCaptor.capture());
        Album captured = albumCaptor.getValue();
        captured.setId(albumExpected.getId());
        assertThat(captured.getType()).isEqualTo(albumExpected.getType());
        assertThat(captured.getLifecycleStatus()).isEqualTo(albumExpected.getLifecycleStatus());
        assertThat(captured.getAvailabilityStatus()).isEqualTo(albumExpected.getAvailabilityStatus());
        assertThat(captured.getCoverS3Key()).isEqualTo(albumExpected.getCoverS3Key());
        assertThat(captured.getReleaseDate()).isEqualTo(albumExpected.getReleaseDate());
        assertThat(captured.getPublishTimestamp()).isEqualTo(albumExpected.getPublishTimestamp());
        assertThat(captured.getTitle()).isEqualTo(albumExpected.getTitle());

        verify(catalogEventProducer).albumCreated(albumCaptor.capture());
        assertThat(captured.getId()).isEqualTo(albumExpected.getId());
    }

    @Test
    void findAllIdsByArtist_delegatesToRepository() {
        UUID artistId = UUID.randomUUID();
        List<UUID> expected = List.of(UUID.randomUUID(), UUID.randomUUID());
        when(albumRepository.findAllIdsByArtist(artistId)).thenReturn(expected);

        List<UUID> result = albumService.findAllIdsByArtist(artistId);

        assertThat(result).isEqualTo(expected);
        verify(albumRepository).findAllIdsByArtist(artistId);
    }

    @Test
    void markAlbumDelete_cascadesToTracksAndUpdatesAvailability() {
        UUID albumId = UUID.randomUUID();
        UUID trackId1 = UUID.randomUUID();
        UUID trackId2 = UUID.randomUUID();
        when(trackService.findAllIdsByAlbum(albumId)).thenReturn(List.of(trackId1, trackId2));

        albumService.markAlbumDelete(albumId);

        verify(trackService).findAllIdsByAlbum(albumId);
        verify(trackService).markTrackDelete(trackId1);
        verify(trackService).markTrackDelete(trackId2);
        verify(albumRepository).updateAvailabilityStatusById(EntityStatus.DELETED, albumId);
        verify(catalogEventProducer).albumDeleted(albumId);
    }

    @Test
    void hideAlbum_cascadesToTracksAndUpdatesAvailability() {
        UUID albumId = UUID.randomUUID();
        UUID trackId = UUID.randomUUID();
        when(trackService.findAllIdsByAlbum(albumId)).thenReturn(List.of(trackId));

        albumService.hideAlbum(albumId);

        verify(trackService).findAllIdsByAlbum(albumId);
        verify(trackService).hideTrack(trackId);
        verify(albumRepository).updateAvailabilityStatusById(EntityStatus.HIDDEN, albumId);
        verify(catalogEventProducer).albumHidden(albumId);
    }

    @Test
    void activeAlbum_cascadesToTracksAndUpdatesAvailability() {
        UUID albumId = UUID.randomUUID();
        UUID trackId = UUID.randomUUID();
        when(trackService.findAllIdsByAlbum(albumId)).thenReturn(List.of(trackId));

        albumService.activeAlbum(albumId);

        verify(trackService).findAllIdsByAlbum(albumId);
        verify(trackService).activeTrack(trackId);
        verify(albumRepository).updateAvailabilityStatusById(EntityStatus.ACTIVE, albumId);
        verify(catalogEventProducer).albumActivated(albumId);
    }

    @Test
    void updateLifecycleStatus_updatesDbAndEmitsEvent() {
        UUID albumId = UUID.randomUUID();

        albumService.updateLifecycleStatus(LifecycleStatus.READY, albumId);

        verify(albumRepository).updateLifecycleStatusById(LifecycleStatus.READY, albumId);
        verify(catalogEventProducer).updateAlbumLifecycleStatus(LifecycleStatus.READY, albumId);
    }

    @Test
    void addAllArtistToAlbum_savesJoinAndEmitsEvent() {
        UUID albumId = UUID.randomUUID();
        UUID artistId = UUID.randomUUID();
        String encodedArtistId = Url62.encode(artistId);

        ArtistDto dto = new ArtistDto();
        dto.setId(encodedArtistId);
        dto.setRole(ArtistRole.MAIN_ARTIST);

        when(albumRepository.existsById(albumId)).thenReturn(true);
        when(artistService.artistExists(artistId)).thenReturn(true);

        albumService.addAllArtistToAlbum(List.of(dto), albumId);

        verify(artistToAlbumRepository).save(any(ArtistToAlbum.class));
        verify(catalogEventProducer).artistAddedToAlbum(artistId, albumId, ArtistRole.MAIN_ARTIST);
    }

    @Test
    void deleteAllArtistFromAlbum_deletesJoinAndEmitsEvent() {
        UUID albumId = UUID.randomUUID();
        UUID artistId = UUID.randomUUID();
        String encodedArtistId = Url62.encode(artistId);

        ArtistDto dto = new ArtistDto();
        dto.setId(encodedArtistId);
        dto.setRole(ArtistRole.FEATURED_ARTIST);

        albumService.deleteAllArtistFromAlbum(List.of(dto), albumId);

        verify(artistToAlbumRepository).deleteById(new ArtistToAlbumPK(artistId, albumId));
        verify(catalogEventProducer).artistDeletedFromAlbum(artistId, albumId);
    }

    @Test
    void albumExists_delegatesToRepository() {
        UUID albumId = UUID.randomUUID();
        when(albumRepository.existsById(albumId)).thenReturn(true);

        boolean result = albumService.albumExists(albumId);

        assertThat(result).isTrue();
        verify(albumRepository).existsById(albumId);
    }
}
