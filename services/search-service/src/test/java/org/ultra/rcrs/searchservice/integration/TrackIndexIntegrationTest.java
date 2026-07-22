package org.ultra.rcrs.searchservice.integration;

import org.junit.jupiter.api.Test;
import org.ultra.rcrs.enums.ArtistRole;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.enums.LifecycleStatus;
import org.ultra.rcrs.events.common.ArtistRoleOuterClass;
import org.ultra.rcrs.events.common.AvailabilityStatusOuterClass;
import org.ultra.rcrs.events.common.DomainEventOuterClass;
import org.ultra.rcrs.events.common.LifecycleStatusOuterClass;
import org.ultra.rcrs.events.track.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TrackIndexIntegrationTest extends BaseIntegrationTest {

    // ── helpers ──────────────────────────────────────────────────────────

    private void sendTrackCreated(String id, String title) throws Exception {
        var event = TrackCreatedEventOuterClass.TrackCreatedEvent.newBuilder()
                .setId(id)
                .setTitle(title)
                .setTrackNumber(1)
                .setExplicit(false)
                .setAvailabilityStatus(AvailabilityStatusOuterClass.AvailabilityStatus.ACTIVE)
                .setLifecycleStatus(LifecycleStatusOuterClass.LifecycleStatus.CREATED)
                .build();
        sendEvent(DomainEventOuterClass.EventType.TRACK_CREATED,
                DomainEventOuterClass.AggregateType.TRACK, id, event);
    }

    private void sendTrackDeleted(String id) throws Exception {
        var event = TrackDeletedEventOuterClass.TrackDeletedEvent.newBuilder()
                .setId(id).build();
        sendEvent(DomainEventOuterClass.EventType.TRACK_DELETED,
                DomainEventOuterClass.AggregateType.TRACK, id, event);
    }

    private void sendTrackHidden(String id) throws Exception {
        var event = TrackHiddenEventOuterClass.TrackHiddenEvent.newBuilder()
                .setId(id).build();
        sendEvent(DomainEventOuterClass.EventType.TRACK_HIDDEN,
                DomainEventOuterClass.AggregateType.TRACK, id, event);
    }

    private void sendTrackActivated(String id) throws Exception {
        var event = TrackActivatedEventOuterClass.TrackActivatedEvent.newBuilder()
                .setId(id).build();
        sendEvent(DomainEventOuterClass.EventType.TRACK_ACTIVATED,
                DomainEventOuterClass.AggregateType.TRACK, id, event);
    }

    private void sendTrackTrueDeleted(String id) throws Exception {
        var event = TrackTrueDeletedEventOuterClass.TrackTrueDeletedEvent.newBuilder()
                .setId(id).build();
        sendEvent(DomainEventOuterClass.EventType.TRACK_TRUE_DELETED,
                DomainEventOuterClass.AggregateType.TRACK, id, event);
    }

    private void sendTrackLifecycleStatusUpdated(String id, LifecycleStatus status) throws Exception {
        var event = TrackUpdateLifecycleStatusEventOuterClass.TrackUpdateLifecycleStatusEvent.newBuilder()
                .setId(id)
                .setLifecycleStatus(LifecycleStatusOuterClass.LifecycleStatus.valueOf(status.name()))
                .build();
        sendEvent(DomainEventOuterClass.EventType.TRACK_LIFECYCLE_STATUS_UPDATED,
                DomainEventOuterClass.AggregateType.TRACK, id, event);
    }

    private void sendTrackAddedToAlbum(String trackId, String albumId) throws Exception {
        var event = TrackAddedToAlbumEventOuterClass.TrackAddedToAlbumEvent.newBuilder()
                .setTrackId(trackId)
                .setAlbumId(albumId)
                .build();
        sendEvent(DomainEventOuterClass.EventType.TRACK_ADDED_TO_ALBUM,
                DomainEventOuterClass.AggregateType.TRACK, trackId, event);
    }

    private void sendArtistAddedToTrack(String artistId, String trackId, ArtistRole role) throws Exception {
        var event = ArtistAddedToTrackEventOuterClass.ArtistAddedToTrackEvent.newBuilder()
                .setArtistId(artistId)
                .setTrackId(trackId)
                .setRole(ArtistRoleOuterClass.ArtistRole.valueOf(role.name()))
                .build();
        sendEvent(DomainEventOuterClass.EventType.ARTIST_ADDED_TO_TRACK,
                DomainEventOuterClass.AggregateType.TRACK, trackId, event);
    }

    private void sendArtistDeletedFromTrack(String artistId, String trackId) throws Exception {
        var event = ArtistDeletedFromTrackEventOuterClass.ArtistDeletedFromTrackEvent.newBuilder()
                .setArtistId(artistId)
                .setTrackId(trackId)
                .build();
        sendEvent(DomainEventOuterClass.EventType.ARTIST_DELETED_FROM_TRACK,
                DomainEventOuterClass.AggregateType.TRACK, trackId, event);
    }

    private void sendOtherAddedToTrack(String otherId, String trackId, String name) throws Exception {
        var event = OtherAddedToTrackEventOuterClass.OtherAddedToTrackEvent.newBuilder()
                .setOtherId(otherId)
                .setTrackId(trackId)
                .setName(name)
                .build();
        sendEvent(DomainEventOuterClass.EventType.OTHER_ADDED_TO_TRACK,
                DomainEventOuterClass.AggregateType.TRACK, trackId, event);
    }

    private void sendOtherDeletedFromTrack(String otherId, String trackId) throws Exception {
        var event = OtherDeletedFromTrackEventOuterClass.OtherDeletedFromTrackEvent.newBuilder()
                .setOtherId(otherId)
                .setTrackId(trackId)
                .build();
        sendEvent(DomainEventOuterClass.EventType.OTHER_DELETED_FROM_TRACK,
                DomainEventOuterClass.AggregateType.TRACK, trackId, event);
    }

    // ── tests ────────────────────────────────────────────────────────────

    @Test
    void trackCreated_createsAdminDocOnly() throws Exception {
        String trackId = UUID.randomUUID().toString();
        sendTrackCreated(trackId, "New Track");
        waitForProcessing();
        refreshAllIndices();

        assertThat(esDocExists("tracks-admin", trackId)).isTrue();
        assertThat(esDocExists("tracks-public", trackId)).isFalse();

        Map<String, Object> doc = getEsDoc("tracks-admin", trackId);
        assertThat(doc.get("title")).isEqualTo("New Track");
        assertThat(doc.get("availability")).isEqualTo(EntityStatus.ACTIVE.name());
        assertThat(doc.get("lifecycleStatus")).isEqualTo(LifecycleStatus.CREATED.name());
    }

    @Test
    void trackLifecycleStatusUpdated_publishesToPublic() throws Exception {
        String trackId = UUID.randomUUID().toString();
        sendTrackCreated(trackId, "Publish Me");
        waitForProcessing();
        refreshAllIndices();

        assertThat(esDocExists("tracks-public", trackId)).isFalse();

        sendTrackLifecycleStatusUpdated(trackId, LifecycleStatus.PUBLISHED);
        waitForProcessing();
        refreshAllIndices();

        assertThat(esDocExists("tracks-admin", trackId)).isTrue();
        assertThat(esDocExists("tracks-public", trackId)).isTrue();

        Map<String, Object> publicDoc = getEsDoc("tracks-public", trackId);
        assertThat(publicDoc.get("title")).isEqualTo("Publish Me");
    }

    @Test
    void trackDeleted_setsAdminDeleted_removesFromPublic() throws Exception {
        String trackId = UUID.randomUUID().toString();
        sendTrackCreated(trackId, "Delete Me");
        sendTrackLifecycleStatusUpdated(trackId, LifecycleStatus.PUBLISHED);
        waitForProcessing();
        refreshAllIndices();

        assertThat(esDocExists("tracks-public", trackId)).isTrue();

        sendTrackDeleted(trackId);
        waitForProcessing();
        refreshAllIndices();

        assertThat(esDocExists("tracks-public", trackId)).isFalse();

        Map<String, Object> adminDoc = getEsDoc("tracks-admin", trackId);
        assertThat(adminDoc.get("availability")).isEqualTo(EntityStatus.DELETED.name());
    }

    @Test
    void trackHidden_setsBothIndicesToHidden() throws Exception {
        String trackId = UUID.randomUUID().toString();
        sendTrackCreated(trackId, "Hide Me");
        sendTrackLifecycleStatusUpdated(trackId, LifecycleStatus.PUBLISHED);
        waitForProcessing();
        refreshAllIndices();

        sendTrackHidden(trackId);
        waitForProcessing();
        refreshAllIndices();

        Map<String, Object> adminDoc = getEsDoc("tracks-admin", trackId);
        assertThat(adminDoc.get("availability")).isEqualTo(EntityStatus.HIDDEN.name());

        Map<String, Object> publicDoc = getEsDoc("tracks-public", trackId);
        assertThat(publicDoc.get("availability")).isEqualTo(EntityStatus.HIDDEN.name());
    }

    @Test
    void trackActivated_setsBothIndicesToActive() throws Exception {
        String trackId = UUID.randomUUID().toString();
        sendTrackCreated(trackId, "Activate Me");
        sendTrackLifecycleStatusUpdated(trackId, LifecycleStatus.PUBLISHED);
        waitForProcessing();
        refreshAllIndices();

        sendTrackHidden(trackId);
        waitForProcessing();
        refreshAllIndices();

        sendTrackActivated(trackId);
        waitForProcessing();
        refreshAllIndices();

        Map<String, Object> adminDoc = getEsDoc("tracks-admin", trackId);
        assertThat(adminDoc.get("availability")).isEqualTo(EntityStatus.ACTIVE.name());

        Map<String, Object> publicDoc = getEsDoc("tracks-public", trackId);
        assertThat(publicDoc.get("availability")).isEqualTo(EntityStatus.ACTIVE.name());
    }

    @Test
    void trackTrueDeleted_removesFromBothIndices() throws Exception {
        String trackId = UUID.randomUUID().toString();
        sendTrackCreated(trackId, "True Delete Me");
        sendTrackLifecycleStatusUpdated(trackId, LifecycleStatus.PUBLISHED);
        waitForProcessing();
        refreshAllIndices();

        assertThat(esDocExists("tracks-admin", trackId)).isTrue();
        assertThat(esDocExists("tracks-public", trackId)).isTrue();

        sendTrackTrueDeleted(trackId);
        waitForProcessing();
        refreshAllIndices();

        assertThat(esDocExists("tracks-admin", trackId)).isFalse();
        assertThat(esDocExists("tracks-public", trackId)).isFalse();
    }

    @Test
    void trackAddedToAlbum_setsAlbumOnTrack() throws Exception {
        String trackId = UUID.randomUUID().toString();
        String albumId = UUID.randomUUID().toString();

        sendTrackCreated(trackId, "Album Track");
        waitForProcessing();
        refreshAllIndices();

        indexAlbumAdminDoc(albumId, "Test Album", "2025", "ACTIVE", "PUBLISHED", null, null);
        refreshAllIndices();

        sendTrackAddedToAlbum(trackId, albumId);
        waitForProcessing();
        refreshAllIndices();

        Map<String, Object> trackDoc = getEsDoc("tracks-admin", trackId);
        assertThat(trackDoc.get("album")).isNotNull();
        @SuppressWarnings("unchecked")
        Map<String, String> album = (Map<String, String>) trackDoc.get("album");
        assertThat(album.get("id")).isEqualTo(albumId);
        assertThat(album.get("title")).isEqualTo("Test Album");

        Map<String, Object> albumDoc = getEsDoc("albums-admin", albumId);
        @SuppressWarnings("unchecked")
        List<Map<String, String>> tracks = (List<Map<String, String>>) albumDoc.get("tracks");
        assertThat(tracks).isNotNull();
        assertThat(tracks).hasSize(1);
        assertThat(tracks.get(0).get("id")).isEqualTo(trackId);
        assertThat(tracks.get(0).get("title")).isEqualTo("Album Track");
    }

    @Test
    void artistAddedToTrack_addsNestedArtistToTrack() throws Exception {
        String trackId = UUID.randomUUID().toString();
        String artistId = UUID.randomUUID().toString();

        sendTrackCreated(trackId, "Artist Track");
        waitForProcessing();
        refreshAllIndices();

        indexArtistAdminDoc(artistId, "Test Artist", List.of("rock"), "ACTIVE", "PUBLISHED", null, null);
        refreshAllIndices();

        sendArtistAddedToTrack(artistId, trackId, ArtistRole.MAIN_ARTIST);
        waitForProcessing();
        refreshAllIndices();

        Map<String, Object> trackDoc = getEsDoc("tracks-admin", trackId);
        @SuppressWarnings("unchecked")
        List<Map<String, String>> artists = (List<Map<String, String>>) trackDoc.get("artists");
        assertThat(artists).isNotNull();
        assertThat(artists).hasSize(1);
        assertThat(artists.get(0).get("id")).isEqualTo(artistId);
        assertThat(artists.get(0).get("name")).isEqualTo("Test Artist");

        Map<String, Object> artistDoc = getEsDoc("artists-admin", artistId);
        @SuppressWarnings("unchecked")
        List<Map<String, String>> tracks = (List<Map<String, String>>) artistDoc.get("tracks");
        assertThat(tracks).isNotNull();
        assertThat(tracks).hasSize(1);
        assertThat(tracks.get(0).get("id")).isEqualTo(trackId);
        assertThat(tracks.get(0).get("title")).isEqualTo("Artist Track");
    }

    @Test
    void artistDeletedFromTrack_removesNestedArtistFromTrack() throws Exception {
        String trackId = UUID.randomUUID().toString();
        String artistId = UUID.randomUUID().toString();

        sendTrackCreated(trackId, "Remove Artist Track");
        waitForProcessing();
        refreshAllIndices();

        indexArtistAdminDoc(artistId, "Remove Artist", List.of("rock"), "ACTIVE", "PUBLISHED", null, null);
        refreshAllIndices();

        sendArtistAddedToTrack(artistId, trackId, ArtistRole.MAIN_ARTIST);
        waitForProcessing();
        refreshAllIndices();

        sendArtistDeletedFromTrack(artistId, trackId);
        waitForProcessing();
        refreshAllIndices();

        Map<String, Object> trackDoc = getEsDoc("tracks-admin", trackId);
        @SuppressWarnings("unchecked")
        List<Map<String, String>> artists = (List<Map<String, String>>) trackDoc.get("artists");
        assertThat(artists).isEmpty();

        Map<String, Object> artistDoc = getEsDoc("artists-admin", artistId);
        @SuppressWarnings("unchecked")
        List<Map<String, String>> tracks = (List<Map<String, String>>) artistDoc.get("tracks");
        assertThat(tracks).isEmpty();
    }

    @Test
    void otherAddedToTrack_addsToTrackArtistsList() throws Exception {
        String trackId = UUID.randomUUID().toString();
        String otherId = UUID.randomUUID().toString();

        sendTrackCreated(trackId, "Other Track");
        waitForProcessing();
        refreshAllIndices();

        sendOtherAddedToTrack(otherId, trackId, "Producer Person");
        waitForProcessing();
        refreshAllIndices();

        Map<String, Object> trackDoc = getEsDoc("tracks-admin", trackId);
        @SuppressWarnings("unchecked")
        List<Map<String, String>> artists = (List<Map<String, String>>) trackDoc.get("artists");
        assertThat(artists).isNotNull();
        assertThat(artists).hasSize(1);
        assertThat(artists.get(0).get("id")).isEqualTo(otherId);
        assertThat(artists.get(0).get("name")).isEqualTo("Producer Person");
    }

    @Test
    void otherDeletedFromTrack_removesFromTrackArtistsList() throws Exception {
        String trackId = UUID.randomUUID().toString();
        String otherId = UUID.randomUUID().toString();

        sendTrackCreated(trackId, "Remove Other Track");
        waitForProcessing();
        refreshAllIndices();

        sendOtherAddedToTrack(otherId, trackId, "Remove Me");
        waitForProcessing();
        refreshAllIndices();

        sendOtherDeletedFromTrack(otherId, trackId);
        waitForProcessing();
        refreshAllIndices();

        Map<String, Object> trackDoc = getEsDoc("tracks-admin", trackId);
        @SuppressWarnings("unchecked")
        List<Map<String, String>> artists = (List<Map<String, String>>) trackDoc.get("artists");
        assertThat(artists).isEmpty();
    }

    @Test
    void trackDeleted_doesNotRemoveFromAlbumIndex() throws Exception {
        String trackId = UUID.randomUUID().toString();
        String albumId = UUID.randomUUID().toString();

        sendTrackCreated(trackId, "Sticky Track");
        indexAlbumAdminDoc(albumId, "Sticky Album", "2025", "ACTIVE", "PUBLISHED", null, null);
        refreshAllIndices();

        sendTrackAddedToAlbum(trackId, albumId);
        waitForProcessing();
        refreshAllIndices();

        sendTrackDeleted(trackId);
        waitForProcessing();
        refreshAllIndices();

        assertThat(esDocExists("tracks-admin", trackId)).isTrue();

        Map<String, Object> albumDoc = getEsDoc("albums-admin", albumId);
        @SuppressWarnings("unchecked")
        List<Map<String, String>> tracks = (List<Map<String, String>>) albumDoc.get("tracks");
        assertThat(tracks).isNotNull();
        assertThat(tracks).hasSize(1);
        assertThat(tracks.get(0).get("id")).isEqualTo(trackId);
    }

    @Test
    void trackDeleted_doesNotRemoveFromArtistIndex() throws Exception {
        String trackId = UUID.randomUUID().toString();
        String artistId = UUID.randomUUID().toString();

        sendTrackCreated(trackId, "Sticky Artist Track");
        indexArtistAdminDoc(artistId, "Sticky Artist", List.of("rock"), "ACTIVE", "PUBLISHED", null, null);
        refreshAllIndices();

        sendArtistAddedToTrack(artistId, trackId, ArtistRole.MAIN_ARTIST);
        waitForProcessing();
        refreshAllIndices();

        sendTrackDeleted(trackId);
        waitForProcessing();
        refreshAllIndices();

        assertThat(esDocExists("tracks-admin", trackId)).isTrue();

        Map<String, Object> artistDoc = getEsDoc("artists-admin", artistId);
        @SuppressWarnings("unchecked")
        List<Map<String, String>> tracks = (List<Map<String, String>>) artistDoc.get("tracks");
        assertThat(tracks).isNotNull();
        assertThat(tracks).hasSize(1);
        assertThat(tracks.get(0).get("id")).isEqualTo(trackId);
    }
}
