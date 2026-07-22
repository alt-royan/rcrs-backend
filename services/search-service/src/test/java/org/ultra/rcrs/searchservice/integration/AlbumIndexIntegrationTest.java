package org.ultra.rcrs.searchservice.integration;

import com.google.protobuf.Timestamp;
import org.junit.jupiter.api.Test;
import org.ultra.rcrs.enums.ArtistRole;
import org.ultra.rcrs.enums.LifecycleStatus;
import org.ultra.rcrs.events.album.*;
import org.ultra.rcrs.events.common.*;
import org.ultra.rcrs.events.track.TrackAddedToAlbumEventOuterClass;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AlbumIndexIntegrationTest extends BaseIntegrationTest {

    private void sendAlbumCreated(String id, String title) throws Exception {
        Timestamp now = Timestamp.newBuilder()
                .setSeconds(System.currentTimeMillis() / 1000)
                .build();
        AlbumCreatedEventOuterClass.AlbumCreatedEvent payload = AlbumCreatedEventOuterClass.AlbumCreatedEvent.newBuilder()
                .setId(id)
                .setTitle(title)
                .setType(AlbumTypeOuterClass.AlbumType.FULL)
                .setCoverS3Key("covers/" + id)
                .setAvailabilityStatus(AvailabilityStatusOuterClass.AvailabilityStatus.ACTIVE)
                .setLifecycleStatus(LifecycleStatusOuterClass.LifecycleStatus.CREATED)
                .setReleaseDate(now)
                .build();
        sendEvent(DomainEventOuterClass.EventType.ALBUM_CREATED,
                DomainEventOuterClass.AggregateType.ALBUM, id, payload);
    }

    private void sendAlbumDeleted(String id) throws Exception {
        AlbumDeletedEventOuterClass.AlbumDeletedEvent payload = AlbumDeletedEventOuterClass.AlbumDeletedEvent.newBuilder()
                .setId(id)
                .build();
        sendEvent(DomainEventOuterClass.EventType.ALBUM_DELETED,
                DomainEventOuterClass.AggregateType.ALBUM, id, payload);
    }

    private void sendAlbumHidden(String id) throws Exception {
        AlbumHiddenEventOuterClass.AlbumHiddenEvent payload = AlbumHiddenEventOuterClass.AlbumHiddenEvent.newBuilder()
                .setId(id)
                .build();
        sendEvent(DomainEventOuterClass.EventType.ALBUM_HIDDEN,
                DomainEventOuterClass.AggregateType.ALBUM, id, payload);
    }

    private void sendAlbumActivated(String id) throws Exception {
        AlbumActivatedEventOuterClass.AlbumActivatedEvent payload = AlbumActivatedEventOuterClass.AlbumActivatedEvent.newBuilder()
                .setId(id)
                .build();
        sendEvent(DomainEventOuterClass.EventType.ALBUM_ACTIVATED,
                DomainEventOuterClass.AggregateType.ALBUM, id, payload);
    }

    private void sendAlbumTrueDeleted(String id) throws Exception {
        AlbumTrueDeletedEventOuterClass.AlbumTrueDeletedEvent payload = AlbumTrueDeletedEventOuterClass.AlbumTrueDeletedEvent.newBuilder()
                .setId(id)
                .build();
        sendEvent(DomainEventOuterClass.EventType.ALBUM_TRUE_DELETED,
                DomainEventOuterClass.AggregateType.ALBUM, id, payload);
    }

    private void sendAlbumLifecycleStatusUpdated(String id, LifecycleStatus status) throws Exception {
        AlbumUpdateLifecycleStatusEventOuterClass.AlbumUpdateLifecycleStatusEvent payload =
                AlbumUpdateLifecycleStatusEventOuterClass.AlbumUpdateLifecycleStatusEvent.newBuilder()
                        .setId(id)
                        .setLifecycleStatus(LifecycleStatusOuterClass.LifecycleStatus.valueOf(status.name()))
                        .build();
        sendEvent(DomainEventOuterClass.EventType.ALBUM_LIFECYCLE_STATUS_UPDATED,
                DomainEventOuterClass.AggregateType.ALBUM, id, payload);
    }

    private void sendArtistAddedToAlbum(String artistId, String albumId, ArtistRole role) throws Exception {
        ArtistAddedToAlbumEventOuterClass.ArtistAddedToAlbumEvent payload =
                ArtistAddedToAlbumEventOuterClass.ArtistAddedToAlbumEvent.newBuilder()
                        .setArtistId(artistId)
                        .setAlbumId(albumId)
                        .setRole(ArtistRoleOuterClass.ArtistRole.valueOf(role.name()))
                        .build();
        sendEvent(DomainEventOuterClass.EventType.ARTIST_ADDED_TO_ALBUM,
                DomainEventOuterClass.AggregateType.ALBUM, albumId, payload);
    }

    private void sendArtistDeletedFromAlbum(String artistId, String albumId) throws Exception {
        ArtistDeletedFromAlbumEventOuterClass.ArtistDeletedFromAlbumEvent payload =
                ArtistDeletedFromAlbumEventOuterClass.ArtistDeletedFromAlbumEvent.newBuilder()
                        .setArtistId(artistId)
                        .setAlbumId(albumId)
                        .build();
        sendEvent(DomainEventOuterClass.EventType.ARTIST_DELETED_FROM_ALBUM,
                DomainEventOuterClass.AggregateType.ALBUM, albumId, payload);
    }

    @Test
    void albumCreated_createsAdminDocOnly() throws Exception {
        String albumId = UUID.randomUUID().toString();
        sendAlbumCreated(albumId, "New Album");
        waitForProcessing();
        refreshAllIndices();

        assertThat(esDocExists("albums-admin", albumId)).isTrue();
        assertThat(esDocExists("albums-public", albumId)).isFalse();

        Map<String, Object> admin = getEsDoc("albums-admin", albumId);
        assertThat(admin.get("title")).isEqualTo("New Album");
        assertThat(admin.get("lifecycleStatus")).isEqualTo("CREATED");
    }

    @Test
    void albumLifecycleStatusUpdated_publishesToPublic() throws Exception {
        String albumId = UUID.randomUUID().toString();
        sendAlbumCreated(albumId, "Lifecycle Album");
        waitForProcessing();
        refreshAllIndices();

        assertThat(esDocExists("albums-public", albumId)).isFalse();

        sendAlbumLifecycleStatusUpdated(albumId, LifecycleStatus.PUBLISHED);
        waitForProcessing();
        refreshAllIndices();

        assertThat(esDocExists("albums-admin", albumId)).isTrue();
        assertThat(esDocExists("albums-public", albumId)).isTrue();

        Map<String, Object> admin = getEsDoc("albums-admin", albumId);
        assertThat(admin.get("lifecycleStatus")).isEqualTo("PUBLISHED");
    }

    @Test
    void albumDeleted_setsAdminDeleted_removesFromPublic() throws Exception {
        String albumId = UUID.randomUUID().toString();
        sendAlbumCreated(albumId, "Delete Album");
        sendAlbumLifecycleStatusUpdated(albumId, LifecycleStatus.PUBLISHED);
        waitForProcessing();
        refreshAllIndices();

        assertThat(esDocExists("albums-public", albumId)).isTrue();

        sendAlbumDeleted(albumId);
        waitForProcessing();
        refreshAllIndices();

        assertThat(esDocExists("albums-admin", albumId)).isTrue();
        assertThat(esDocExists("albums-public", albumId)).isFalse();

        Map<String, Object> admin = getEsDoc("albums-admin", albumId);
        assertThat(admin.get("availability")).isEqualTo("DELETED");
    }

    @Test
    void albumHidden_setsBothIndicesToHidden() throws Exception {
        String albumId = UUID.randomUUID().toString();
        sendAlbumCreated(albumId, "Hidden Album");
        sendAlbumLifecycleStatusUpdated(albumId, LifecycleStatus.PUBLISHED);
        waitForProcessing();
        refreshAllIndices();

        sendAlbumHidden(albumId);
        waitForProcessing();
        refreshAllIndices();

        assertThat(esDocExists("albums-admin", albumId)).isTrue();
        assertThat(esDocExists("albums-public", albumId)).isTrue();

        Map<String, Object> admin = getEsDoc("albums-admin", albumId);
        assertThat(admin.get("availability")).isEqualTo("HIDDEN");

        Map<String, Object> pub = getEsDoc("albums-public", albumId);
        assertThat(pub.get("availability")).isEqualTo("HIDDEN");
    }

    @Test
    void albumActivated_setsBothIndicesToActive() throws Exception {
        String albumId = UUID.randomUUID().toString();
        sendAlbumCreated(albumId, "Activatable Album");
        sendAlbumLifecycleStatusUpdated(albumId, LifecycleStatus.PUBLISHED);
        waitForProcessing();
        refreshAllIndices();

        sendAlbumHidden(albumId);
        waitForProcessing();
        refreshAllIndices();

        Map<String, Object> admin = getEsDoc("albums-admin", albumId);
        assertThat(admin.get("availability")).isEqualTo("HIDDEN");

        sendAlbumActivated(albumId);
        waitForProcessing();
        refreshAllIndices();

        Map<String, Object> adminAfter = getEsDoc("albums-admin", albumId);
        assertThat(adminAfter.get("availability")).isEqualTo("ACTIVE");

        Map<String, Object> pub = getEsDoc("albums-public", albumId);
        assertThat(pub.get("availability")).isEqualTo("ACTIVE");
    }

    @Test
    void albumTrueDeleted_removesFromBothIndices() throws Exception {
        String albumId = UUID.randomUUID().toString();
        sendAlbumCreated(albumId, "True Delete Album");
        waitForProcessing();
        refreshAllIndices();

        assertThat(esDocExists("albums-admin", albumId)).isTrue();

        sendAlbumTrueDeleted(albumId);
        waitForProcessing();
        refreshAllIndices();

        assertThat(esDocExists("albums-admin", albumId)).isFalse();
        assertThat(esDocExists("albums-public", albumId)).isFalse();
    }

    @Test
    void artistAddedToAlbum_addsNestedArtistToAlbum() throws Exception {
        String albumId = UUID.randomUUID().toString();
        String artistId = UUID.randomUUID().toString();

        sendAlbumCreated(albumId, "Artist Album");
        waitForProcessing();
        refreshAllIndices();

        indexArtistAdminDoc(artistId, "Featured Artist", List.of("rock"), "ACTIVE", "PUBLISHED",
                null, null);
        refreshAllIndices();

        sendArtistAddedToAlbum(artistId, albumId, ArtistRole.MAIN_ARTIST);
        waitForProcessing();
        refreshAllIndices();

        Map<String, Object> albumAdmin = getEsDoc("albums-admin", albumId);
        @SuppressWarnings("unchecked")
        List<Map<String, String>> artists = (List<Map<String, String>>) albumAdmin.get("artists");
        assertThat(artists).hasSize(1);
        assertThat(artists.get(0).get("id")).isEqualTo(artistId);
        assertThat(artists.get(0).get("name")).isEqualTo("Featured Artist");

        Map<String, Object> artistAdmin = getEsDoc("artists-admin", artistId);
        @SuppressWarnings("unchecked")
        List<Map<String, String>> albums = (List<Map<String, String>>) artistAdmin.get("albums");
        assertThat(albums).hasSize(1);
        assertThat(albums.get(0).get("id")).isEqualTo(albumId);
        assertThat(albums.get(0).get("title")).isEqualTo("Artist Album");
    }

    @Test
    void artistDeletedFromAlbum_removesNestedArtistFromAlbum() throws Exception {
        String albumId = UUID.randomUUID().toString();
        String artistId = UUID.randomUUID().toString();

        sendAlbumCreated(albumId, "Removal Album");
        indexArtistAdminDoc(artistId, "Removable Artist", List.of("jazz"), "ACTIVE", "PUBLISHED",
                null, null);
        waitForProcessing();
        refreshAllIndices();

        sendArtistAddedToAlbum(artistId, albumId, ArtistRole.FEATURED_ARTIST);
        waitForProcessing();
        refreshAllIndices();

        Map<String, Object> albumAdmin = getEsDoc("albums-admin", albumId);
        @SuppressWarnings("unchecked")
        List<Map<String, String>> artistsBefore = (List<Map<String, String>>) albumAdmin.get("artists");
        assertThat(artistsBefore).hasSize(1);

        sendArtistDeletedFromAlbum(artistId, albumId);
        waitForProcessing();
        refreshAllIndices();

        Map<String, Object> albumAfter = getEsDoc("albums-admin", albumId);
        @SuppressWarnings("unchecked")
        List<Map<String, String>> artistsAfter = (List<Map<String, String>>) albumAfter.get("artists");
        assertThat(artistsAfter).isEmpty();

        Map<String, Object> artistAfter = getEsDoc("artists-admin", artistId);
        @SuppressWarnings("unchecked")
        List<Map<String, String>> albumsAfter = (List<Map<String, String>>) artistAfter.get("albums");
        assertThat(albumsAfter).isEmpty();
    }

    @Test
    void albumDeleted_doesNotRemoveTrackFromAlbumIndex() throws Exception {
        String albumId = UUID.randomUUID().toString();
        String trackId = UUID.randomUUID().toString();

        sendAlbumCreated(albumId, "Track Album");
        indexTrackAdminDoc(trackId, "Persistent Track", "ACTIVE", "PUBLISHED", null, null);
        waitForProcessing();
        refreshAllIndices();

        TrackAddedToAlbumEventOuterClass.TrackAddedToAlbumEvent trackPayload =
                TrackAddedToAlbumEventOuterClass.TrackAddedToAlbumEvent.newBuilder()
                        .setTrackId(trackId)
                        .setAlbumId(albumId)
                        .build();
        sendEvent(DomainEventOuterClass.EventType.TRACK_ADDED_TO_ALBUM,
                DomainEventOuterClass.AggregateType.TRACK, trackId, trackPayload);
        waitForProcessing();
        refreshAllIndices();

        Map<String, Object> albumBeforeDelete = getEsDoc("albums-admin", albumId);
        @SuppressWarnings("unchecked")
        List<Map<String, String>> tracksBefore = (List<Map<String, String>>) albumBeforeDelete.get("tracks");
        assertThat(tracksBefore).hasSize(1);
        assertThat(tracksBefore.get(0).get("id")).isEqualTo(trackId);

        sendAlbumDeleted(albumId);
        waitForProcessing();
        refreshAllIndices();

        assertThat(esDocExists("albums-admin", albumId)).isTrue();

        Map<String, Object> albumAfterDelete = getEsDoc("albums-admin", albumId);
        assertThat(albumAfterDelete.get("availability")).isEqualTo("DELETED");

        @SuppressWarnings("unchecked")
        List<Map<String, String>> tracksAfter = (List<Map<String, String>>) albumAfterDelete.get("tracks");
        assertThat(tracksAfter).hasSize(1);
        assertThat(tracksAfter.get(0).get("id")).isEqualTo(trackId);
    }
}
