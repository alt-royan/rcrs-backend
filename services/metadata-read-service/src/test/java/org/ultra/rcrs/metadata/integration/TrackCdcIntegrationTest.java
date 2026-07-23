package org.ultra.rcrs.metadata.integration;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.ultra.rcrs.enums.ArtistRole;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.enums.LifecycleStatus;
import org.ultra.rcrs.metadata.model.TrackDocument;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TrackCdcIntegrationTest extends BaseIntegrationTest {

    @Test
    @Order(1)
    void trackCreated_createsDocumentInMongo() throws Exception {
        Thread.sleep(5000);
        String id = randomId();
        sendTrackCreated(id, "CDC Track");
        
        var doc = trackRepository.findById(id).block();
        assertThat(doc).isNotNull();
        assertThat(doc.getTitle()).isEqualTo("CDC Track");
        assertThat(doc.getTrackNumber()).isEqualTo(1);
        assertThat(doc.getAvailabilityStatus()).isEqualTo(EntityStatus.ACTIVE);
        assertThat(doc.getLifecycleStatus()).isEqualTo(LifecycleStatus.CREATED);
    }

    @Test
    @Order(2)
    void trackDeleted_setsAvailabilityToDeleted() throws Exception {
        String id = randomId();
        sendTrackCreated(id, "Delete Track");
        
        sendTrackDeleted(id);
        
        var doc = trackRepository.findById(id).block();
        assertThat(doc).isNotNull();
        assertThat(doc.getAvailabilityStatus()).isEqualTo(EntityStatus.DELETED);
    }

    @Test
    @Order(3)
    void trackHidden_setsAvailabilityToHidden() throws Exception {
        String id = randomId();
        sendTrackCreated(id, "Hidden Track");
        
        sendTrackHidden(id);
        
        var doc = trackRepository.findById(id).block();
        assertThat(doc).isNotNull();
        assertThat(doc.getAvailabilityStatus()).isEqualTo(EntityStatus.HIDDEN);
    }

    @Test
    @Order(4)
    void trackActivated_setsAvailabilityToActive() throws Exception {
        String id = randomId();
        sendTrackCreated(id, "Activate Track");
        
        sendTrackHidden(id);
        
        sendTrackActivated(id);
        
        var doc = trackRepository.findById(id).block();
        assertThat(doc).isNotNull();
        assertThat(doc.getAvailabilityStatus()).isEqualTo(EntityStatus.ACTIVE);
    }

    @Test
    @Order(5)
    void trackLifecycleStatusUpdated_changesLifecycle() throws Exception {
        String id = randomId();
        sendTrackCreated(id, "Lifecycle Track");
        
        sendTrackLifecycleStatusUpdated(id, LifecycleStatus.PUBLISHED);
        
        var doc = trackRepository.findById(id).block();
        assertThat(doc).isNotNull();
        assertThat(doc.getLifecycleStatus()).isEqualTo(LifecycleStatus.PUBLISHED);
    }

    @Test
    @Order(6)
    void trackTrueDeleted_removesDocumentFromMongo() throws Exception {
        String id = randomId();
        sendTrackCreated(id, "True Delete Track");
        
        sendTrackTrueDeleted(id);
        
        var doc = trackRepository.findById(id).block();
        assertThat(doc).isNull();
    }

    @Test
    @Order(7)
    void trackAddedToAlbum_setsAlbumEmbedAndIncrementsTotalTracks() throws Exception {
        String trackId = randomId();
        String albumId = randomId();

        sendTrackCreated(trackId, "Track In Album");
        
        sendAlbumCreated(albumId, "Album For Track");
        
        sendTrackAddedToAlbum(trackId, albumId);
        
        var trackDoc = trackRepository.findById(trackId).block();
        assertThat(trackDoc).isNotNull();
        assertThat(trackDoc.getAlbum()).isNotNull();
        assertThat(trackDoc.getAlbum().getId()).isEqualTo(albumId);
        assertThat(trackDoc.getAlbum().getTitle()).isEqualTo("Album For Track");

        var albumDoc = albumRepository.findById(albumId).block();
        assertThat(albumDoc).isNotNull();
        assertThat(albumDoc.getTotalTracks()).isEqualTo(1);
    }

    @Test
    @Order(8)
    void trackAddedToAlbum_whenAlbumDoesNotExist_noError() throws Exception {
        String trackId = randomId();
        String albumId = randomId();

        sendTrackCreated(trackId, "Orphan Track");
        
        sendTrackAddedToAlbum(trackId, albumId);
        
        var doc = trackRepository.findById(trackId).block();
        assertThat(doc).isNotNull();
        assertThat(doc.getAlbum()).isNull();
    }

    @Test
    @Order(9)
    void artistAddedToTrack_addsEmbed() throws Exception {
        String artistId = randomId();
        String trackId = randomId();

        sendArtistCreated(artistId, "Track Artist", EntityStatus.ACTIVE);
        
        sendTrackCreated(trackId, "Track With Artist");
        
        sendArtistAddedToTrack(artistId, trackId, ArtistRole.MAIN_ARTIST);
        
        var doc = trackRepository.findById(trackId).block();
        assertThat(doc).isNotNull();
        assertThat(doc.getArtists()).hasSize(1);
        assertThat(doc.getArtists().get(0).getId()).isEqualTo(artistId);
        assertThat(doc.getArtists().get(0).getName()).isEqualTo("Track Artist");
        assertThat(doc.getArtists().get(0).getRole()).isEqualTo(ArtistRole.MAIN_ARTIST);
    }

    @Test
    @Order(10)
    void artistDeletedFromTrack_removesEmbed() throws Exception {
        String artistId = randomId();
        String trackId = randomId();

        sendArtistCreated(artistId, "Remove Track Artist", EntityStatus.ACTIVE);
        
        sendTrackCreated(trackId, "Track Remove Artist");
        
        sendArtistAddedToTrack(artistId, trackId, ArtistRole.MAIN_ARTIST);
        
        sendArtistDeletedFromTrack(artistId, trackId);
        
        var doc = trackRepository.findById(trackId).block();
        assertThat(doc).isNotNull();
        assertThat(doc.getArtists()).hasSize(0);
    }

    @Test
    @Order(11)
    void otherAddedToTrack_addsOtherEmbed() throws Exception {
        String trackId = randomId();
        String otherId = randomId();

        sendTrackCreated(trackId, "Track With Other");
        
        sendOtherAddedToTrack(otherId, trackId, "Featured Guest");
        
        var doc = trackRepository.findById(trackId).block();
        assertThat(doc).isNotNull();
        assertThat(doc.getOthers()).hasSize(1);
        assertThat(doc.getOthers().get(0).getId()).isEqualTo(otherId);
        assertThat(doc.getOthers().get(0).getName()).isEqualTo("Featured Guest");
    }

    @Test
    @Order(12)
    void otherDeletedFromTrack_removesOtherEmbed() throws Exception {
        String trackId = randomId();
        String otherId = randomId();

        sendTrackCreated(trackId, "Track Remove Other");
        
        sendOtherAddedToTrack(otherId, trackId, "Remove Me");
        
        sendOtherDeletedFromTrack(otherId, trackId);
        
        var doc = trackRepository.findById(trackId).block();
        assertThat(doc).isNotNull();
        assertThat(doc.getOthers()).hasSize(0);
    }

    @Test
    @Order(13)
    void artistAddedToTrack_whenTrackDoesNotExist_noError() throws Exception {
        String artistId = randomId();
        String trackId = randomId();

        sendArtistCreated(artistId, "Orphan Artist Track", EntityStatus.ACTIVE);
        
        sendArtistAddedToTrack(artistId, trackId, ArtistRole.MAIN_ARTIST);
        
        var doc = trackRepository.findById(trackId).block();
        assertThat(doc).isNull();
    }

    @Test
    @Order(14)
    void artistAddedToTrack_whenArtistDoesNotExist_noError() throws Exception {
        String trackId = randomId();
        String artistId = randomId();

        sendTrackCreated(trackId, "Track No Artist");
        
        sendArtistAddedToTrack(artistId, trackId, ArtistRole.MAIN_ARTIST);
        
        var doc = trackRepository.findById(trackId).block();
        assertThat(doc).isNotNull();
        assertThat(doc.getArtists()).isNull();
    }

    @Test
    @Order(15)
    void otherAddedToTrack_whenTrackDoesNotExist_noError() throws Exception {
        String trackId = randomId();
        String otherId = randomId();

        sendOtherAddedToTrack(otherId, trackId, "Ghost");
        
        var doc = trackRepository.findById(trackId).block();
        assertThat(doc).isNull();
    }

    @Test
    @Order(16)
    void artistAddedToTrack_multipleArtists() throws Exception {
        String artist1Id = randomId();
        String artist2Id = randomId();
        String trackId = randomId();

        sendArtistCreated(artist1Id, "Lead Artist", EntityStatus.ACTIVE);
        
        sendArtistCreated(artist2Id, "Guest Artist", EntityStatus.ACTIVE);
        
        sendTrackCreated(trackId, "Multi Artist Track");
        
        sendArtistAddedToTrack(artist1Id, trackId, ArtistRole.MAIN_ARTIST);
        
        sendArtistAddedToTrack(artist2Id, trackId, ArtistRole.FEATURED_ARTIST);
        
        var doc = trackRepository.findById(trackId).block();
        assertThat(doc).isNotNull();
        assertThat(doc.getArtists()).hasSize(2);

        var ids = doc.getArtists().stream()
                .map(TrackDocument.ArtistEmbed::getId).toList();
        assertThat(ids).containsExactlyInAnyOrder(artist1Id, artist2Id);
    }

    @Test
    @Order(17)
    void artistDeletedFromTrack_onlyRemovesSpecificArtist() throws Exception {
        String artist1Id = randomId();
        String artist2Id = randomId();
        String trackId = randomId();

        sendArtistCreated(artist1Id, "Keep Track Artist", EntityStatus.ACTIVE);
        
        sendArtistCreated(artist2Id, "Remove Track Artist 2", EntityStatus.ACTIVE);
        
        sendTrackCreated(trackId, "Selective Remove Track");
        
        sendArtistAddedToTrack(artist1Id, trackId, ArtistRole.MAIN_ARTIST);
        
        sendArtistAddedToTrack(artist2Id, trackId, ArtistRole.FEATURED_ARTIST);
        
        sendArtistDeletedFromTrack(artist2Id, trackId);
        
        var doc = trackRepository.findById(trackId).block();
        assertThat(doc).isNotNull();
        assertThat(doc.getArtists()).hasSize(1);
        assertThat(doc.getArtists().getFirst().getId()).isEqualTo(artist1Id);
    }

    @Test
    @Order(18)
    void otherAddedToTrack_multipleOthers() throws Exception {
        String trackId = randomId();
        String other1Id = randomId();
        String other2Id = randomId();

        sendTrackCreated(trackId, "Multi Other Track");
        
        sendOtherAddedToTrack(other1Id, trackId, "Guest One");
        
        sendOtherAddedToTrack(other2Id, trackId, "Guest Two");
        
        var doc = trackRepository.findById(trackId).block();
        assertThat(doc).isNotNull();
        assertThat(doc.getOthers()).hasSize(2);

        var ids = doc.getOthers().stream()
                .map(TrackDocument.OtherArtistEmbed::getId).toList();
        assertThat(ids).containsExactlyInAnyOrder(other1Id, other2Id);
    }
}
