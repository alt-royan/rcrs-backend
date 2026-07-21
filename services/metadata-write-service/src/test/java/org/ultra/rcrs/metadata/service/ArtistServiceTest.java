package org.ultra.rcrs.metadata.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.metadata.dto.request.ArtistCreateRequest;
import org.ultra.rcrs.metadata.kafka.CatalogEventProducer;
import org.ultra.rcrs.metadata.model.Artist;
import org.ultra.rcrs.metadata.model.SocialLinks;
import org.ultra.rcrs.metadata.repository.ArtistRepository;
import org.ultra.rcrs.utils.S3Utils;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArtistServiceTest {

    @Mock
    private ArtistRepository artistRepository;
    @Mock
    private AlbumService albumService;
    @Mock
    private TrackService trackService;
    @Mock
    private CatalogEventProducer catalogEventProducer;
    @Mock
    private S3Utils s3Utils;

    @InjectMocks
    private ArtistService artistService;

    @Test
    void createArtist_savesArtistAndEmitsEvent() {
        ArtistCreateRequest request = new ArtistCreateRequest();
        request.setName("Test Artist");
        request.setAvatarUri("s3://bucket/avatar.jpg");
        request.setSocialLinks(List.of());
        request.setTags(List.of("rock"));

        when(s3Utils.parseKey("s3://bucket/avatar.jpg")).thenReturn("avatar.jpg");

        Artist artistExpected = Artist.builder()
                .id(UUID.randomUUID())
                .name(request.getName())
                .avatarS3Key(s3Utils.parseKey(request.getAvatarUri()))
                .socialLinks(new SocialLinks(request.getSocialLinks()))
                .tags(request.getTags())
                .availabilityStatus(EntityStatus.ACTIVE)
                .build();

        when(artistRepository.save(any(Artist.class))).thenAnswer(i -> {
            var artistSaved = (Artist) i.getArguments()[0];
            artistSaved.setId(artistExpected.getId());
            return artistSaved;
        });
        ArgumentCaptor<Artist> artistCaptor = ArgumentCaptor.forClass(Artist.class);

        UUID result = artistService.createArtist(request);
        assertThat(result).isEqualTo(artistExpected.getId());

        verify(artistRepository).save(artistCaptor.capture());
        Artist captured = artistCaptor.getValue();
        captured.setId(artistExpected.getId());
        assertThat(captured.getName()).isEqualTo(artistExpected.getName());
        assertThat(captured.getAvatarS3Key()).isEqualTo(artistExpected.getAvatarS3Key());
        assertThat(captured.getSocialLinks()).isEqualTo(artistExpected.getSocialLinks());
        assertThat(captured.getTags()).isEqualTo(artistExpected.getTags());
        assertThat(captured.getAvailabilityStatus()).isEqualTo(artistExpected.getAvailabilityStatus());
        assertThat(captured.getId()).isEqualTo(artistExpected.getId());

        verify(catalogEventProducer).artistCreated(artistCaptor.capture());
    }

    @Test
    void markArtistDelete_cascadesToAlbumsAndUpdatesAvailability() {
        UUID artistId = UUID.randomUUID();
        UUID albumId1 = UUID.randomUUID();
        UUID albumId2 = UUID.randomUUID();
        when(albumService.findAllIdsByArtist(artistId)).thenReturn(List.of(albumId1, albumId2));

        artistService.markArtistDelete(artistId);

        verify(albumService).findAllIdsByArtist(artistId);
        verify(albumService).markAlbumDelete(albumId1);
        verify(albumService).markAlbumDelete(albumId2);
        verify(artistRepository).updateAvailabilityStatusById(EntityStatus.DELETED, artistId);
        verify(catalogEventProducer).artistDeleted(artistId);
    }

    @Test
    void hideArtist_cascadesToAlbumsAndUpdatesAvailability() {
        UUID artistId = UUID.randomUUID();
        UUID albumId = UUID.randomUUID();
        when(albumService.findAllIdsByArtist(artistId)).thenReturn(List.of(albumId));

        artistService.hideArtist(artistId);

        verify(albumService).findAllIdsByArtist(artistId);
        verify(albumService).hideAlbum(albumId);
        verify(artistRepository).updateAvailabilityStatusById(EntityStatus.HIDDEN, artistId);
        verify(catalogEventProducer).artistHidden(artistId);
    }

    @Test
    void activeArtist_cascadesToAlbumsAndUpdatesAvailability() {
        UUID artistId = UUID.randomUUID();
        UUID albumId = UUID.randomUUID();
        when(albumService.findAllIdsByArtist(artistId)).thenReturn(List.of(albumId));

        artistService.activeArtist(artistId);

        verify(albumService).findAllIdsByArtist(artistId);
        verify(albumService).activeAlbum(albumId);
        verify(artistRepository).updateAvailabilityStatusById(EntityStatus.ACTIVE, artistId);
        verify(catalogEventProducer).artistActivated(artistId);
    }

    @Test
    void artistExists_delegatesToRepository() {
        UUID artistId = UUID.randomUUID();
        when(artistRepository.existsById(artistId)).thenReturn(true);

        boolean result = artistService.artistExists(artistId);

        assertThat(result).isTrue();
        verify(artistRepository).existsById(artistId);
    }
}
