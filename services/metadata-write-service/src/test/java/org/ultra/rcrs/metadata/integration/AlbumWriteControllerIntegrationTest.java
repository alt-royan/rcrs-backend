package org.ultra.rcrs.metadata.integration;

import org.junit.jupiter.api.Test;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.enums.LifecycleStatus;
import org.ultra.rcrs.events.common.DomainEventOuterClass;
import org.ultra.rcrs.kafka.Topics;
import org.ultra.rcrs.metadata.model.Album;
import org.ultra.rcrs.metadata.model.Artist;
import org.ultra.rcrs.metadata.model.ArtistToAlbumPK;
import org.ultra.rcrs.metadata.model.Track;
import org.ultra.rcrs.utils.Url62;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

class AlbumWriteControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    void createAlbum_201Created_persistsAndEmitsEvent() throws Exception {
        String json = """
                {
                    "title": "Test Album",
                    "type": "FULL",
                    "releaseDate": "2025-01-15T00:00:00",
                    "coverUri": "s3://bucket/cover.jpg"
                }
                """;

        String responseJson = mockMvc.perform(post("/albums")
                        .contentType("application/json")
                        .content(json))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isCreated())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.id").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        String encodedId = responseJson.replaceAll(".*\"id\"\\s*:\\s*\"([^\"]+)\".*", "$1");
        UUID albumId = Url62.decode(encodedId);

        Album album = albumRepository.findById(albumId).orElseThrow();
        assertThat(album.getTitle()).isEqualTo("Test Album");
        assertThat(album.getAvailabilityStatus()).isEqualTo(EntityStatus.ACTIVE);
        assertThat(album.getLifecycleStatus()).isEqualTo(LifecycleStatus.CREATED);

        List<TopicEvent> all = drainEvents(2);
        List<DomainEventOuterClass.DomainEvent> cdc = eventsOnTopic(all, Topics.CATALOG_CDC_TOPIC);
        List<DomainEventOuterClass.DomainEvent> search = eventsOnTopic(all, Topics.SEARCH_INDEX_TOPIC);

        assertThat(cdc).anyMatch(e ->
                e.getEventType() == DomainEventOuterClass.EventType.ALBUM_CREATED
                        && e.getAggregateId().equals(encodedId));
        assertThat(search).anyMatch(e ->
                e.getEventType() == DomainEventOuterClass.EventType.ALBUM_CREATED
                        && e.getAggregateId().equals(encodedId));
    }

    @Test
    void addArtistsToAlbum_200OK_persistsJoinAndEmitsEvent() throws Exception {
        Artist artist = createArtistInDb("Album Artist", EntityStatus.ACTIVE);
        Album album = createAlbumInDb("Album With Artist", EntityStatus.ACTIVE);

        String json = """
                {
                    "artists": [
                        {
                            "id": "%s",
                            "name": "Album Artist",
                            "role": "MAIN_ARTIST"
                        }
                    ]
                }
                """.formatted(Url62.encode(artist.getId()));

        mockMvc.perform(post("/albums/" + Url62.encode(album.getId()) + "/artists")
                        .contentType("application/json")
                        .content(json))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());

        assertThat(artistToAlbumRepository.existsById(
                new ArtistToAlbumPK(artist.getId(), album.getId()))).isTrue();

        List<TopicEvent> all = drainEvents(2);
        List<DomainEventOuterClass.DomainEvent> cdc = eventsOnTopic(all, Topics.CATALOG_CDC_TOPIC);
        List<DomainEventOuterClass.DomainEvent> search = eventsOnTopic(all, Topics.SEARCH_INDEX_TOPIC);

        assertThat(cdc).anyMatch(e ->
                e.getEventType() == DomainEventOuterClass.EventType.ARTIST_ADDED_TO_ALBUM
                        && e.getAggregateId().equals(Url62.encode(album.getId())));
        assertThat(search).anyMatch(e ->
                e.getEventType() == DomainEventOuterClass.EventType.ARTIST_ADDED_TO_ALBUM
                        && e.getAggregateId().equals(Url62.encode(album.getId())));
    }

    @Test
    void deleteArtistsFromAlbum_200OK_removesJoinAndEmitsEvent() throws Exception {
        Artist artist = createArtistInDb("Artist To Remove", EntityStatus.ACTIVE);
        Album album = createAlbumInDb("Album", EntityStatus.ACTIVE);
        linkArtistToAlbum(artist.getId(), album.getId());

        String json = """
                {
                    "artists": [
                        {
                            "id": "%s",
                            "name": "Artist To Remove",
                            "role": "MAIN_ARTIST"
                        }
                    ]
                }
                """.formatted(Url62.encode(artist.getId()));

        mockMvc.perform(delete("/albums/" + Url62.encode(album.getId()) + "/artists")
                        .contentType("application/json")
                        .content(json))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());

        assertThat(artistToAlbumRepository.existsById(
                new ArtistToAlbumPK(artist.getId(), album.getId()))).isFalse();

        List<TopicEvent> all = drainEvents(2);
        List<DomainEventOuterClass.DomainEvent> cdc = eventsOnTopic(all, Topics.CATALOG_CDC_TOPIC);
        List<DomainEventOuterClass.DomainEvent> search = eventsOnTopic(all, Topics.SEARCH_INDEX_TOPIC);

        assertThat(cdc).anyMatch(e ->
                e.getEventType() == DomainEventOuterClass.EventType.ARTIST_DELETED_FROM_ALBUM);
        assertThat(search).anyMatch(e ->
                e.getEventType() == DomainEventOuterClass.EventType.ARTIST_DELETED_FROM_ALBUM);
    }

    @Test
    void updateAlbumStatus_200OK_persistsAndEmitsEvent() throws Exception {
        Album album = createAlbumInDb("Album Status", EntityStatus.ACTIVE);

        String json = """
                {
                    "status": "TRANSCODING"
                }
                """;

        mockMvc.perform(put("/albums/" + Url62.encode(album.getId()) + "/status")
                        .contentType("application/json")
                        .content(json))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());

        Album updated = albumRepository.findById(album.getId()).orElseThrow();
        assertThat(updated.getLifecycleStatus()).isEqualTo(LifecycleStatus.TRANSCODING);

        List<TopicEvent> all = drainEvents(2);
        List<DomainEventOuterClass.DomainEvent> cdc = eventsOnTopic(all, Topics.CATALOG_CDC_TOPIC);
        List<DomainEventOuterClass.DomainEvent> search = eventsOnTopic(all, Topics.SEARCH_INDEX_TOPIC);

        assertThat(cdc).anyMatch(e ->
                e.getEventType() == DomainEventOuterClass.EventType.ALBUM_LIFECYCLE_STATUS_UPDATED
                        && e.getAggregateId().equals(Url62.encode(album.getId())));
        assertThat(search).anyMatch(e ->
                e.getEventType() == DomainEventOuterClass.EventType.ALBUM_LIFECYCLE_STATUS_UPDATED
                        && e.getAggregateId().equals(Url62.encode(album.getId())));
    }

    @Test
    void hideAlbum_200OK_cascadesAndEmitsEvent() throws Exception {
        Album album = createAlbumInDb("Album To Hide", EntityStatus.ACTIVE);
        Track track = createTrackInDb("Track In Hidden Album", EntityStatus.ACTIVE, album.getId());

        mockMvc.perform(put("/albums/" + Url62.encode(album.getId()) + "/hide"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());

        assertThat(albumRepository.findById(album.getId()).orElseThrow().getAvailabilityStatus()).isEqualTo(EntityStatus.HIDDEN);
        assertThat(trackRepository.findById(track.getId()).orElseThrow().getAvailabilityStatus()).isEqualTo(EntityStatus.HIDDEN);

        List<TopicEvent> all = drainEvents(4);
        List<DomainEventOuterClass.DomainEvent> cdc = eventsOnTopic(all, Topics.CATALOG_CDC_TOPIC);
        List<DomainEventOuterClass.DomainEvent> search = eventsOnTopic(all, Topics.SEARCH_INDEX_TOPIC);

        assertThat(cdc).anyMatch(e -> e.getEventType() == DomainEventOuterClass.EventType.TRACK_HIDDEN);
        assertThat(cdc).anyMatch(e -> e.getEventType() == DomainEventOuterClass.EventType.ALBUM_HIDDEN);
        assertThat(search).anyMatch(e -> e.getEventType() == DomainEventOuterClass.EventType.TRACK_HIDDEN);
        assertThat(search).anyMatch(e -> e.getEventType() == DomainEventOuterClass.EventType.ALBUM_HIDDEN);
    }

    @Test
    void activeAlbum_200OK_cascadesAndEmitsEvent() throws Exception {
        Album album = createAlbumInDb("Album To Activate", EntityStatus.HIDDEN);
        Track track = createTrackInDb("Track In Activated Album", EntityStatus.HIDDEN, album.getId());

        mockMvc.perform(put("/albums/" + Url62.encode(album.getId()) + "/active"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());

        assertThat(albumRepository.findById(album.getId()).orElseThrow().getAvailabilityStatus()).isEqualTo(EntityStatus.ACTIVE);
        assertThat(trackRepository.findById(track.getId()).orElseThrow().getAvailabilityStatus()).isEqualTo(EntityStatus.ACTIVE);

        List<TopicEvent> all = drainEvents(4);
        List<DomainEventOuterClass.DomainEvent> cdc = eventsOnTopic(all, Topics.CATALOG_CDC_TOPIC);
        List<DomainEventOuterClass.DomainEvent> search = eventsOnTopic(all, Topics.SEARCH_INDEX_TOPIC);

        assertThat(cdc).anyMatch(e -> e.getEventType() == DomainEventOuterClass.EventType.TRACK_ACTIVATED);
        assertThat(cdc).anyMatch(e -> e.getEventType() == DomainEventOuterClass.EventType.ALBUM_ACTIVATED);
        assertThat(search).anyMatch(e -> e.getEventType() == DomainEventOuterClass.EventType.TRACK_ACTIVATED);
        assertThat(search).anyMatch(e -> e.getEventType() == DomainEventOuterClass.EventType.ALBUM_ACTIVATED);
    }

    @Test
    void deleteAlbum_204NoContent_cascadesAndEmitsEvent() throws Exception {
        Album album = createAlbumInDb("Album To Delete", EntityStatus.ACTIVE);
        Track track = createTrackInDb("Track In Deleted Album", EntityStatus.ACTIVE, album.getId());

        mockMvc.perform(delete("/albums/" + Url62.encode(album.getId())))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isNoContent());

        assertThat(albumRepository.findById(album.getId()).orElseThrow().getAvailabilityStatus()).isEqualTo(EntityStatus.DELETED);
        assertThat(trackRepository.findById(track.getId()).orElseThrow().getAvailabilityStatus()).isEqualTo(EntityStatus.DELETED);

        List<TopicEvent> all = drainEvents(4);
        List<DomainEventOuterClass.DomainEvent> cdc = eventsOnTopic(all, Topics.CATALOG_CDC_TOPIC);
        List<DomainEventOuterClass.DomainEvent> search = eventsOnTopic(all, Topics.SEARCH_INDEX_TOPIC);

        assertThat(cdc).anyMatch(e -> e.getEventType() == DomainEventOuterClass.EventType.TRACK_DELETED);
        assertThat(cdc).anyMatch(e -> e.getEventType() == DomainEventOuterClass.EventType.ALBUM_DELETED);
        assertThat(search).anyMatch(e -> e.getEventType() == DomainEventOuterClass.EventType.TRACK_DELETED);
        assertThat(search).anyMatch(e -> e.getEventType() == DomainEventOuterClass.EventType.ALBUM_DELETED);
    }
}
