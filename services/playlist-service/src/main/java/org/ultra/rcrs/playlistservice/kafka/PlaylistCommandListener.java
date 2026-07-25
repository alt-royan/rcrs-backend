package org.ultra.rcrs.playlistservice.kafka;

import com.google.protobuf.Any;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.ultra.rcrs.events.common.DomainEventOuterClass;
import org.ultra.rcrs.events.playlist.AddTracksToPlaylistEventOuterClass;
import org.ultra.rcrs.events.playlist.CreatePlaylistEventOuterClass;
import org.ultra.rcrs.events.playlist.DeletePlaylistEventOuterClass;
import org.ultra.rcrs.events.playlist.DeleteTracksFromPlaylistEventOuterClass;
import org.ultra.rcrs.kafka.Topics;
import org.ultra.rcrs.playlistservice.service.PlaylistService;

@Component
@Slf4j
@RequiredArgsConstructor
public class PlaylistCommandListener {

    private final PlaylistService playlistService;

    @KafkaListener(topics = Topics.PLAYLIST_COMMANDS_TOPIC, groupId = "playlist-service-group", containerFactory = "byteArrayContainerFactory")
    public void handleCommand(ConsumerRecord<String, byte[]> record) {
        try {
            DomainEventOuterClass.DomainEvent event = DomainEventOuterClass.DomainEvent.parseFrom(record.value());
            log.info("Received playlist command: type={} aggregate={} id={}", event.getEventType(), event.getAggregateType(), event.getAggregateId());

            switch (event.getEventType()) {
                case PLAYLIST_CREATED -> onPlaylistCreated(event.getPayload());
                case TRACKS_ADDED_TO_PLAYLIST -> onTracksAddedToPlaylist(event.getPayload());
                case TRACKS_REMOVED_FROM_PLAYLIST -> onTracksRemovedFromPlaylist(event.getPayload());
                case PLAYLIST_DELETED -> onPlaylistDeleted(event.getPayload());
                default -> log.warn("Unknown playlist command type: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("Failed to process playlist command: {}", e.getMessage(), e);
        }
    }

    private void onPlaylistCreated(Any payload) {
        try {
            CreatePlaylistEventOuterClass.CreatePlaylistEvent event = payload.unpack(CreatePlaylistEventOuterClass.CreatePlaylistEvent.class);
            playlistService.createPlaylist(event.getId(), event.getOwnerId(), event.getTitle(),
                    event.getDescription(), event.getCoverS3Key(), event.getIsPublic()).block();
        } catch (Exception e) {
            log.error("Failed to unpack CreatePlaylistEvent: {}", e.getMessage(), e);
        }
    }

    private void onTracksAddedToPlaylist(Any payload) {
        try {
            AddTracksToPlaylistEventOuterClass.AddTracksToPlaylistEvent event = payload.unpack(AddTracksToPlaylistEventOuterClass.AddTracksToPlaylistEvent.class);
            playlistService.addTracks(event.getPlaylistId(), event.getTrackIdsList()).block();
        } catch (Exception e) {
            log.error("Failed to unpack AddTracksToPlaylistEvent: {}", e.getMessage(), e);
        }
    }

    private void onTracksRemovedFromPlaylist(Any payload) {
        try {
            DeleteTracksFromPlaylistEventOuterClass.DeleteTracksFromPlaylistEvent event = payload.unpack(DeleteTracksFromPlaylistEventOuterClass.DeleteTracksFromPlaylistEvent.class);
            playlistService.deleteTracks(event.getPlaylistId(), event.getTrackIdsList()).block();
        } catch (Exception e) {
            log.error("Failed to unpack DeleteTracksFromPlaylistEvent: {}", e.getMessage(), e);
        }
    }

    private void onPlaylistDeleted(Any payload) {
        try {
            DeletePlaylistEventOuterClass.DeletePlaylistEvent event = payload.unpack(DeletePlaylistEventOuterClass.DeletePlaylistEvent.class);
            playlistService.deletePlaylist(event.getId()).block();
        } catch (Exception e) {
            log.error("Failed to unpack DeletePlaylistEvent: {}", e.getMessage(), e);
        }
    }
}
