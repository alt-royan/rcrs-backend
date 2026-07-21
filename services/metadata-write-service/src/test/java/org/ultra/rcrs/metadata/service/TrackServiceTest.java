package org.ultra.rcrs.metadata.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ultra.rcrs.enums.ArtistRole;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.enums.LifecycleStatus;
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
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrackServiceTest {

    @Mock
    private TrackRepository trackRepository;
    @Mock
    private ArtistToTrackRepository artistToTrackRepository;
    @Mock
    private OtherArtistRepository otherArtistRepository;
    @Mock
    private ArtistService artistService;
    @Mock
    private AlbumService albumService;
    @Mock
    private CatalogEventProducer catalogEventProducer;

    @InjectMocks
    private TrackService trackService;

    @Test
    void createTrack_savesTrackAndEmitsEvents() {
        UUID albumId = UUID.randomUUID();
        String encodedAlbumId = Url62.encode(albumId);

        TrackUploadRequest request = new TrackUploadRequest();
        request.setAlbumId(encodedAlbumId);
        request.setTitle("Test Track");
        request.setTrackNumber(1);
        request.setExplicit(false);

        when(albumService.albumExists(albumId)).thenReturn(true);

        Track trackExpected = Track.builder()
                .id(UUID.randomUUID())
                .lifecycleStatus(LifecycleStatus.CREATED)
                .title(request.getTitle())
                .durationMs(null)
                .trackNumber(request.getTrackNumber())
                .explicit(request.getExplicit())
                .availabilityStatus(EntityStatus.ACTIVE)
                .albumId(albumId)
                .build();

        when(trackRepository.save(any(Track.class))).thenAnswer(i -> {
            var trackSaved = (Track) i.getArguments()[0];
            trackSaved.setId(trackExpected.getId());
            return trackSaved;
        });
        ArgumentCaptor<Track> trackCaptor = ArgumentCaptor.forClass(Track.class);

        UUID result = trackService.createTrack(request);
        assertThat(result).isEqualTo(trackExpected.getId());

        verify(trackRepository).save(trackCaptor.capture());
        Track captured = trackCaptor.getValue();
        captured.setId(trackExpected.getId());
        assertThat(captured.getTitle()).isEqualTo(trackExpected.getTitle());
        assertThat(captured.getLifecycleStatus()).isEqualTo(trackExpected.getLifecycleStatus());
        assertThat(captured.getDurationMs()).isEqualTo(trackExpected.getDurationMs());
        assertThat(captured.getTrackNumber()).isEqualTo(trackExpected.getTrackNumber());
        assertThat(captured.getExplicit()).isEqualTo(trackExpected.getExplicit());
        assertThat(captured.getAvailabilityStatus()).isEqualTo(trackExpected.getAvailabilityStatus());
        assertThat(captured.getAlbumId()).isEqualTo(trackExpected.getAlbumId());
        assertThat(captured.getId()).isEqualTo(trackExpected.getId());

        verify(catalogEventProducer).trackCreated(trackCaptor.capture());
        verify(catalogEventProducer).trackAddedToAlbum(trackExpected.getId(), albumId);
    }

    @Test
    void findAllIdsByAlbum_delegatesToRepository() {
        UUID albumId = UUID.randomUUID();
        List<UUID> expected = List.of(UUID.randomUUID(), UUID.randomUUID());
        when(trackRepository.findAllIdsByAlbumId(albumId)).thenReturn(expected);

        List<UUID> result = trackService.findAllIdsByAlbum(albumId);

        assertThat(result).isEqualTo(expected);
        verify(trackRepository).findAllIdsByAlbumId(albumId);
    }

    @Test
    void markTrackDelete_updatesAvailabilityAndEmitsEvent() {
        UUID trackId = UUID.randomUUID();

        trackService.markTrackDelete(trackId);

        verify(trackRepository).updateAvailabilityStatusById(EntityStatus.DELETED, trackId);
        verify(catalogEventProducer).trackDeleted(trackId);
    }

    @Test
    void hideTrack_updatesAvailabilityAndEmitsEvent() {
        UUID trackId = UUID.randomUUID();

        trackService.hideTrack(trackId);

        verify(trackRepository).updateAvailabilityStatusById(EntityStatus.HIDDEN, trackId);
        verify(catalogEventProducer).trackHidden(trackId);
    }

    @Test
    void activeTrack_updatesAvailabilityAndEmitsEvent() {
        UUID trackId = UUID.randomUUID();

        trackService.activeTrack(trackId);

        verify(trackRepository).updateAvailabilityStatusById(EntityStatus.ACTIVE, trackId);
        verify(catalogEventProducer).trackActivated(trackId);
    }

    @Test
    void updateLifecycleStatus_updatesDbAndEmitsEvent() {
        UUID trackId = UUID.randomUUID();

        trackService.updateLifecycleStatus(LifecycleStatus.TRANSCODING, trackId);

        verify(trackRepository).updateLifecycleStatusById(LifecycleStatus.TRANSCODING, trackId);
        verify(catalogEventProducer).updateTrackLifecycleStatus(LifecycleStatus.TRANSCODING, trackId);
    }

    @Test
    void handleTranscodingCompleted_updatesStatusAndDuration() {
        UUID trackId = UUID.randomUUID();

        trackService.handleTranscodingCompleted(trackId, LifecycleStatus.READY, 240000);

        verify(trackRepository).updateLifecycleStatusAndDurationById(LifecycleStatus.READY, 240000, trackId);
    }

    @Test
    void addAllArtistToTrack_savesJoinAndEmitsEvent() {
        UUID trackId = UUID.randomUUID();
        UUID artistId = UUID.randomUUID();
        String encodedArtistId = Url62.encode(artistId);

        ArtistDto dto = new ArtistDto();
        dto.setId(encodedArtistId);
        dto.setRole(ArtistRole.MAIN_ARTIST);

        when(trackRepository.existsById(trackId)).thenReturn(true);
        when(artistService.artistExists(artistId)).thenReturn(true);

        trackService.addAllArtistToTrack(List.of(dto), trackId);

        verify(artistToTrackRepository).save(any(ArtistToTrack.class));
        verify(catalogEventProducer).artistAddedToTrack(artistId, trackId, ArtistRole.MAIN_ARTIST);
    }

    @Test
    void addAllOthersToTrack_savesOtherAndEmitsEvent() {
        UUID trackId = UUID.randomUUID();

        OtherArtistDto dto = new OtherArtistDto();
        dto.setName("Guest Artist");
        dto.setRoles(Set.of(ArtistRole.FEATURED_ARTIST));
        dto.setSocialLinks(List.of());

        when(trackRepository.existsById(trackId)).thenReturn(true);
        when(otherArtistRepository.save(any(OtherArtist.class))).thenReturn(new OtherArtist(dto, trackId));

        trackService.addAllOthersToTrack(List.of(dto), trackId);

        verify(otherArtistRepository).save(any(OtherArtist.class));
        verify(catalogEventProducer).otherAddedToTrack(any(OtherArtist.class), eq(trackId));
    }

    @Test
    void deleteAllArtistFromTrack_deletesJoinAndEmitsEvent() {
        UUID trackId = UUID.randomUUID();
        UUID artistId = UUID.randomUUID();
        String encodedArtistId = Url62.encode(artistId);

        ArtistDto dto = new ArtistDto();
        dto.setId(encodedArtistId);
        dto.setRole(ArtistRole.MAIN_ARTIST);

        trackService.deleteAllArtistFromTrack(List.of(dto), trackId);

        verify(artistToTrackRepository).deleteById(new ArtistToTrackPK(artistId, trackId));
        verify(catalogEventProducer).artistDeletedFromTrack(artistId, trackId);
    }

    @Test
    void deleteAllOthersFromTrack_deletesOtherAndEmitsEvent() {
        UUID trackId = UUID.randomUUID();
        UUID otherId = UUID.randomUUID();
        String encodedOtherId = Url62.encode(otherId);

        OtherArtistDto dto = new OtherArtistDto();
        dto.setId(encodedOtherId);
        dto.setName("Guest");

        trackService.deleteAllOthersFromTrack(List.of(dto), trackId);

        verify(otherArtistRepository).deleteById(otherId);
        verify(catalogEventProducer).otherDeletedFromTrack(otherId, trackId);
    }
}
