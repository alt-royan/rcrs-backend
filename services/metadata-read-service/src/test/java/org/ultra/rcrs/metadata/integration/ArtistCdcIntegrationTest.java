package org.ultra.rcrs.metadata.integration;

import org.junit.jupiter.api.Test;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.enums.ArtistRole;
import org.ultra.rcrs.enums.LifecycleStatus;
import org.ultra.rcrs.events.artist.ArtistCreatedEventOuterClass;
import org.ultra.rcrs.events.common.AvailabilityStatusOuterClass;
import org.ultra.rcrs.events.common.DomainEventOuterClass;
import org.ultra.rcrs.metadata.model.AlbumPublicDocument;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

class ArtistCdcIntegrationTest extends BaseIntegrationTest {

    @Test
    void artistCreated_createsDocumentInMongo() throws Exception {
        String id = randomId();
        sendArtistCreated(id, "CDC Artist", EntityStatus.ACTIVE);
        
        var doc = artistRepository.findById(id).block();
        assertThat(doc).isNotNull();
        assertThat(doc.getName()).isEqualTo("CDC Artist");
        assertThat(doc.getAvailabilityStatus()).isEqualTo(EntityStatus.ACTIVE);
    }

    @Test
    void artistCreated_withHiddenStatus_persistsCorrectly() throws Exception {
        String id = randomId();
        sendEvent(DomainEventOuterClass.EventType.ARTIST_CREATED,
                DomainEventOuterClass.AggregateType.ARTIST, id,
                ArtistCreatedEventOuterClass.ArtistCreatedEvent.newBuilder()
                        .setId(id).setName("Hidden CDC Artist")
                        .setAvatarS3Key("avatars/hidden.jpg")
                        .setAvailabilityStatus(AvailabilityStatusOuterClass.AvailabilityStatus.HIDDEN)
                        .build());
        
        var doc = artistRepository.findById(id).block();
        assertThat(doc).isNotNull();
        assertThat(doc.getAvailabilityStatus()).isEqualTo(EntityStatus.HIDDEN);
    }

    @Test
    void artistDeleted_setsAvailabilityToDeleted() throws Exception {
        String id = randomId();
        sendArtistCreated(id, "Delete Me", EntityStatus.ACTIVE);
        
        sendArtistDeleted(id);
        
        var doc = artistRepository.findById(id).block();
        assertThat(doc).isNotNull();
        assertThat(doc.getAvailabilityStatus()).isEqualTo(EntityStatus.DELETED);
    }

    @Test
    void artistHidden_setsAvailabilityToHidden() throws Exception {
        String id = randomId();
        sendArtistCreated(id, "Hide Me", EntityStatus.ACTIVE);
        
        sendArtistHidden(id);
        
        var doc = artistRepository.findById(id).block();
        assertThat(doc).isNotNull();
        assertThat(doc.getAvailabilityStatus()).isEqualTo(EntityStatus.HIDDEN);
    }

    @Test
    void artistActivated_setsAvailabilityToActive() throws Exception {
        String id = randomId();
        sendArtistCreated(id, "Activate Me", EntityStatus.HIDDEN);
        
        sendArtistActivated(id);
        
        var doc = artistRepository.findById(id).block();
        assertThat(doc).isNotNull();
        assertThat(doc.getAvailabilityStatus()).isEqualTo(EntityStatus.ACTIVE);
    }

    @Test
    void artistTrueDeleted_removesDocumentFromMongo() throws Exception {
        String id = randomId();
        sendArtistCreated(id, "True Delete Me", EntityStatus.ACTIVE);
        
        sendArtistTrueDeleted(id);
        
        var doc = artistRepository.findById(id).block();
        assertThat(doc).isNull();
    }

    @Test
    void artistAddedToAlbum_addsArtistEmbedToAlbum() throws Exception {
        String artistId = randomId();
        String albumId = randomId();

        sendArtistCreated(artistId, "Embed Artist", EntityStatus.ACTIVE);
        
        sendAlbumCreated(albumId, "Album For Embed");
        
        sendArtistAddedToAlbum(artistId, albumId, ArtistRole.MAIN_ARTIST);
        
        var album = albumRepository.findById(albumId).block();
        assertThat(album).isNotNull();
        assertThat(album.getArtists()).hasSize(1);
        assertThat(album.getArtists().getFirst().getId()).isEqualTo(artistId);
        assertThat(album.getArtists().getFirst().getName()).isEqualTo("Embed Artist");
        assertThat(album.getArtists().getFirst().getRole()).isEqualTo(ArtistRole.MAIN_ARTIST);
    }

    @Test
    void artistDeletedFromAlbum_removesArtistEmbedFromAlbum() throws Exception {
        String artistId = randomId();
        String albumId = randomId();

        sendArtistCreated(artistId, "Remove Artist", EntityStatus.ACTIVE);
        
        sendAlbumCreated(albumId, "Album For Remove");
        
        sendArtistAddedToAlbum(artistId, albumId, ArtistRole.MAIN_ARTIST);
        
        var album = albumRepository.findById(albumId).block();
        assertThat(Objects.requireNonNull(album).getArtists()).hasSize(1);

        sendArtistDeletedFromAlbum(artistId, albumId);
        
        album = albumRepository.findById(albumId).block();
        assertThat(album).isNotNull();
        assertThat(album.getArtists()).isEmpty();
    }
}
