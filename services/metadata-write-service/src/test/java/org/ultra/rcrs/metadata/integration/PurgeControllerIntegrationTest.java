package org.ultra.rcrs.metadata.integration;

import org.junit.jupiter.api.Test;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.events.common.DomainEventOuterClass;
import org.ultra.rcrs.kafka.Topics;
import org.ultra.rcrs.metadata.model.Album;
import org.ultra.rcrs.metadata.model.Artist;
import org.ultra.rcrs.metadata.model.Track;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

class PurgeControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    void purge_200OK_deletesAndEmitsEvents() throws Exception {
        Artist artist = createArtistInDb("Deleted Artist", EntityStatus.DELETED);
        Album album = createAlbumInDb("Deleted Album", EntityStatus.DELETED);
        Track track = createTrackInDb("Deleted Track", EntityStatus.DELETED, album.getId());

        mockMvc.perform(post("/purge"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());

        assertThat(artistRepository.findById(artist.getId())).isEmpty();
        assertThat(albumRepository.findById(album.getId())).isEmpty();
        assertThat(trackRepository.findById(track.getId())).isEmpty();

        List<TopicEvent> all = drainEvents(6);
        List<DomainEventOuterClass.DomainEvent> cdc = eventsOnTopic(all, Topics.CATALOG_CDC_TOPIC);
        List<DomainEventOuterClass.DomainEvent> search = eventsOnTopic(all, Topics.SEARCH_INDEX_TOPIC);

        assertThat(cdc).anyMatch(e -> e.getEventType() == DomainEventOuterClass.EventType.TRACK_TRUE_DELETED);
        assertThat(cdc).anyMatch(e -> e.getEventType() == DomainEventOuterClass.EventType.ALBUM_TRUE_DELETED);
        assertThat(cdc).anyMatch(e -> e.getEventType() == DomainEventOuterClass.EventType.ARTIST_TRUE_DELETED);
        assertThat(search).anyMatch(e -> e.getEventType() == DomainEventOuterClass.EventType.TRACK_TRUE_DELETED);
        assertThat(search).anyMatch(e -> e.getEventType() == DomainEventOuterClass.EventType.ALBUM_TRUE_DELETED);
        assertThat(search).anyMatch(e -> e.getEventType() == DomainEventOuterClass.EventType.ARTIST_TRUE_DELETED);
    }
}
