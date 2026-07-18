package org.ultra.rcrs.catalogservice.kafka.listener;

import com.google.protobuf.Any;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.ultra.rcrs.events.album.AlbumCreatedEventOuterClass;
import org.ultra.rcrs.events.album.AlbumDeletedEventOuterClass;
import org.ultra.rcrs.events.album.AlbumHiddenEventOuterClass;
import org.ultra.rcrs.events.album.AlbumUpdateLifecycleStatusEventOuterClass;
import org.ultra.rcrs.events.album.ArtistAddedToAlbumEventOuterClass;
import org.ultra.rcrs.events.album.ArtistDeletedFromAlbumEventOuterClass;
import org.ultra.rcrs.events.artist.ArtistCreatedEventOuterClass;
import org.ultra.rcrs.events.artist.ArtistDeletedEventOuterClass;
import org.ultra.rcrs.events.artist.ArtistHiddenEventOuterClass;
import org.ultra.rcrs.events.common.DomainEventOuterClass;
import org.ultra.rcrs.events.track.ArtistAddedToTrackEventOuterClass;
import org.ultra.rcrs.events.track.ArtistDeletedFromTrackEventOuterClass;
import org.ultra.rcrs.events.track.OtherAddedToTrackEventOuterClass;
import org.ultra.rcrs.events.track.OtherDeletedFromTrackEventOuterClass;
import org.ultra.rcrs.events.track.TrackCreatedEventOuterClass;
import org.ultra.rcrs.events.track.TrackDeletedEventOuterClass;
import org.ultra.rcrs.events.track.TrackHiddenEventOuterClass;
import org.ultra.rcrs.events.track.TrackAddedToAlbumEventOuterClass;
import org.ultra.rcrs.events.track.TrackUpdateLifecycleStatusEventOuterClass;
import org.ultra.rcrs.kafka.Topics;
import org.ultra.rcrs.catalogservice.service.write.AlbumWriteService;
import org.ultra.rcrs.catalogservice.service.write.ArtistWriteService;
import org.ultra.rcrs.catalogservice.service.write.TrackWriteService;

@Component
@Slf4j
@RequiredArgsConstructor
public class CdcEventListener {

    private final ArtistWriteService artistWriteService;
    private final AlbumWriteService albumWriteService;
    private final TrackWriteService trackWriteService;

    @KafkaListener(topics = Topics.CATALOG_CDC_TOPIC, groupId = "catalog-read-group")
    public void handleCdcEvent(byte[] message) {
        try {
            DomainEventOuterClass.DomainEvent event = DomainEventOuterClass.DomainEvent.parseFrom(message);
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
                default -> log.warn("Unknown event type: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("Failed to process CDC event: {}", e.getMessage(), e);
        }
    }

    private void onArtistCreated(Any payload) {
        try {
            ArtistCreatedEventOuterClass.ArtistCreatedEvent event = payload.unpack(ArtistCreatedEventOuterClass.ArtistCreatedEvent.class);
            artistWriteService.handleArtistCreated(event).subscribe();
        } catch (Exception e) {
            log.error("Failed to unpack ArtistCreatedEvent: {}", e.getMessage(), e);
        }
    }

    private void onArtistDeleted(Any payload) {
        try {
            ArtistDeletedEventOuterClass.ArtistDeletedEvent event = payload.unpack(ArtistDeletedEventOuterClass.ArtistDeletedEvent.class);
            artistWriteService.handleArtistDeleted(event.getId()).subscribe();
        } catch (Exception e) {
            log.error("Failed to unpack ArtistDeletedEvent: {}", e.getMessage(), e);
        }
    }

    private void onArtistHidden(Any payload) {
        try {
            ArtistHiddenEventOuterClass.ArtistHiddenEvent event = payload.unpack(ArtistHiddenEventOuterClass.ArtistHiddenEvent.class);
            artistWriteService.handleArtistHidden(event.getId()).subscribe();
        } catch (Exception e) {
            log.error("Failed to unpack ArtistHiddenEvent: {}", e.getMessage(), e);
        }
    }

    private void onAlbumCreated(Any payload) {
        try {
            AlbumCreatedEventOuterClass.AlbumCreatedEvent event = payload.unpack(AlbumCreatedEventOuterClass.AlbumCreatedEvent.class);
            albumWriteService.handleAlbumCreated(event).subscribe();
        } catch (Exception e) {
            log.error("Failed to unpack AlbumCreatedEvent: {}", e.getMessage(), e);
        }
    }

    private void onAlbumDeleted(Any payload) {
        try {
            AlbumDeletedEventOuterClass.AlbumDeletedEvent event = payload.unpack(AlbumDeletedEventOuterClass.AlbumDeletedEvent.class);
            albumWriteService.handleAlbumDeleted(event.getId()).subscribe();
        } catch (Exception e) {
            log.error("Failed to unpack AlbumDeletedEvent: {}", e.getMessage(), e);
        }
    }

    private void onAlbumHidden(Any payload) {
        try {
            AlbumHiddenEventOuterClass.AlbumHiddenEvent event = payload.unpack(AlbumHiddenEventOuterClass.AlbumHiddenEvent.class);
            albumWriteService.handleAlbumHidden(event.getId()).subscribe();
        } catch (Exception e) {
            log.error("Failed to unpack AlbumHiddenEvent: {}", e.getMessage(), e);
        }
    }

    private void onTrackCreated(Any payload) {
        try {
            TrackCreatedEventOuterClass.TrackCreatedEvent event = payload.unpack(TrackCreatedEventOuterClass.TrackCreatedEvent.class);
            trackWriteService.handleTrackCreated(event).subscribe();
        } catch (Exception e) {
            log.error("Failed to unpack TrackCreatedEvent: {}", e.getMessage(), e);
        }
    }

    private void onTrackDeleted(Any payload) {
        try {
            TrackDeletedEventOuterClass.TrackDeletedEvent event = payload.unpack(TrackDeletedEventOuterClass.TrackDeletedEvent.class);
            trackWriteService.handleTrackDeleted(event.getId()).subscribe();
        } catch (Exception e) {
            log.error("Failed to unpack TrackDeletedEvent: {}", e.getMessage(), e);
        }
    }

    private void onTrackHidden(Any payload) {
        try {
            TrackHiddenEventOuterClass.TrackHiddenEvent event = payload.unpack(TrackHiddenEventOuterClass.TrackHiddenEvent.class);
            trackWriteService.handleTrackHidden(event.getId()).subscribe();
        } catch (Exception e) {
            log.error("Failed to unpack TrackHiddenEvent: {}", e.getMessage(), e);
        }
    }

    private void onArtistAddedToTrack(Any payload) {
        try {
            ArtistAddedToTrackEventOuterClass.ArtistAddedToTrackEvent event = payload.unpack(ArtistAddedToTrackEventOuterClass.ArtistAddedToTrackEvent.class);
            trackWriteService.handleArtistAddedToTrack(event).subscribe();
        } catch (Exception e) {
            log.error("Failed to unpack ArtistAddedToTrackEvent: {}", e.getMessage(), e);
        }
    }

    private void onArtistAddedToAlbum(Any payload) {
        try {
            ArtistAddedToAlbumEventOuterClass.ArtistAddedToAlbumEvent event = payload.unpack(ArtistAddedToAlbumEventOuterClass.ArtistAddedToAlbumEvent.class);
            albumWriteService.handleArtistAddedToAlbum(event).subscribe();
        } catch (Exception e) {
            log.error("Failed to unpack ArtistAddedToAlbumEvent: {}", e.getMessage(), e);
        }
    }

    private void onOtherAddedToTrack(Any payload) {
        try {
            OtherAddedToTrackEventOuterClass.OtherAddedToTrackEvent event = payload.unpack(OtherAddedToTrackEventOuterClass.OtherAddedToTrackEvent.class);
            trackWriteService.handleOtherAddedToTrack(event).subscribe();
        } catch (Exception e) {
            log.error("Failed to unpack OtherAddedToTrackEvent: {}", e.getMessage(), e);
        }
    }

    private void onOtherDeletedFromTrack(Any payload) {
        try {
            OtherDeletedFromTrackEventOuterClass.OtherDeletedFromTrackEvent event = payload.unpack(OtherDeletedFromTrackEventOuterClass.OtherDeletedFromTrackEvent.class);
            trackWriteService.handleOtherDeletedFromTrack(event).subscribe();
        } catch (Exception e) {
            log.error("Failed to unpack OtherDeletedFromTrackEvent: {}", e.getMessage(), e);
        }
    }

    private void onArtistDeletedFromAlbum(Any payload) {
        try {
            ArtistDeletedFromAlbumEventOuterClass.ArtistDeletedFromAlbumEvent event = payload.unpack(ArtistDeletedFromAlbumEventOuterClass.ArtistDeletedFromAlbumEvent.class);
            albumWriteService.handleArtistDeletedFromAlbum(event).subscribe();
        } catch (Exception e) {
            log.error("Failed to unpack ArtistDeletedFromAlbumEvent: {}", e.getMessage(), e);
        }
    }

    private void onArtistDeletedFromTrack(Any payload) {
        try {
            ArtistDeletedFromTrackEventOuterClass.ArtistDeletedFromTrackEvent event = payload.unpack(ArtistDeletedFromTrackEventOuterClass.ArtistDeletedFromTrackEvent.class);
            trackWriteService.handleArtistDeletedFromTrack(event).subscribe();
        } catch (Exception e) {
            log.error("Failed to unpack ArtistDeletedFromTrackEvent: {}", e.getMessage(), e);
        }
    }

    private void onAlbumLifecycleStatusUpdated(Any payload) {
        try {
            AlbumUpdateLifecycleStatusEventOuterClass.AlbumUpdateLifecycleStatusEvent event = payload.unpack(AlbumUpdateLifecycleStatusEventOuterClass.AlbumUpdateLifecycleStatusEvent.class);
            albumWriteService.handleAlbumLifecycleStatusUpdated(event).subscribe();
        } catch (Exception e) {
            log.error("Failed to unpack AlbumUpdateLifecycleStatusEvent: {}", e.getMessage(), e);
        }
    }

    private void onTrackLifecycleStatusUpdated(Any payload) {
        try {
            TrackUpdateLifecycleStatusEventOuterClass.TrackUpdateLifecycleStatusEvent event = payload.unpack(TrackUpdateLifecycleStatusEventOuterClass.TrackUpdateLifecycleStatusEvent.class);
            trackWriteService.handleTrackLifecycleStatusUpdated(event).subscribe();
        } catch (Exception e) {
            log.error("Failed to unpack TrackUpdateLifecycleStatusEvent: {}", e.getMessage(), e);
        }
    }

    private void onTrackAddedToAlbum(Any payload) {
        try {
            TrackAddedToAlbumEventOuterClass.TrackAddedToAlbumEvent event = payload.unpack(TrackAddedToAlbumEventOuterClass.TrackAddedToAlbumEvent.class);
            trackWriteService.handleTrackAddedToAlbum(event).subscribe();
        } catch (Exception e) {
            log.error("Failed to unpack TrackAddedToAlbumEvent: {}", e.getMessage(), e);
        }
    }
}
