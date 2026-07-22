package org.ultra.rcrs.metadata.integration;

import org.junit.jupiter.api.Test;
import org.ultra.rcrs.enums.AlbumType;
import org.ultra.rcrs.enums.ArtistRole;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.enums.LifecycleStatus;
import org.ultra.rcrs.metadata.model.AlbumPublicDocument;

import static org.assertj.core.api.Assertions.assertThat;

class AlbumCdcIntegrationTest extends BaseIntegrationTest {

    @Test
    void albumCreated_createsDocumentInMongo() throws Exception {
        String id = randomId();
        sendAlbumCreated(id, "CDC Album");
        
        var doc = albumRepository.findById(id).block();
        assertThat(doc).isNotNull();
        assertThat(doc.getTitle()).isEqualTo("CDC Album");
        assertThat(doc.getType()).isEqualTo(AlbumType.FULL);
        assertThat(doc.getLifecycleStatus()).isEqualTo(LifecycleStatus.CREATED);
        assertThat(doc.getAvailabilityStatus()).isEqualTo(EntityStatus.ACTIVE);
    }

    @Test
    void albumDeleted_setsAvailabilityToDeleted() throws Exception {
        String id = randomId();
        sendAlbumCreated(id, "Delete Album");
        
        sendAlbumDeleted(id);
        
        var doc = albumRepository.findById(id).block();
        assertThat(doc).isNotNull();
        assertThat(doc.getAvailabilityStatus()).isEqualTo(EntityStatus.DELETED);
    }

    @Test
    void albumHidden_setsAvailabilityToHidden() throws Exception {
        String id = randomId();
        sendAlbumCreated(id, "Hidden Album");
        
        sendAlbumHidden(id);
        
        var doc = albumRepository.findById(id).block();
        assertThat(doc).isNotNull();
        assertThat(doc.getAvailabilityStatus()).isEqualTo(EntityStatus.HIDDEN);
    }

    @Test
    void albumActivated_setsAvailabilityToActive() throws Exception {
        String id = randomId();
        sendAlbumCreated(id, "Activate Album");
        
        sendAlbumHidden(id);
        
        sendAlbumActivated(id);
        
        var doc = albumRepository.findById(id).block();
        assertThat(doc).isNotNull();
        assertThat(doc.getAvailabilityStatus()).isEqualTo(EntityStatus.ACTIVE);
    }

    @Test
    void albumLifecycleStatusUpdated_changesLifecycle() throws Exception {
        String id = randomId();
        sendAlbumCreated(id, "Lifecycle Album");
        
        sendAlbumLifecycleStatusUpdated(id, LifecycleStatus.PUBLISHED);
        
        var doc = albumRepository.findById(id).block();
        assertThat(doc).isNotNull();
        assertThat(doc.getLifecycleStatus()).isEqualTo(LifecycleStatus.PUBLISHED);
    }

    @Test
    void albumTrueDeleted_removesDocumentFromMongo() throws Exception {
        String id = randomId();
        sendAlbumCreated(id, "True Delete Album");
        
        sendAlbumTrueDeleted(id);
        
        var doc = albumRepository.findById(id).block();
        assertThat(doc).isNull();
    }

    @Test
    void artistAddedToAlbum_addsEmbed() throws Exception {
        String artistId = randomId();
        String albumId = randomId();

        sendArtistCreated(artistId, "Album Artist", EntityStatus.ACTIVE);
        
        sendAlbumCreated(albumId, "Album With Artist");
        
        sendArtistAddedToAlbum(artistId, albumId, ArtistRole.MAIN_ARTIST);
        
        var doc = albumRepository.findById(albumId).block();
        assertThat(doc).isNotNull();
         assertThat(doc.getArtists()).hasSize(1);
        assertThat(doc.getArtists().getFirst().getId()).isEqualTo(artistId);
        assertThat(doc.getArtists().getFirst().getName()).isEqualTo("Album Artist");
        assertThat(doc.getArtists().getFirst().getRole()).isEqualTo(ArtistRole.MAIN_ARTIST);
    }

    @Test
    void artistDeletedFromAlbum_removesEmbed() throws Exception {
        String artistId = randomId();
        String albumId = randomId();

        sendArtistCreated(artistId, "Remove From Album", EntityStatus.ACTIVE);
        
        sendAlbumCreated(albumId, "Album Remove Artist");
        
        sendArtistAddedToAlbum(artistId, albumId, ArtistRole.MAIN_ARTIST);
        
        sendArtistDeletedFromAlbum(artistId, albumId);
        
        var doc = albumRepository.findById(albumId).block();
        assertThat(doc).isNotNull();
        assertThat(doc.getArtists()).isEmpty();
    }

    @Test
    void artistAddedToAlbum_multipleArtists() throws Exception {
        String artist1Id = randomId();
        String artist2Id = randomId();
        String albumId = randomId();

        sendArtistCreated(artist1Id, "Artist One", EntityStatus.ACTIVE);
        
        sendArtistCreated(artist2Id, "Artist Two", EntityStatus.ACTIVE);
        
        sendAlbumCreated(albumId, "Multi Artist Album");
        
        sendArtistAddedToAlbum(artist1Id, albumId, ArtistRole.MAIN_ARTIST);
        
        sendArtistAddedToAlbum(artist2Id, albumId, ArtistRole.FEATURED_ARTIST);
        
        var doc = albumRepository.findById(albumId).block();
        assertThat(doc).isNotNull();
        assertThat(doc.getArtists()).hasSize(2);

        var ids = doc.getArtists().stream().map(AlbumPublicDocument.ArtistEmbed::getId).toList();
        assertThat(ids).containsExactlyInAnyOrder(artist1Id, artist2Id);
    }

    @Test
    void artistDeletedFromAlbum_onlyRemovesSpecificArtist() throws Exception {
        String artist1Id = randomId();
        String artist2Id = randomId();
        String albumId = randomId();

        sendArtistCreated(artist1Id, "Keep Artist", EntityStatus.ACTIVE);
        
        sendArtistCreated(artist2Id, "Remove Artist", EntityStatus.ACTIVE);
        
        sendAlbumCreated(albumId, "Selective Remove");
        
        sendArtistAddedToAlbum(artist1Id, albumId, ArtistRole.MAIN_ARTIST);
        
        sendArtistAddedToAlbum(artist2Id, albumId, ArtistRole.FEATURED_ARTIST);
        
        sendArtistDeletedFromAlbum(artist2Id, albumId);
        
        var doc = albumRepository.findById(albumId).block();
        assertThat(doc).isNotNull();
        assertThat(doc.getArtists()).hasSize(1);
        assertThat(doc.getArtists().getFirst().getId()).isEqualTo(artist1Id);
    }

    @Test
    void artistAddedToAlbum_whenAlbumDoesNotExist_noError() throws Exception {
        String artistId = randomId();
        String albumId = randomId();

        sendArtistCreated(artistId, "Orphan Artist", EntityStatus.ACTIVE);
        
        sendArtistAddedToAlbum(artistId, albumId, ArtistRole.MAIN_ARTIST);
        
        var doc = albumRepository.findById(albumId).block();
        assertThat(doc).isNull();
    }

    @Test
    void artistAddedToAlbum_whenArtistDoesNotExist_noError() throws Exception {
        String artistId = randomId();
        String albumId = randomId();

        sendAlbumCreated(albumId, "No Artist Album");
        
        sendArtistAddedToAlbum(artistId, albumId, ArtistRole.MAIN_ARTIST);
        
        var doc = albumRepository.findById(albumId).block();
        assertThat(doc).isNotNull();
        assertThat(doc.getArtists()).isNull();
    }
}
