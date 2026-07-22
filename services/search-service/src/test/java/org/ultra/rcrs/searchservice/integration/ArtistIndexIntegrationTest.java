package org.ultra.rcrs.searchservice.integration;

import org.junit.jupiter.api.Test;
import org.ultra.rcrs.events.album.ArtistAddedToAlbumEventOuterClass;
import org.ultra.rcrs.events.album.ArtistDeletedFromAlbumEventOuterClass;
import org.ultra.rcrs.events.artist.*;
import org.ultra.rcrs.events.common.ArtistRoleOuterClass;
import org.ultra.rcrs.events.common.AvailabilityStatusOuterClass;
import org.ultra.rcrs.events.common.DomainEventOuterClass;
import org.ultra.rcrs.events.track.ArtistAddedToTrackEventOuterClass;
import org.ultra.rcrs.events.track.ArtistDeletedFromTrackEventOuterClass;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ArtistIndexIntegrationTest extends BaseIntegrationTest {

    private void sendArtistCreated(String id, String name, AvailabilityStatusOuterClass.AvailabilityStatus status, List<String> tags) throws Exception {
        ArtistCreatedEventOuterClass.ArtistCreatedEvent event = ArtistCreatedEventOuterClass.ArtistCreatedEvent.newBuilder()
                .setId(id)
                .setName(name)
                .setAvailabilityStatus(status)
                .addAllTags(tags)
                .build();
        sendEvent(DomainEventOuterClass.EventType.ARTIST_CREATED, DomainEventOuterClass.AggregateType.ARTIST, id, event);
    }

    private void sendArtistDeleted(String id) throws Exception {
        ArtistDeletedEventOuterClass.ArtistDeletedEvent event = ArtistDeletedEventOuterClass.ArtistDeletedEvent.newBuilder()
                .setId(id)
                .build();
        sendEvent(DomainEventOuterClass.EventType.ARTIST_DELETED, DomainEventOuterClass.AggregateType.ARTIST, id, event);
    }

    private void sendArtistHidden(String id) throws Exception {
        ArtistHiddenEventOuterClass.ArtistHiddenEvent event = ArtistHiddenEventOuterClass.ArtistHiddenEvent.newBuilder()
                .setId(id)
                .build();
        sendEvent(DomainEventOuterClass.EventType.ARTIST_HIDDEN, DomainEventOuterClass.AggregateType.ARTIST, id, event);
    }

    private void sendArtistActivated(String id) throws Exception {
        ArtistActivatedEventOuterClass.ArtistActivatedEvent event = ArtistActivatedEventOuterClass.ArtistActivatedEvent.newBuilder()
                .setId(id)
                .build();
        sendEvent(DomainEventOuterClass.EventType.ARTIST_ACTIVATED, DomainEventOuterClass.AggregateType.ARTIST, id, event);
    }

    private void sendArtistTrueDeleted(String id) throws Exception {
        ArtistTrueDeletedEventOuterClass.ArtistTrueDeletedEvent event = ArtistTrueDeletedEventOuterClass.ArtistTrueDeletedEvent.newBuilder()
                .setId(id)
                .build();
        sendEvent(DomainEventOuterClass.EventType.ARTIST_TRUE_DELETED, DomainEventOuterClass.AggregateType.ARTIST, id, event);
    }

    @Test
    void artistCreated_activeIndex_bothAdminAndPublic() throws Exception {
        String id = UUID.randomUUID().toString();
        sendArtistCreated(id, "Active Artist", AvailabilityStatusOuterClass.AvailabilityStatus.ACTIVE, List.of("rock", "jazz"));
        waitForProcessing();
        refreshAllIndices();

        assertThat(esDocExists("artists-admin", id)).isTrue();
        assertThat(esDocExists("artists-public", id)).isTrue();

        Map<String, Object> adminDoc = getEsDoc("artists-admin", id);
        assertThat(adminDoc.get("name")).isEqualTo("Active Artist");
        assertThat(adminDoc.get("availability")).isEqualTo("ACTIVE");

        Map<String, Object> publicDoc = getEsDoc("artists-public", id);
        assertThat(publicDoc.get("name")).isEqualTo("Active Artist");
        assertThat(publicDoc.get("availability")).isEqualTo("ACTIVE");
    }

    @Test
    void artistCreated_deletedIndex_adminOnly() throws Exception {
        String id = UUID.randomUUID().toString();
        sendArtistCreated(id, "Deleted Artist", AvailabilityStatusOuterClass.AvailabilityStatus.DELETED, List.of("rock"));
        waitForProcessing();
        refreshAllIndices();

        assertThat(esDocExists("artists-admin", id)).isTrue();
        assertThat(esDocExists("artists-public", id)).isFalse();

        Map<String, Object> adminDoc = getEsDoc("artists-admin", id);
        assertThat(adminDoc.get("availability")).isEqualTo("DELETED");
    }

    @Test
    void artistDeleted_setsAdminDeleted_removesFromPublic() throws Exception {
        String id = UUID.randomUUID().toString();
        sendArtistCreated(id, "Artist To Delete", AvailabilityStatusOuterClass.AvailabilityStatus.ACTIVE, List.of("pop"));
        waitForProcessing();
        refreshAllIndices();

        assertThat(esDocExists("artists-public", id)).isTrue();

        sendArtistDeleted(id);
        waitForProcessing();
        refreshAllIndices();

        assertThat(esDocExists("artists-admin", id)).isTrue();
        Map<String, Object> adminDoc = getEsDoc("artists-admin", id);
        assertThat(adminDoc.get("availability")).isEqualTo("DELETED");

        assertThat(esDocExists("artists-public", id)).isFalse();
    }

    @Test
    void artistHidden_setsBothIndicesToHidden() throws Exception {
        String id = UUID.randomUUID().toString();
        sendArtistCreated(id, "Artist To Hide", AvailabilityStatusOuterClass.AvailabilityStatus.ACTIVE, List.of("rock"));
        waitForProcessing();
        refreshAllIndices();

        sendArtistHidden(id);
        waitForProcessing();
        refreshAllIndices();

        assertThat(esDocExists("artists-admin", id)).isTrue();
        assertThat(esDocExists("artists-public", id)).isTrue();

        Map<String, Object> adminDoc = getEsDoc("artists-admin", id);
        assertThat(adminDoc.get("availability")).isEqualTo("HIDDEN");

        Map<String, Object> publicDoc = getEsDoc("artists-public", id);
        assertThat(publicDoc.get("availability")).isEqualTo("HIDDEN");
    }

    @Test
    void artistActivated_setsBothIndicesToActive() throws Exception {
        String id = UUID.randomUUID().toString();
        sendArtistCreated(id, "Artist To Activate", AvailabilityStatusOuterClass.AvailabilityStatus.DELETED, List.of("jazz"));
        waitForProcessing();
        refreshAllIndices();

        sendArtistActivated(id);
        waitForProcessing();
        refreshAllIndices();

        assertThat(esDocExists("artists-admin", id)).isTrue();
        assertThat(esDocExists("artists-public", id)).isTrue();

        Map<String, Object> adminDoc = getEsDoc("artists-admin", id);
        assertThat(adminDoc.get("availability")).isEqualTo("ACTIVE");

        Map<String, Object> publicDoc = getEsDoc("artists-public", id);
        assertThat(publicDoc.get("availability")).isEqualTo("ACTIVE");
    }

    @Test
    void artistTrueDeleted_removesFromBothIndices() throws Exception {
        String id = UUID.randomUUID().toString();
        sendArtistCreated(id, "Artist True Deleted", AvailabilityStatusOuterClass.AvailabilityStatus.ACTIVE, List.of("rock"));
        waitForProcessing();
        refreshAllIndices();

        assertThat(esDocExists("artists-admin", id)).isTrue();
        assertThat(esDocExists("artists-public", id)).isTrue();

        sendArtistTrueDeleted(id);
        waitForProcessing();
        refreshAllIndices();

        assertThat(esDocExists("artists-admin", id)).isFalse();
        assertThat(esDocExists("artists-public", id)).isFalse();
    }

    @Test
    void artistAddedToAlbum_addsNestedAlbumToArtist() throws Exception {
        String artistId = UUID.randomUUID().toString();
        String albumId = UUID.randomUUID().toString();
        sendArtistCreated(artistId, "Album Artist", AvailabilityStatusOuterClass.AvailabilityStatus.ACTIVE, List.of("rock"));
        waitForProcessing();
        refreshAllIndices();

        indexAlbumAdminDoc(albumId, "Test Album", "2025", "ACTIVE", "PUBLISHED", null, null);
        refreshAllIndices();

        ArtistAddedToAlbumEventOuterClass.ArtistAddedToAlbumEvent event = ArtistAddedToAlbumEventOuterClass.ArtistAddedToAlbumEvent.newBuilder()
                .setArtistId(artistId)
                .setAlbumId(albumId)
                .setRole(ArtistRoleOuterClass.ArtistRole.MAIN_ARTIST)
                .build();
        sendEvent(DomainEventOuterClass.EventType.ARTIST_ADDED_TO_ALBUM, DomainEventOuterClass.AggregateType.ALBUM, albumId, event);
        waitForProcessing();
        refreshAllIndices();

        assertThat(esDocExists("artists-admin", artistId)).isTrue();
        assertThat(esDocExists("artists-public", artistId)).isTrue();

        Map<String, Object> adminDoc = getEsDoc("artists-admin", artistId);
        @SuppressWarnings("unchecked")
        List<Map<String, String>> adminAlbums = (List<Map<String, String>>) adminDoc.get("albums");
        assertThat(adminAlbums).hasSize(1);
        assertThat(adminAlbums.get(0).get("id")).isEqualTo(albumId);
        assertThat(adminAlbums.get(0).get("title")).isEqualTo("Test Album");

        Map<String, Object> publicDoc = getEsDoc("artists-public", artistId);
        @SuppressWarnings("unchecked")
        List<Map<String, String>> publicAlbums = (List<Map<String, String>>) publicDoc.get("albums");
        assertThat(publicAlbums).hasSize(1);
        assertThat(publicAlbums.get(0).get("id")).isEqualTo(albumId);
        assertThat(publicAlbums.get(0).get("title")).isEqualTo("Test Album");
    }

    @Test
    void artistDeletedFromAlbum_removesNestedAlbumFromArtist() throws Exception {
        String artistId = UUID.randomUUID().toString();
        String albumId = UUID.randomUUID().toString();
        sendArtistCreated(artistId, "Album Remove Artist", AvailabilityStatusOuterClass.AvailabilityStatus.ACTIVE, List.of("rock"));
        waitForProcessing();
        refreshAllIndices();

        indexAlbumAdminDoc(albumId, "Test Album", "2025", "ACTIVE", "PUBLISHED", null, null);
        refreshAllIndices();

        ArtistAddedToAlbumEventOuterClass.ArtistAddedToAlbumEvent addEvent = ArtistAddedToAlbumEventOuterClass.ArtistAddedToAlbumEvent.newBuilder()
                .setArtistId(artistId)
                .setAlbumId(albumId)
                .setRole(ArtistRoleOuterClass.ArtistRole.MAIN_ARTIST)
                .build();
        sendEvent(DomainEventOuterClass.EventType.ARTIST_ADDED_TO_ALBUM, DomainEventOuterClass.AggregateType.ALBUM, albumId, addEvent);
        waitForProcessing();
        refreshAllIndices();

        Map<String, Object> adminDoc = getEsDoc("artists-admin", artistId);
        @SuppressWarnings("unchecked")
        List<Map<String, String>> albumsAfterAdd = (List<Map<String, String>>) adminDoc.get("albums");
        assertThat(albumsAfterAdd).hasSize(1);

        ArtistDeletedFromAlbumEventOuterClass.ArtistDeletedFromAlbumEvent removeEvent = ArtistDeletedFromAlbumEventOuterClass.ArtistDeletedFromAlbumEvent.newBuilder()
                .setArtistId(artistId)
                .setAlbumId(albumId)
                .build();
        sendEvent(DomainEventOuterClass.EventType.ARTIST_DELETED_FROM_ALBUM, DomainEventOuterClass.AggregateType.ALBUM, albumId, removeEvent);
        waitForProcessing();
        refreshAllIndices();

        Map<String, Object> adminDocAfter = getEsDoc("artists-admin", artistId);
        @SuppressWarnings("unchecked")
        List<Map<String, String>> adminAlbums = (List<Map<String, String>>) adminDocAfter.get("albums");
        assertThat(adminAlbums).isEmpty();

        Map<String, Object> publicDocAfter = getEsDoc("artists-public", artistId);
        @SuppressWarnings("unchecked")
        List<Map<String, String>> publicAlbums = (List<Map<String, String>>) publicDocAfter.get("albums");
        assertThat(publicAlbums).isEmpty();
    }

    @Test
    void artistAddedToTrack_addsNestedTrackToArtist() throws Exception {
        String artistId = UUID.randomUUID().toString();
        String trackId = UUID.randomUUID().toString();
        sendArtistCreated(artistId, "Track Artist", AvailabilityStatusOuterClass.AvailabilityStatus.ACTIVE, List.of("jazz"));
        waitForProcessing();
        refreshAllIndices();

        indexTrackAdminDoc(trackId, "Test Track", "ACTIVE", "PUBLISHED", null, null);
        refreshAllIndices();

        ArtistAddedToTrackEventOuterClass.ArtistAddedToTrackEvent event = ArtistAddedToTrackEventOuterClass.ArtistAddedToTrackEvent.newBuilder()
                .setArtistId(artistId)
                .setTrackId(trackId)
                .setRole(ArtistRoleOuterClass.ArtistRole.MAIN_ARTIST)
                .build();
        sendEvent(DomainEventOuterClass.EventType.ARTIST_ADDED_TO_TRACK, DomainEventOuterClass.AggregateType.TRACK, trackId, event);
        waitForProcessing();
        refreshAllIndices();

        assertThat(esDocExists("artists-admin", artistId)).isTrue();
        assertThat(esDocExists("artists-public", artistId)).isTrue();

        Map<String, Object> adminDoc = getEsDoc("artists-admin", artistId);
        @SuppressWarnings("unchecked")
        List<Map<String, String>> adminTracks = (List<Map<String, String>>) adminDoc.get("tracks");
        assertThat(adminTracks).hasSize(1);
        assertThat(adminTracks.get(0).get("id")).isEqualTo(trackId);
        assertThat(adminTracks.get(0).get("title")).isEqualTo("Test Track");
    }

    @Test
    void artistDeletedFromTrack_removesNestedTrackFromArtist() throws Exception {
        String artistId = UUID.randomUUID().toString();
        String trackId = UUID.randomUUID().toString();
        sendArtistCreated(artistId, "Track Remove Artist", AvailabilityStatusOuterClass.AvailabilityStatus.ACTIVE, List.of("jazz"));
        waitForProcessing();
        refreshAllIndices();

        indexTrackAdminDoc(trackId, "Test Track", "ACTIVE", "PUBLISHED", null, null);
        refreshAllIndices();

        ArtistAddedToTrackEventOuterClass.ArtistAddedToTrackEvent addEvent = ArtistAddedToTrackEventOuterClass.ArtistAddedToTrackEvent.newBuilder()
                .setArtistId(artistId)
                .setTrackId(trackId)
                .setRole(ArtistRoleOuterClass.ArtistRole.MAIN_ARTIST)
                .build();
        sendEvent(DomainEventOuterClass.EventType.ARTIST_ADDED_TO_TRACK, DomainEventOuterClass.AggregateType.TRACK, trackId, addEvent);
        waitForProcessing();
        refreshAllIndices();

        Map<String, Object> adminDoc = getEsDoc("artists-admin", artistId);
        @SuppressWarnings("unchecked")
        List<Map<String, String>> tracksAfterAdd = (List<Map<String, String>>) adminDoc.get("tracks");
        assertThat(tracksAfterAdd).hasSize(1);

        ArtistDeletedFromTrackEventOuterClass.ArtistDeletedFromTrackEvent removeEvent = ArtistDeletedFromTrackEventOuterClass.ArtistDeletedFromTrackEvent.newBuilder()
                .setArtistId(artistId)
                .setTrackId(trackId)
                .build();
        sendEvent(DomainEventOuterClass.EventType.ARTIST_DELETED_FROM_TRACK, DomainEventOuterClass.AggregateType.TRACK, trackId, removeEvent);
        waitForProcessing();
        refreshAllIndices();

        Map<String, Object> adminDocAfter = getEsDoc("artists-admin", artistId);
        @SuppressWarnings("unchecked")
        List<Map<String, String>> adminTracks = (List<Map<String, String>>) adminDocAfter.get("tracks");
        assertThat(adminTracks).isEmpty();

        Map<String, Object> publicDocAfter = getEsDoc("artists-public", artistId);
        @SuppressWarnings("unchecked")
        List<Map<String, String>> publicTracks = (List<Map<String, String>>) publicDocAfter.get("tracks");
        assertThat(publicTracks).isEmpty();
    }
}
