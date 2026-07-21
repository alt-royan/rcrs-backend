package org.ultra.rcrs.metadata.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.metadata.kafka.CatalogEventProducer;
import org.ultra.rcrs.metadata.repository.*;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurgeServiceTest {

    @Mock
    private TrackRepository trackRepository;
    @Mock
    private AlbumRepository albumRepository;
    @Mock
    private ArtistRepository artistRepository;
    @Mock
    private OtherArtistRepository otherArtistRepository;
    @Mock
    private ArtistToTrackRepository artistToTrackRepository;
    @Mock
    private ArtistToAlbumRepository artistToAlbumRepository;
    @Mock
    private CatalogEventProducer catalogEventProducer;

    @InjectMocks
    private PurgeService purgeService;

    @Test
    void purge_deletesAllSoftDeletedEntitiesAndEmitsEvents() {
        UUID trackId = UUID.randomUUID();
        UUID albumId = UUID.randomUUID();
        UUID artistId = UUID.randomUUID();

        when(trackRepository.findAllByAvailabilityStatus(EntityStatus.DELETED)).thenReturn(List.of(trackId));
        when(albumRepository.findAllByAvailabilityStatus(EntityStatus.DELETED)).thenReturn(List.of(albumId));
        when(artistRepository.findAllByAvailabilityStatus(EntityStatus.DELETED)).thenReturn(List.of(artistId));

        purgeService.purge();

        verify(otherArtistRepository).deleteByTrackIdsIn(List.of(trackId));
        verify(artistToTrackRepository).deleteByTrackIdsIn(List.of(trackId));
        verify(trackRepository).deleteAllByIdInBatch(List.of(trackId));
        verify(catalogEventProducer).trackTrueDeleted(trackId);

        verify(artistToAlbumRepository).deleteByAlbumIdsIn(List.of(albumId));
        verify(albumRepository).deleteAllByIdInBatch(List.of(albumId));
        verify(catalogEventProducer).albumTrueDeleted(albumId);

        verify(artistRepository).deleteAllByIdInBatch(List.of(artistId));
        verify(catalogEventProducer).artistTrueDeleted(artistId);
    }

    @Test
    void purge_skipsEmptyLists() {
        when(trackRepository.findAllByAvailabilityStatus(EntityStatus.DELETED)).thenReturn(List.of());
        when(albumRepository.findAllByAvailabilityStatus(EntityStatus.DELETED)).thenReturn(List.of());
        when(artistRepository.findAllByAvailabilityStatus(EntityStatus.DELETED)).thenReturn(List.of());

        purgeService.purge();

        verify(otherArtistRepository, never()).deleteByTrackIdsIn(any());
        verify(artistToTrackRepository, never()).deleteByTrackIdsIn(any());
        verify(trackRepository, never()).deleteAllByIdInBatch(any());
        verify(artistToAlbumRepository, never()).deleteByAlbumIdsIn(any());
        verify(albumRepository, never()).deleteAllByIdInBatch(any());
        verify(artistRepository, never()).deleteAllByIdInBatch(any());
        verifyNoInteractions(catalogEventProducer);
    }
}
