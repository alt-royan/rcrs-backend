package org.ultra.rcrs.metadata.integration;

import org.junit.jupiter.api.Test;
import org.ultra.rcrs.enums.ArtistRole;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.enums.LifecycleStatus;
import org.ultra.rcrs.events.common.DomainEventOuterClass;
import org.ultra.rcrs.kafka.Topics;
import org.ultra.rcrs.metadata.dto.OtherArtistDto;
import org.ultra.rcrs.metadata.model.*;
import org.ultra.rcrs.utils.Url62;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

class TrackWriteControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    void createTrack_201Created_persistsAndEmitsEvents() throws Exception {
        Album album = createAlbumInDb("Track Album", EntityStatus.ACTIVE);

        String json = """
                {
                    "albumId": "%s",
                    "title": "Test Track",
                    "trackNumber": 1,
                    "explicit": false
                }
                """.formatted(Url62.encode(album.getId()));

        String responseJson = mockMvc.perform(post("/tracks")
                        .contentType("application/json")
                        .content(json))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isCreated())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.id").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        String encodedId = responseJson.replaceAll(".*\"id\"\\s*:\\s*\"([^\"]+)\".*", "$1");
        UUID trackId = Url62.decode(encodedId);

        Track track = trackRepository.findById(trackId).orElseThrow();
        assertThat(track.getTitle()).isEqualTo("Test Track");
        assertThat(track.getAvailabilityStatus()).isEqualTo(EntityStatus.ACTIVE);
        assertThat(track.getAlbumId()).isEqualTo(album.getId());

        List<TopicEvent> all = drainEvents(4);
        List<DomainEventOuterClass.DomainEvent> cdc = eventsOnTopic(all, Topics.CATALOG_CDC_TOPIC);
        List<DomainEventOuterClass.DomainEvent> search = eventsOnTopic(all, Topics.SEARCH_INDEX_TOPIC);

        assertThat(cdc).anyMatch(e ->
                e.getEventType() == DomainEventOuterClass.EventType.TRACK_CREATED
                        && e.getAggregateId().equals(encodedId));
        assertThat(cdc).anyMatch(e ->
                e.getEventType() == DomainEventOuterClass.EventType.TRACK_ADDED_TO_ALBUM
                        && e.getAggregateId().equals(encodedId));
        assertThat(search).anyMatch(e ->
                e.getEventType() == DomainEventOuterClass.EventType.TRACK_CREATED
                        && e.getAggregateId().equals(encodedId));
        assertThat(search).anyMatch(e ->
                e.getEventType() == DomainEventOuterClass.EventType.TRACK_ADDED_TO_ALBUM
                        && e.getAggregateId().equals(encodedId));
    }

    @Test
    void addArtistsToTrack_200OK_persistsJoinAndEmitsEvent() throws Exception {
        Artist artist = createArtistInDb("Track Artist", EntityStatus.ACTIVE);
        Album album = createAlbumInDb("Album", EntityStatus.ACTIVE);
        Track track = createTrackInDb("Track", EntityStatus.ACTIVE, album.getId());

        String json = """
                {
                    "artists": [
                        {
                            "id": "%s",
                            "name": "Track Artist",
                            "role": "MAIN_ARTIST"
                        }
                    ]
                }
                """.formatted(Url62.encode(artist.getId()));

        mockMvc.perform(post("/tracks/" + Url62.encode(track.getId()) + "/artists")
                        .contentType("application/json")
                        .content(json))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());

        assertThat(artistToTrackRepository.existsById(
                new ArtistToTrackPK(artist.getId(), track.getId()))).isTrue();

        List<TopicEvent> all = drainEvents(2);
        List<DomainEventOuterClass.DomainEvent> cdc = eventsOnTopic(all, Topics.CATALOG_CDC_TOPIC);
        List<DomainEventOuterClass.DomainEvent> search = eventsOnTopic(all, Topics.SEARCH_INDEX_TOPIC);

        assertThat(cdc).anyMatch(e ->
                e.getEventType() == DomainEventOuterClass.EventType.ARTIST_ADDED_TO_TRACK
                        && e.getAggregateId().equals(Url62.encode(track.getId())));
        assertThat(search).anyMatch(e ->
                e.getEventType() == DomainEventOuterClass.EventType.ARTIST_ADDED_TO_TRACK
                        && e.getAggregateId().equals(Url62.encode(track.getId())));
    }

    @Test
    void deleteArtistsFromTrack_200OK_removesJoinAndEmitsEvent() throws Exception {
        Artist artist = createArtistInDb("Artist To Remove", EntityStatus.ACTIVE);
        Album album = createAlbumInDb("Album", EntityStatus.ACTIVE);
        Track track = createTrackInDb("Track", EntityStatus.ACTIVE, album.getId());

        artistToTrackRepository.save(ArtistToTrack.builder()
                .artistId(artist.getId())
                .trackId(track.getId())
                .artistRole(ArtistRole.MAIN_ARTIST)
                .build());

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

        mockMvc.perform(delete("/tracks/" + Url62.encode(track.getId()) + "/artists")
                        .contentType("application/json")
                        .content(json))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());

        assertThat(artistToTrackRepository.existsById(
                new ArtistToTrackPK(artist.getId(), track.getId()))).isFalse();

        List<TopicEvent> all = drainEvents(2);
        List<DomainEventOuterClass.DomainEvent> cdc = eventsOnTopic(all, Topics.CATALOG_CDC_TOPIC);
        List<DomainEventOuterClass.DomainEvent> search = eventsOnTopic(all, Topics.SEARCH_INDEX_TOPIC);

        assertThat(cdc).anyMatch(e ->
                e.getEventType() == DomainEventOuterClass.EventType.ARTIST_DELETED_FROM_TRACK);
        assertThat(search).anyMatch(e ->
                e.getEventType() == DomainEventOuterClass.EventType.ARTIST_DELETED_FROM_TRACK);
    }

    @Test
    void addOthersToTrack_200OK_persistsAndEmitsEvent() throws Exception {
        Album album = createAlbumInDb("Album", EntityStatus.ACTIVE);
        Track track = createTrackInDb("Track", EntityStatus.ACTIVE, album.getId());

        String json = """
                {
                    "others": [
                        {
                            "name": "Guest Artist",
                            "roles": ["FEATURED_ARTIST"],
                            "socialLinks": []
                        }
                    ]
                }
                """;

        mockMvc.perform(post("/tracks/" + Url62.encode(track.getId()) + "/others")
                        .contentType("application/json")
                        .content(json))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());

        List<OtherArtist> others = otherArtistRepository.findAll();
        assertThat(others).hasSize(1);
        assertThat(others.get(0).getName()).isEqualTo("Guest Artist");
        assertThat(others.get(0).getTrackId()).isEqualTo(track.getId());

        List<TopicEvent> all = drainEvents(2);
        List<DomainEventOuterClass.DomainEvent> cdc = eventsOnTopic(all, Topics.CATALOG_CDC_TOPIC);
        List<DomainEventOuterClass.DomainEvent> search = eventsOnTopic(all, Topics.SEARCH_INDEX_TOPIC);

        assertThat(cdc).anyMatch(e ->
                e.getEventType() == DomainEventOuterClass.EventType.OTHER_ADDED_TO_TRACK
                        && e.getAggregateId().equals(Url62.encode(track.getId())));
        assertThat(search).anyMatch(e ->
                e.getEventType() == DomainEventOuterClass.EventType.OTHER_ADDED_TO_TRACK
                        && e.getAggregateId().equals(Url62.encode(track.getId())));
    }

    @Test
    void deleteOthersFromTrack_200OK_removesAndEmitsEvent() throws Exception {
        Album album = createAlbumInDb("Album", EntityStatus.ACTIVE);
        Track track = createTrackInDb("Track", EntityStatus.ACTIVE, album.getId());

        OtherArtist other = otherArtistRepository.save(
                new OtherArtist(new OtherArtistDto() {{
                    setName("Guest To Remove");
                    setRoles(Set.of(ArtistRole.FEATURED_ARTIST));
                    setSocialLinks(List.of());
                }}, track.getId()));

        String json = """
                {
                    "others": [
                        {
                            "id": "%s",
                            "name": "Guest To Remove"
                        }
                    ]
                }
                """.formatted(Url62.encode(other.getId()));

        mockMvc.perform(delete("/tracks/" + Url62.encode(track.getId()) + "/others")
                        .contentType("application/json")
                        .content(json))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());

        assertThat(otherArtistRepository.existsById(other.getId())).isFalse();

        List<TopicEvent> all = drainEvents(2);
        List<DomainEventOuterClass.DomainEvent> cdc = eventsOnTopic(all, Topics.CATALOG_CDC_TOPIC);
        List<DomainEventOuterClass.DomainEvent> search = eventsOnTopic(all, Topics.SEARCH_INDEX_TOPIC);

        assertThat(cdc).anyMatch(e ->
                e.getEventType() == DomainEventOuterClass.EventType.OTHER_DELETED_FROM_TRACK);
        assertThat(search).anyMatch(e ->
                e.getEventType() == DomainEventOuterClass.EventType.OTHER_DELETED_FROM_TRACK);
    }

    @Test
    void updateTrackStatus_200OK_persistsAndEmitsEvent() throws Exception {
        Album album = createAlbumInDb("Album", EntityStatus.ACTIVE);
        Track track = createTrackInDb("Track", EntityStatus.ACTIVE, album.getId());

        String json = """
                {
                    "status": "TRANSCODING"
                }
                """;

        mockMvc.perform(put("/tracks/" + Url62.encode(track.getId()) + "/status")
                        .contentType("application/json")
                        .content(json))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());

        Track updated = trackRepository.findById(track.getId()).orElseThrow();
        assertThat(updated.getLifecycleStatus()).isEqualTo(LifecycleStatus.TRANSCODING);

        List<TopicEvent> all = drainEvents(2);
        List<DomainEventOuterClass.DomainEvent> cdc = eventsOnTopic(all, Topics.CATALOG_CDC_TOPIC);
        List<DomainEventOuterClass.DomainEvent> search = eventsOnTopic(all, Topics.SEARCH_INDEX_TOPIC);

        assertThat(cdc).anyMatch(e ->
                e.getEventType() == DomainEventOuterClass.EventType.TRACK_LIFECYCLE_STATUS_UPDATED
                        && e.getAggregateId().equals(Url62.encode(track.getId())));
        assertThat(search).anyMatch(e ->
                e.getEventType() == DomainEventOuterClass.EventType.TRACK_LIFECYCLE_STATUS_UPDATED
                        && e.getAggregateId().equals(Url62.encode(track.getId())));
    }

    @Test
    void hideTrack_200OK_persistsAndEmitsEvent() throws Exception {
        Album album = createAlbumInDb("Album", EntityStatus.ACTIVE);
        Track track = createTrackInDb("Track To Hide", EntityStatus.ACTIVE, album.getId());

        mockMvc.perform(put("/tracks/" + Url62.encode(track.getId()) + "/hide"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());

        Track updated = trackRepository.findById(track.getId()).orElseThrow();
        assertThat(updated.getAvailabilityStatus()).isEqualTo(EntityStatus.HIDDEN);

        List<TopicEvent> all = drainEvents(2);
        List<DomainEventOuterClass.DomainEvent> cdc = eventsOnTopic(all, Topics.CATALOG_CDC_TOPIC);
        List<DomainEventOuterClass.DomainEvent> search = eventsOnTopic(all, Topics.SEARCH_INDEX_TOPIC);

        assertThat(cdc).anyMatch(e ->
                e.getEventType() == DomainEventOuterClass.EventType.TRACK_HIDDEN
                        && e.getAggregateId().equals(Url62.encode(track.getId())));
        assertThat(search).anyMatch(e ->
                e.getEventType() == DomainEventOuterClass.EventType.TRACK_HIDDEN
                        && e.getAggregateId().equals(Url62.encode(track.getId())));
    }

    @Test
    void activeTrack_200OK_persistsAndEmitsEvent() throws Exception {
        Album album = createAlbumInDb("Album", EntityStatus.ACTIVE);
        Track track = createTrackInDb("Track To Activate", EntityStatus.HIDDEN, album.getId());

        mockMvc.perform(put("/tracks/" + Url62.encode(track.getId()) + "/active"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());

        Track updated = trackRepository.findById(track.getId()).orElseThrow();
        assertThat(updated.getAvailabilityStatus()).isEqualTo(EntityStatus.ACTIVE);

        List<TopicEvent> all = drainEvents(2);
        List<DomainEventOuterClass.DomainEvent> cdc = eventsOnTopic(all, Topics.CATALOG_CDC_TOPIC);
        List<DomainEventOuterClass.DomainEvent> search = eventsOnTopic(all, Topics.SEARCH_INDEX_TOPIC);

        assertThat(cdc).anyMatch(e ->
                e.getEventType() == DomainEventOuterClass.EventType.TRACK_ACTIVATED
                        && e.getAggregateId().equals(Url62.encode(track.getId())));
        assertThat(search).anyMatch(e ->
                e.getEventType() == DomainEventOuterClass.EventType.TRACK_ACTIVATED
                        && e.getAggregateId().equals(Url62.encode(track.getId())));
    }

    @Test
    void deleteTrack_204NoContent_persistsAndEmitsEvent() throws Exception {
        Album album = createAlbumInDb("Album", EntityStatus.ACTIVE);
        Track track = createTrackInDb("Track To Delete", EntityStatus.ACTIVE, album.getId());

        mockMvc.perform(delete("/tracks/" + Url62.encode(track.getId())))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isNoContent());

        Track updated = trackRepository.findById(track.getId()).orElseThrow();
        assertThat(updated.getAvailabilityStatus()).isEqualTo(EntityStatus.DELETED);

        List<TopicEvent> all = drainEvents(2);
        List<DomainEventOuterClass.DomainEvent> cdc = eventsOnTopic(all, Topics.CATALOG_CDC_TOPIC);
        List<DomainEventOuterClass.DomainEvent> search = eventsOnTopic(all, Topics.SEARCH_INDEX_TOPIC);

        assertThat(cdc).anyMatch(e ->
                e.getEventType() == DomainEventOuterClass.EventType.TRACK_DELETED
                        && e.getAggregateId().equals(Url62.encode(track.getId())));
        assertThat(search).anyMatch(e ->
                e.getEventType() == DomainEventOuterClass.EventType.TRACK_DELETED
                        && e.getAggregateId().equals(Url62.encode(track.getId())));
    }
}
