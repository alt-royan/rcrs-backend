package org.ultra.rcrs.metadata.kafka.listener;

import com.google.protobuf.Any;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.ultra.rcrs.events.album.AlbumCreatedEventOuterClass;
import org.ultra.rcrs.events.album.AlbumDeletedEventOuterClass;
import org.ultra.rcrs.events.album.AlbumHiddenEventOuterClass;
import org.ultra.rcrs.events.album.AlbumActivatedEventOuterClass;
import org.ultra.rcrs.events.album.AlbumUpdateLifecycleStatusEventOuterClass;
import org.ultra.rcrs.events.album.ArtistAddedToAlbumEventOuterClass;
import org.ultra.rcrs.events.album.ArtistDeletedFromAlbumEventOuterClass;
import org.ultra.rcrs.events.artist.ArtistCreatedEventOuterClass;
import org.ultra.rcrs.events.artist.ArtistDeletedEventOuterClass;
import org.ultra.rcrs.events.artist.ArtistHiddenEventOuterClass;
import org.ultra.rcrs.events.artist.ArtistActivatedEventOuterClass;
import org.ultra.rcrs.events.artist.ArtistTrueDeletedEventOuterClass;
import org.ultra.rcrs.events.album.AlbumTrueDeletedEventOuterClass;
import org.ultra.rcrs.events.track.TrackTrueDeletedEventOuterClass;
import org.ultra.rcrs.events.common.DomainEventOuterClass;
import org.ultra.rcrs.events.track.ArtistAddedToTrackEventOuterClass;
import org.ultra.rcrs.events.track.ArtistDeletedFromTrackEventOuterClass;
import org.ultra.rcrs.events.track.OtherAddedToTrackEventOuterClass;
import org.ultra.rcrs.events.track.OtherDeletedFromTrackEventOuterClass;
import org.ultra.rcrs.events.track.TrackCreatedEventOuterClass;
import org.ultra.rcrs.events.track.TrackDeletedEventOuterClass;
import org.ultra.rcrs.events.track.TrackHiddenEventOuterClass;
import org.ultra.rcrs.events.track.TrackActivatedEventOuterClass;
import org.ultra.rcrs.events.track.TrackAddedToAlbumEventOuterClass;
import org.ultra.rcrs.events.track.TrackUpdateLifecycleStatusEventOuterClass;
import org.ultra.rcrs.kafka.Topics;
import org.ultra.rcrs.metadata.service.write.AlbumWriteService;
import org.ultra.rcrs.metadata.service.write.ArtistWriteService;
import org.ultra.rcrs.metadata.service.write.TrackWriteService;

@Component
@Slf4j
@RequiredArgsConstructor
public class CdcEventListener {

    private final ArtistWriteService artistWriteService;
    private final AlbumWriteService albumWriteService;
    private final TrackWriteService trackWriteService;

    @KafkaListener(topics = Topics.CATALOG_CDC_TOPIC, groupId = "catalog-read-group", containerFactory = "byteArrayContainerFactory")
    public void handleCdcEvent(ConsumerRecord<String, byte[]> record) {
        try {
            DomainEventOuterClass.DomainEvent event = DomainEventOuterClass.DomainEvent.parseFrom(record.value());
            log.info("Received CDC event: type={} aggregate={} id={}", event.getEventType(), event.getAggregateType(), event.getAggregateId());

            switch (event.getEventType()) {
                case ARTIST_CREATED -> onArtistCreated(event.getPayload());
                case ARTIST_DELETED -> onArtistDeleted(event.getPayload());
                case ARTIST_HIDDEN -> onArtistHidden(event.getPayload());
                case ALBUM_CREATED -> onAlbumCreated(event.getPayload());
                case ALBUM_DELETED -> onAlbumDeleted(event.getPayload());
                case ALBUM_HIDDEN -> onAlbumHidden(event.getPayload());
                case TRACK_CREATED -> onTrackCreated(event.getPayload());
                case TRACK_DELETED -> onTrackDeleted(event.getPayload());
                case TRACK_HIDDEN -> onTrackHidden(event.getPayload());
                case ARTIST_ADDED_TO_TRACK -> onArtistAddedToTrack(event.getPayload());
                case ARTIST_ADDED_TO_ALBUM -> onArtistAddedToAlbum(event.getPayload());
                case OTHER_ADDED_TO_TRACK -> onOtherAddedToTrack(event.getPayload());
                case OTHER_DELETED_FROM_TRACK -> onOtherDeletedFromTrack(event.getPayload());
                case ARTIST_DELETED_FROM_ALBUM -> onArtistDeletedFromAlbum(event.getPayload());
                case ARTIST_DELETED_FROM_TRACK -> onArtistDeletedFromTrack(event.getPayload());
                case ALBUM_LIFECYCLE_STATUS_UPDATED -> onAlbumLifecycleStatusUpdated(event.getPayload());
                case TRACK_LIFECYCLE_STATUS_UPDATED -> onTrackLifecycleStatusUpdated(event.getPayload());
                case TRACK_ADDED_TO_ALBUM -> onTrackAddedToAlbum(event.getPayload());
                case ARTIST_ACTIVATED -> onArtistActivated(event.getPayload());
                case ALBUM_ACTIVATED -> onAlbumActivated(event.getPayload());
                case TRACK_ACTIVATED -> onTrackActivated(event.getPayload());
                case ARTIST_TRUE_DELETED -> onArtistTrueDeleted(event.getPayload());
                case ALBUM_TRUE_DELETED -> onAlbumTrueDeleted(event.getPayload());
                case TRACK_TRUE_DELETED -> onTrackTrueDeleted(event.getPayload());
                default -> log.warn("Unknown event type: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("Failed to process CDC event: {}", e.getMessage(), e);
        }
    }

    private void onArtistCreated(Any payload) {
        try {
            ArtistCreatedEventOuterClass.ArtistCreatedEvent event = payload.unpack(ArtistCreatedEventOuterClass.ArtistCreatedEvent.class);
            artistWriteService.handleArtistCreated(event);
        } catch (Exception e) {
            log.error("Failed to unpack ArtistCreatedEvent: {}", e.getMessage(), e);
        }
    }

    private void onArtistDeleted(Any payload) {
        try {
            ArtistDeletedEventOuterClass.ArtistDeletedEvent event = payload.unpack(ArtistDeletedEventOuterClass.ArtistDeletedEvent.class);
            artistWriteService.handleArtistDeleted(event.getId());
        } catch (Exception e) {
            log.error("Failed to unpack ArtistDeletedEvent: {}", e.getMessage(), e);
        }
    }

    private void onArtistHidden(Any payload) {
        try {
            ArtistHiddenEventOuterClass.ArtistHiddenEvent event = payload.unpack(ArtistHiddenEventOuterClass.ArtistHiddenEvent.class);
            artistWriteService.handleArtistHidden(event.getId());
        } catch (Exception e) {
            log.error("Failed to unpack ArtistHiddenEvent: {}", e.getMessage(), e);
        }
    }

    private void onAlbumCreated(Any payload) {
        try {
            AlbumCreatedEventOuterClass.AlbumCreatedEvent event = payload.unpack(AlbumCreatedEventOuterClass.AlbumCreatedEvent.class);
            albumWriteService.handleAlbumCreated(event);
        } catch (Exception e) {
            log.error("Failed to unpack AlbumCreatedEvent: {}", e.getMessage(), e);
        }
    }

    private void onAlbumDeleted(Any payload) {
        try {
            AlbumDeletedEventOuterClass.AlbumDeletedEvent event = payload.unpack(AlbumDeletedEventOuterClass.AlbumDeletedEvent.class);
            albumWriteService.handleAlbumDeleted(event.getId());
        } catch (Exception e) {
            log.error("Failed to unpack AlbumDeletedEvent: {}", e.getMessage(), e);
        }
    }

    private void onAlbumHidden(Any payload) {
        try {
            AlbumHiddenEventOuterClass.AlbumHiddenEvent event = payload.unpack(AlbumHiddenEventOuterClass.AlbumHiddenEvent.class);
            albumWriteService.handleAlbumHidden(event.getId());
        } catch (Exception e) {
            log.error("Failed to unpack AlbumHiddenEvent: {}", e.getMessage(), e);
        }
    }

    private void onTrackCreated(Any payload) {
        try {
            TrackCreatedEventOuterClass.TrackCreatedEvent event = payload.unpack(TrackCreatedEventOuterClass.TrackCreatedEvent.class);
            trackWriteService.handleTrackCreated(event);
        } catch (Exception e) {
            log.error("Failed to unpack TrackCreatedEvent: {}", e.getMessage(), e);
        }
    }

    private void onTrackDeleted(Any payload) {
        try {
            TrackDeletedEventOuterClass.TrackDeletedEvent event = payload.unpack(TrackDeletedEventOuterClass.TrackDeletedEvent.class);
            trackWriteService.handleTrackDeleted(event.getId());
        } catch (Exception e) {
            log.error("Failed to unpack TrackDeletedEvent: {}", e.getMessage(), e);
        }
    }

    private void onTrackHidden(Any payload) {
        try {
            TrackHiddenEventOuterClass.TrackHiddenEvent event = payload.unpack(TrackHiddenEventOuterClass.TrackHiddenEvent.class);
            trackWriteService.handleTrackHidden(event.getId());
        } catch (Exception e) {
            log.error("Failed to unpack TrackHiddenEvent: {}", e.getMessage(), e);
        }
    }

    private void onArtistAddedToTrack(Any payload) {
        try {
            ArtistAddedToTrackEventOuterClass.ArtistAddedToTrackEvent event = payload.unpack(ArtistAddedToTrackEventOuterClass.ArtistAddedToTrackEvent.class);
            trackWriteService.handleArtistAddedToTrack(event);
        } catch (Exception e) {
            log.error("Failed to unpack ArtistAddedToTrackEvent: {}", e.getMessage(), e);
        }
    }

    private void onArtistAddedToAlbum(Any payload) {
        try {
            ArtistAddedToAlbumEventOuterClass.ArtistAddedToAlbumEvent event = payload.unpack(ArtistAddedToAlbumEventOuterClass.ArtistAddedToAlbumEvent.class);
            albumWriteService.handleArtistAddedToAlbum(event);
        } catch (Exception e) {
            log.error("Failed to unpack ArtistAddedToAlbumEvent: {}", e.getMessage(), e);
        }
    }

    private void onOtherAddedToTrack(Any payload) {
        try {
            OtherAddedToTrackEventOuterClass.OtherAddedToTrackEvent event = payload.unpack(OtherAddedToTrackEventOuterClass.OtherAddedToTrackEvent.class);
            trackWriteService.handleOtherAddedToTrack(event);
        } catch (Exception e) {
            log.error("Failed to unpack OtherAddedToTrackEvent: {}", e.getMessage(), e);
        }
    }

    private void onOtherDeletedFromTrack(Any payload) {
        try {
            OtherDeletedFromTrackEventOuterClass.OtherDeletedFromTrackEvent event = payload.unpack(OtherDeletedFromTrackEventOuterClass.OtherDeletedFromTrackEvent.class);
            trackWriteService.handleOtherDeletedFromTrack(event);
        } catch (Exception e) {
            log.error("Failed to unpack OtherDeletedFromTrackEvent: {}", e.getMessage(), e);
        }
    }

    private void onArtistDeletedFromAlbum(Any payload) {
        try {
            ArtistDeletedFromAlbumEventOuterClass.ArtistDeletedFromAlbumEvent event = payload.unpack(ArtistDeletedFromAlbumEventOuterClass.ArtistDeletedFromAlbumEvent.class);
            albumWriteService.handleArtistDeletedFromAlbum(event);
        } catch (Exception e) {
            log.error("Failed to unpack ArtistDeletedFromAlbumEvent: {}", e.getMessage(), e);
        }
    }

    private void onArtistDeletedFromTrack(Any payload) {
        try {
            ArtistDeletedFromTrackEventOuterClass.ArtistDeletedFromTrackEvent event = payload.unpack(ArtistDeletedFromTrackEventOuterClass.ArtistDeletedFromTrackEvent.class);
            trackWriteService.handleArtistDeletedFromTrack(event);
        } catch (Exception e) {
            log.error("Failed to unpack ArtistDeletedFromTrackEvent: {}", e.getMessage(), e);
        }
    }

    private void onAlbumLifecycleStatusUpdated(Any payload) {
        try {
            AlbumUpdateLifecycleStatusEventOuterClass.AlbumUpdateLifecycleStatusEvent event = payload.unpack(AlbumUpdateLifecycleStatusEventOuterClass.AlbumUpdateLifecycleStatusEvent.class);
            albumWriteService.handleAlbumLifecycleStatusUpdated(event);
        } catch (Exception e) {
            log.error("Failed to unpack AlbumUpdateLifecycleStatusEvent: {}", e.getMessage(), e);
        }
    }

    private void onTrackLifecycleStatusUpdated(Any payload) {
        try {
            TrackUpdateLifecycleStatusEventOuterClass.TrackUpdateLifecycleStatusEvent event = payload.unpack(TrackUpdateLifecycleStatusEventOuterClass.TrackUpdateLifecycleStatusEvent.class);
            trackWriteService.handleTrackLifecycleStatusUpdated(event);
        } catch (Exception e) {
            log.error("Failed to unpack TrackUpdateLifecycleStatusEvent: {}", e.getMessage(), e);
        }
    }

    private void onTrackAddedToAlbum(Any payload) {
        try {
            TrackAddedToAlbumEventOuterClass.TrackAddedToAlbumEvent event = payload.unpack(TrackAddedToAlbumEventOuterClass.TrackAddedToAlbumEvent.class);
            trackWriteService.handleTrackAddedToAlbum(event);
        } catch (Exception e) {
            log.error("Failed to unpack TrackAddedToAlbumEvent: {}", e.getMessage(), e);
        }
    }

    private void onArtistActivated(Any payload) {
        try {
            ArtistActivatedEventOuterClass.ArtistActivatedEvent event = payload.unpack(ArtistActivatedEventOuterClass.ArtistActivatedEvent.class);
            artistWriteService.handleArtistActivated(event.getId());
        } catch (Exception e) {
            log.error("Failed to unpack ArtistActivatedEvent: {}", e.getMessage(), e);
        }
    }

    private void onAlbumActivated(Any payload) {
        try {
            AlbumActivatedEventOuterClass.AlbumActivatedEvent event = payload.unpack(AlbumActivatedEventOuterClass.AlbumActivatedEvent.class);
            albumWriteService.handleAlbumActivated(event.getId());
        } catch (Exception e) {
            log.error("Failed to unpack AlbumActivatedEvent: {}", e.getMessage(), e);
        }
    }

    private void onTrackActivated(Any payload) {
        try {
            TrackActivatedEventOuterClass.TrackActivatedEvent event = payload.unpack(TrackActivatedEventOuterClass.TrackActivatedEvent.class);
            trackWriteService.handleTrackActivated(event.getId());
        } catch (Exception e) {
            log.error("Failed to unpack TrackActivatedEvent: {}", e.getMessage(), e);
        }
    }

    private void onArtistTrueDeleted(Any payload) {
        try {
            ArtistTrueDeletedEventOuterClass.ArtistTrueDeletedEvent event = payload.unpack(ArtistTrueDeletedEventOuterClass.ArtistTrueDeletedEvent.class);
            artistWriteService.handleArtistTrueDeleted(event.getId());
        } catch (Exception e) {
            log.error("Failed to unpack ArtistTrueDeletedEvent: {}", e.getMessage(), e);
        }
    }

    private void onAlbumTrueDeleted(Any payload) {
        try {
            AlbumTrueDeletedEventOuterClass.AlbumTrueDeletedEvent event = payload.unpack(AlbumTrueDeletedEventOuterClass.AlbumTrueDeletedEvent.class);
            albumWriteService.handleAlbumTrueDeleted(event.getId());
        } catch (Exception e) {
            log.error("Failed to unpack AlbumTrueDeletedEvent: {}", e.getMessage(), e);
        }
    }

    private void onTrackTrueDeleted(Any payload) {
        try {
            TrackTrueDeletedEventOuterClass.TrackTrueDeletedEvent event = payload.unpack(TrackTrueDeletedEventOuterClass.TrackTrueDeletedEvent.class);
            trackWriteService.handleTrackTrueDeleted(event.getId());
        } catch (Exception e) {
            log.error("Failed to unpack TrackTrueDeletedEvent: {}", e.getMessage(), e);
        }
    }
}
