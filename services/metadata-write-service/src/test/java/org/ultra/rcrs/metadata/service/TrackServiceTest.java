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
import org.ultra.rcrs.exceptions.NotFoundException;
import org.ultra.rcrs.metadata.dto.ArtistDto;
import org.ultra.rcrs.metadata.dto.OtherArtistDto;
import org.ultra.rcrs.metadata.dto.request.TrackUploadRequest;
import org.ultra.rcrs.metadata.kafka.CatalogEventProducer;
import org.ultra.rcrs.metadata.model.ArtistToTrack;
import org.ultra.rcrs.metadata.model.OtherArtist;
import org.ultra.rcrs.metadata.model.Track;
import org.ultra.rcrs.metadata.repository.ArtistToTrackRepository;
import org.ultra.rcrs.metadata.repository.OtherArtistRepository;
import org.ultra.rcrs.metadata.repository.TrackRepository;
import org.ultra.rcrs.utils.Url62;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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
    void createTrack() {
        UUID albumId = UUID.randomUUID();
        var request = new TrackUploadRequest();
        request.setAlbumId(Url62.encode(albumId));
        request.setTitle("Test Track");
        request.setTrackNumber(1);
        request.setExplicit(false);

        UUID generatedId = UUID.randomUUID();
        var savedTrack = Track.builder()
                .id(generatedId)
                .title("Test Track")
                .trackNumber(1)
                .explicit(false)
                .lifecycleStatus(LifecycleStatus.CREATED)
                .availabilityStatus(EntityStatus.ACTIVE)
                .albumId(albumId)
                .durationMs(null)
                .build();

        when(albumService.albumExists(albumId)).thenReturn(true);
        when(trackRepository.save(any(Track.class))).thenReturn(savedTrack);

        UUID result = trackService.createTrack(request);

        assertEquals(generatedId, result);

        ArgumentCaptor<Track> trackCaptor = ArgumentCaptor.forClass(Track.class);
        verify(trackRepository).save(trackCaptor.capture());
        Track captured = trackCaptor.getValue();
        assertEquals("Test Track", captured.getTitle());
        assertEquals(1, captured.getTrackNumber());
        assertFalse(captured.getExplicit());
        assertEquals(LifecycleStatus.CREATED, captured.getLifecycleStatus());
        assertEquals(EntityStatus.ACTIVE, captured.getAvailabilityStatus());
        assertNull(captured.getDurationMs());

        verify(catalogEventProducer).trackCreated(savedTrack);
        verify(catalogEventProducer).trackAddedToAlbum(generatedId, albumId);
    }

    @Test
    void createTrackAlbumNotFound() {
        UUID albumId = UUID.randomUUID();
        var request = new TrackUploadRequest();
        request.setAlbumId(Url62.encode(albumId));

        when(albumService.albumExists(albumId)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> trackService.createTrack(request));
        verify(trackRepository, never()).save(any());
        verifyNoInteractions(catalogEventProducer);
    }

    @Test
    void createTrackWithExplicit() {
        UUID albumId = UUID.randomUUID();
        var request = new TrackUploadRequest();
        request.setAlbumId(Url62.encode(albumId));
        request.setTitle("Explicit Track");
        request.setTrackNumber(2);
        request.setExplicit(true);

        UUID generatedId = UUID.randomUUID();
        var savedTrack = Track.builder()
                .id(generatedId)
                .title("Explicit Track")
                .trackNumber(2)
                .explicit(true)
                .lifecycleStatus(LifecycleStatus.CREATED)
                .availabilityStatus(EntityStatus.ACTIVE)
                .albumId(albumId)
                .build();

        when(albumService.albumExists(albumId)).thenReturn(true);
        when(trackRepository.save(any(Track.class))).thenReturn(savedTrack);

        trackService.createTrack(request);

        ArgumentCaptor<Track> captor = ArgumentCaptor.forClass(Track.class);
        verify(trackRepository).save(captor.capture());
        assertTrue(captor.getValue().getExplicit());
    }

    @Test
    void markTrackDelete() {
        UUID trackId = UUID.randomUUID();
        trackService.markTrackDelete(trackId);
        verify(trackRepository).updateAvailabilityStatusById(EntityStatus.DELETED, trackId);
        verify(catalogEventProducer).trackDeleted(trackId);
    }

    @Test
    void hideTrack() {
        UUID trackId = UUID.randomUUID();
        trackService.hideTrack(trackId);
        verify(trackRepository).updateAvailabilityStatusById(EntityStatus.HIDDEN, trackId);
        verify(catalogEventProducer).trackHidden(trackId);
    }

    @Test
    void activeTrack() {
        UUID trackId = UUID.randomUUID();
        trackService.activeTrack(trackId);
        verify(trackRepository).updateAvailabilityStatusById(EntityStatus.ACTIVE, trackId);
        verify(catalogEventProducer).trackActivated(trackId);
    }

    @Test
    void addArtistToTrack() {
        UUID trackId = UUID.randomUUID();
        UUID artistId = UUID.randomUUID();
        var artistDto = new ArtistDto();
        artistDto.setId(Url62.encode(artistId));

        when(trackRepository.existsById(trackId)).thenReturn(true);
        when(artistService.artistExists(artistId)).thenReturn(true);
        when(artistToTrackRepository.save(any(ArtistToTrack.class))).thenReturn(mock(ArtistToTrack.class));

        trackService.addAllArtistToTrack(List.of(artistDto), trackId);

        verify(artistToTrackRepository).save(any(ArtistToTrack.class));
        verify(catalogEventProducer).artistAddedToTrack(eq(artistId), eq(trackId), any());
    }

    @Test
    void addArtistToTrackNotFound() {
        UUID trackId = UUID.randomUUID();
        var artistDto = new ArtistDto();
        artistDto.setId(Url62.encode(UUID.randomUUID()));

        when(trackRepository.existsById(trackId)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> trackService.addAllArtistToTrack(List.of(artistDto), trackId));
    }

    @Test
    void addOthersToTrack() {
        UUID trackId = UUID.randomUUID();
        var otherDto = new OtherArtistDto();
        otherDto.setId(Url62.encode(UUID.randomUUID()));
        otherDto.setName("Other Artist");
        otherDto.setRoles(Set.of(ArtistRole.FEATURED_ARTIST));

        var saved = new OtherArtist(otherDto, trackId);
        saved.setId(UUID.randomUUID());
        when(trackRepository.existsById(trackId)).thenReturn(true);
        when(otherArtistRepository.save(any(OtherArtist.class))).thenReturn(saved);

        trackService.addAllOthersToTrack(List.of(otherDto), trackId);

        verify(otherArtistRepository).save(any(OtherArtist.class));
        verify(catalogEventProducer).otherAddedToTrack(eq(saved), eq(trackId));
    }
}
