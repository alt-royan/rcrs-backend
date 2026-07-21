package org.ultra.rcrs.metadata.integration;

import org.junit.jupiter.api.Test;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.enums.LifecycleStatus;
import org.ultra.rcrs.events.common.DomainEventOuterClass;
import org.ultra.rcrs.kafka.Topics;
import org.ultra.rcrs.metadata.model.Album;
import org.ultra.rcrs.metadata.model.Artist;
import org.ultra.rcrs.metadata.model.Track;
import org.ultra.rcrs.utils.Url62;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

class ArtistWriteControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    void createArtist_201Created_persistsAndEmitsEvent() throws Exception {
        String json = """
                {
                    "name": "Test Artist",
                    "avatarUri": "s3://bucket/avatar.jpg",
                    "socialLinks": [],
                    "tags": ["rock"]
                }
                """;

        String responseJson = mockMvc.perform(post("/artists")
                        .contentType("application/json")
                        .content(json))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isCreated())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.id").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        String encodedId = responseJson.replaceAll(".*\"id\"\\s*:\\s*\"([^\"]+)\".*", "$1");
        UUID artistId = Url62.decode(encodedId);

        Artist artist = artistRepository.findById(artistId).orElseThrow();
        assertThat(artist.getName()).isEqualTo("Test Artist");
        assertThat(artist.getAvailabilityStatus()).isEqualTo(EntityStatus.ACTIVE);

        List<TopicEvent> all = drainEvents(2);
        List<DomainEventOuterClass.DomainEvent> cdc = eventsOnTopic(all, Topics.CATALOG_CDC_TOPIC);
        List<DomainEventOuterClass.DomainEvent> search = eventsOnTopic(all, Topics.SEARCH_INDEX_TOPIC);

        assertThat(cdc).anyMatch(e ->
                e.getEventType() == DomainEventOuterClass.EventType.ARTIST_CREATED
                        && e.getAggregateId().equals(encodedId)
                        && e.getAggregateType() == DomainEventOuterClass.AggregateType.ARTIST
                        && e.getProducer().equals("metadata-write-service-test"));
        assertThat(search).anyMatch(e ->
                e.getEventType() == DomainEventOuterClass.EventType.ARTIST_CREATED
                        && e.getAggregateId().equals(encodedId)
                        && e.getAggregateType() == DomainEventOuterClass.AggregateType.ARTIST);
    }

    @Test
    void hideArtist_200OK_persistsAndEmitsEvent() throws Exception {
        Artist artist = createArtistInDb("Artist To Hide", EntityStatus.ACTIVE);
        String encodedId = Url62.encode(artist.getId());

        mockMvc.perform(put("/artists/" + encodedId + "/hide"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());

        Artist updated = artistRepository.findById(artist.getId()).orElseThrow();
        assertThat(updated.getAvailabilityStatus()).isEqualTo(EntityStatus.HIDDEN);

        List<TopicEvent> all = drainEvents(2);
        List<DomainEventOuterClass.DomainEvent> cdc = eventsOnTopic(all, Topics.CATALOG_CDC_TOPIC);
        List<DomainEventOuterClass.DomainEvent> search = eventsOnTopic(all, Topics.SEARCH_INDEX_TOPIC);

        assertThat(cdc).anyMatch(e ->
                e.getEventType() == DomainEventOuterClass.EventType.ARTIST_HIDDEN
                        && e.getAggregateId().equals(encodedId));
        assertThat(search).anyMatch(e ->
                e.getEventType() == DomainEventOuterClass.EventType.ARTIST_HIDDEN
                        && e.getAggregateId().equals(encodedId));
    }

    @Test
    void activeArtist_200OK_persistsAndEmitsEvent() throws Exception {
        Artist artist = createArtistInDb("Artist To Activate", EntityStatus.HIDDEN);
        String encodedId = Url62.encode(artist.getId());

        mockMvc.perform(put("/artists/" + encodedId + "/active"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());

        Artist updated = artistRepository.findById(artist.getId()).orElseThrow();
        assertThat(updated.getAvailabilityStatus()).isEqualTo(EntityStatus.ACTIVE);

        List<TopicEvent> all = drainEvents(2);
        List<DomainEventOuterClass.DomainEvent> cdc = eventsOnTopic(all, Topics.CATALOG_CDC_TOPIC);
        List<DomainEventOuterClass.DomainEvent> search = eventsOnTopic(all, Topics.SEARCH_INDEX_TOPIC);

        assertThat(cdc).anyMatch(e ->
                e.getEventType() == DomainEventOuterClass.EventType.ARTIST_ACTIVATED
                        && e.getAggregateId().equals(encodedId));
        assertThat(search).anyMatch(e ->
                e.getEventType() == DomainEventOuterClass.EventType.ARTIST_ACTIVATED
                        && e.getAggregateId().equals(encodedId));
    }

    @Test
    void deleteArtist_204NoContent_cascadesAndEmitsEvent() throws Exception {
        Artist artist = createArtistInDb("Artist To Delete", EntityStatus.ACTIVE);
        Album album = createAlbumInDb("Album To Delete", EntityStatus.ACTIVE);
        linkArtistToAlbum(artist.getId(), album.getId());
        Track track = createTrackInDb("Track To Delete", EntityStatus.ACTIVE, album.getId());

        mockMvc.perform(delete("/artists/" + Url62.encode(artist.getId())))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isNoContent());

        assertThat(artistRepository.findById(artist.getId()).orElseThrow().getAvailabilityStatus()).isEqualTo(EntityStatus.DELETED);
        assertThat(albumRepository.findById(album.getId()).orElseThrow().getAvailabilityStatus()).isEqualTo(EntityStatus.DELETED);
        assertThat(trackRepository.findById(track.getId()).orElseThrow().getAvailabilityStatus()).isEqualTo(EntityStatus.DELETED);

        List<TopicEvent> all = drainEvents(6);
        List<DomainEventOuterClass.DomainEvent> cdc = eventsOnTopic(all, Topics.CATALOG_CDC_TOPIC);
        List<DomainEventOuterClass.DomainEvent> search = eventsOnTopic(all, Topics.SEARCH_INDEX_TOPIC);

        assertThat(cdc).anyMatch(e -> e.getEventType() == DomainEventOuterClass.EventType.TRACK_DELETED);
        assertThat(cdc).anyMatch(e -> e.getEventType() == DomainEventOuterClass.EventType.ALBUM_DELETED);
        assertThat(cdc).anyMatch(e -> e.getEventType() == DomainEventOuterClass.EventType.ARTIST_DELETED);
        assertThat(search).anyMatch(e -> e.getEventType() == DomainEventOuterClass.EventType.TRACK_DELETED);
        assertThat(search).anyMatch(e -> e.getEventType() == DomainEventOuterClass.EventType.ALBUM_DELETED);
        assertThat(search).anyMatch(e -> e.getEventType() == DomainEventOuterClass.EventType.ARTIST_DELETED);
    }
}
