package org.ultra.rcrs.metadata.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.ultra.rcrs.enums.AlbumType;
import org.ultra.rcrs.enums.ArtistRole;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.enums.LifecycleStatus;
import org.ultra.rcrs.events.album.AlbumCreatedEventOuterClass;
import org.ultra.rcrs.events.artist.ArtistCreatedEventOuterClass;
import org.ultra.rcrs.events.common.*;
import org.ultra.rcrs.events.track.ArtistAddedToTrackEventOuterClass;
import org.ultra.rcrs.events.track.TrackCreatedEventOuterClass;
import org.ultra.rcrs.metadata.config.MongoReactiveConfig;
import org.ultra.rcrs.metadata.model.AlbumPublicDocument;
import org.ultra.rcrs.metadata.model.ArtistPublicDocument;
import org.ultra.rcrs.metadata.model.TrackPublicDocument;
import org.ultra.rcrs.metadata.repository.AlbumDocumentRepository;
import org.ultra.rcrs.metadata.repository.ArtistDocumentRepository;
import org.ultra.rcrs.metadata.repository.TrackDocumentRepository;
import org.ultra.rcrs.metadata.service.write.AlbumWriteService;
import org.ultra.rcrs.metadata.service.write.ArtistWriteService;
import org.ultra.rcrs.metadata.service.write.TrackWriteService;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ComponentScan(excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = MongoReactiveConfig.class
))
@Testcontainers
@ActiveProfiles("test")
class CdcEventIntegrationTest {

    @Container
    static MongoDBContainer mongo = new MongoDBContainer("mongo:7");

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongo::getConnectionString);
    }

    @Autowired private ArtistWriteService artistWriteService;
    @Autowired private AlbumWriteService albumWriteService;
    @Autowired private TrackWriteService trackWriteService;
    @Autowired private ArtistDocumentRepository artistDocRepository;
    @Autowired private AlbumDocumentRepository albumDocRepository;
    @Autowired private TrackDocumentRepository trackDocRepository;

    @Test
    void artistCreated_upsertsArtistDocument() {
        ArtistCreatedEventOuterClass.ArtistCreatedEvent event =
                ArtistCreatedEventOuterClass.ArtistCreatedEvent.newBuilder()
                        .setId("artist-001")
                        .setName("CDC Artist")
                        .setAvatarS3Key("avatar.jpg")
                        .setAvailabilityStatus(AvailabilityStatusOuterClass.AvailabilityStatus.ACTIVE)
                        .addTags("rock")
                        .addTags("indie")
                        .addSocialLinks(SocialLinkOuterClass.SocialLink.newBuilder()
                                .setResourceName("twitter")
                                .setUrl("https://twitter.com/test")
                                .build())
                        .build();

        artistWriteService.handleArtistCreated(event);

        ArtistPublicDocument doc = artistDocRepository.findById("artist-001").block();
        assertThat(doc).isNotNull();
        assertThat(doc.getName()).isEqualTo("CDC Artist");
        assertThat(doc.getAvatarS3Key()).isEqualTo("avatar.jpg");
        assertThat(doc.getAvailabilityStatus()).isEqualTo(EntityStatus.ACTIVE);
        assertThat(doc.getTags()).containsExactly("rock", "indie");
        assertThat(doc.getSocialLinks()).hasSize(1);
        assertThat(doc.getSocialLinks().get(0).getResourceName()).isEqualTo("twitter");
    }

    @Test
    void artistDeleted_setsAvailabilityStatusToDeleted() {
        ArtistCreatedEventOuterClass.ArtistCreatedEvent createEvent =
                ArtistCreatedEventOuterClass.ArtistCreatedEvent.newBuilder()
                        .setId("artist-002")
                        .setName("Delete Me")
                        .setAvailabilityStatus(AvailabilityStatusOuterClass.AvailabilityStatus.ACTIVE)
                        .build();
        artistWriteService.handleArtistCreated(createEvent);

        artistWriteService.handleArtistDeleted("artist-002");

        ArtistPublicDocument doc = artistDocRepository.findById("artist-002").block();
        assertThat(doc).isNotNull();
        assertThat(doc.getAvailabilityStatus()).isEqualTo(EntityStatus.DELETED);
    }

    @Test
    void artistHidden_setsAvailabilityStatusToHidden() {
        ArtistCreatedEventOuterClass.ArtistCreatedEvent createEvent =
                ArtistCreatedEventOuterClass.ArtistCreatedEvent.newBuilder()
                        .setId("artist-003")
                        .setName("Hide Me")
                        .setAvailabilityStatus(AvailabilityStatusOuterClass.AvailabilityStatus.ACTIVE)
                        .build();
        artistWriteService.handleArtistCreated(createEvent);

        artistWriteService.handleArtistHidden("artist-003");

        ArtistPublicDocument doc = artistDocRepository.findById("artist-003").block();
        assertThat(doc.getAvailabilityStatus()).isEqualTo(EntityStatus.HIDDEN);
    }

    @Test
    void artistActivated_restoresAvailabilityStatus() {
        ArtistCreatedEventOuterClass.ArtistCreatedEvent createEvent =
                ArtistCreatedEventOuterClass.ArtistCreatedEvent.newBuilder()
                        .setId("artist-004")
                        .setName("Reactivate Me")
                        .setAvailabilityStatus(AvailabilityStatusOuterClass.AvailabilityStatus.ACTIVE)
                        .build();
        artistWriteService.handleArtistCreated(createEvent);
        artistWriteService.handleArtistHidden("artist-004");

        artistWriteService.handleArtistActivated("artist-004");

        ArtistPublicDocument doc = artistDocRepository.findById("artist-004").block();
        assertThat(doc.getAvailabilityStatus()).isEqualTo(EntityStatus.ACTIVE);
    }

    @Test
    void artistTrueDeleted_removesDocument() {
        ArtistCreatedEventOuterClass.ArtistCreatedEvent createEvent =
                ArtistCreatedEventOuterClass.ArtistCreatedEvent.newBuilder()
                        .setId("artist-005")
                        .setName("Purge Me")
                        .setAvailabilityStatus(AvailabilityStatusOuterClass.AvailabilityStatus.ACTIVE)
                        .build();
        artistWriteService.handleArtistCreated(createEvent);

        artistWriteService.handleArtistTrueDeleted("artist-005");

        ArtistPublicDocument doc = artistDocRepository.findById("artist-005").block();
        assertThat(doc).isNull();
    }

    @Test
    void albumCreated_upsertsAlbumDocument() {
        AlbumCreatedEventOuterClass.AlbumCreatedEvent event =
                AlbumCreatedEventOuterClass.AlbumCreatedEvent.newBuilder()
                        .setId("album-001")
                        .setTitle("CDC Album")
                        .setType(AlbumTypeOuterClass.AlbumType.FULL)
                        .setCoverS3Key("cover.jpg")
                        .setAvailabilityStatus(AvailabilityStatusOuterClass.AvailabilityStatus.ACTIVE)
                        .setLifecycleStatus(LifecycleStatusOuterClass.LifecycleStatus.CREATED)
                        .build();

        albumWriteService.handleAlbumCreated(event);

        AlbumPublicDocument doc = albumDocRepository.findById("album-001").block();
        assertThat(doc).isNotNull();
        assertThat(doc.getTitle()).isEqualTo("CDC Album");
        assertThat(doc.getType()).isEqualTo(AlbumType.FULL);
        assertThat(doc.getLifecycleStatus()).isEqualTo(LifecycleStatus.CREATED);
        assertThat(doc.getAvailabilityStatus()).isEqualTo(EntityStatus.ACTIVE);
        assertThat(doc.getTotalTracks()).isZero();
        assertThat(doc.getTotalDurationMs()).isZero();
    }

    @Test
    void albumLifecycleStatusUpdated_updatesDocument() {
        AlbumCreatedEventOuterClass.AlbumCreatedEvent createEvent =
                AlbumCreatedEventOuterClass.AlbumCreatedEvent.newBuilder()
                        .setId("album-002")
                        .setTitle("Lifecycle Album")
                        .setType(AlbumTypeOuterClass.AlbumType.SINGLE)
                        .setAvailabilityStatus(AvailabilityStatusOuterClass.AvailabilityStatus.ACTIVE)
                        .setLifecycleStatus(LifecycleStatusOuterClass.LifecycleStatus.CREATED)
                        .build();
        albumWriteService.handleAlbumCreated(createEvent);

        AlbumUpdateLifecycleStatusEventOuterClass.AlbumUpdateLifecycleStatusEvent updateEvent =
                AlbumUpdateLifecycleStatusEventOuterClass.AlbumUpdateLifecycleStatusEvent.newBuilder()
                        .setId("album-002")
                        .setLifecycleStatus(LifecycleStatusOuterClass.LifecycleStatus.PUBLISHED)
                        .build();
        albumWriteService.handleAlbumLifecycleStatusUpdated(updateEvent);

        AlbumPublicDocument doc = albumDocRepository.findById("album-002").block();
        assertThat(doc.getLifecycleStatus()).isEqualTo(LifecycleStatus.PUBLISHED);
    }

    @Test
    void artistAddedToAlbum_appendsArtistEmbed() {
        ArtistCreatedEventOuterClass.ArtistCreatedEvent artistEvent =
                ArtistCreatedEventOuterClass.ArtistCreatedEvent.newBuilder()
                        .setId("artist-010")
                        .setName("Album Artist")
                        .setAvailabilityStatus(AvailabilityStatusOuterClass.AvailabilityStatus.ACTIVE)
                        .build();
        artistWriteService.handleArtistCreated(artistEvent);

        AlbumCreatedEventOuterClass.AlbumCreatedEvent albumEvent =
                AlbumCreatedEventOuterClass.AlbumCreatedEvent.newBuilder()
                        .setId("album-010")
                        .setTitle("Album With Artist")
                        .setType(AlbumTypeOuterClass.AlbumType.FULL)
                        .setAvailabilityStatus(AvailabilityStatusOuterClass.AvailabilityStatus.ACTIVE)
                        .setLifecycleStatus(LifecycleStatusOuterClass.LifecycleStatus.CREATED)
                        .build();
        albumWriteService.handleAlbumCreated(albumEvent);

        ArtistAddedToAlbumEventOuterClass.ArtistAddedToAlbumEvent linkEvent =
                ArtistAddedToAlbumEventOuterClass.ArtistAddedToAlbumEvent.newBuilder()
                        .setArtistId("artist-010")
                        .setAlbumId("album-010")
                        .setRole(ArtistRoleOuterClass.ArtistRole.MAIN_ARTIST)
                        .build();
        albumWriteService.handleArtistAddedToAlbum(linkEvent);

        AlbumPublicDocument doc = albumDocRepository.findById("album-010").block();
        assertThat(doc.getArtists()).isNotNull();
        assertThat(doc.getArtists()).hasSize(1);
        assertThat(doc.getArtists().get(0).getId()).isEqualTo("artist-010");
        assertThat(doc.getArtists().get(0).getName()).isEqualTo("Album Artist");
        assertThat(doc.getArtists().get(0).getRole()).isEqualTo(ArtistRole.MAIN_ARTIST);
    }

    @Test
    void trackCreated_upsertsTrackDocument() {
        TrackCreatedEventOuterClass.TrackCreatedEvent event =
                TrackCreatedEventOuterClass.TrackCreatedEvent.newBuilder()
                        .setId("track-001")
                        .setTitle("CDC Track")
                        .setTrackNumber(3)
                        .setExplicit(true)
                        .setAvailabilityStatus(AvailabilityStatusOuterClass.AvailabilityStatus.ACTIVE)
                        .setLifecycleStatus(LifecycleStatusOuterClass.LifecycleStatus.CREATED)
                        .build();

        trackWriteService.handleTrackCreated(event);

        TrackPublicDocument doc = trackDocRepository.findById("track-001").block();
        assertThat(doc).isNotNull();
        assertThat(doc.getTitle()).isEqualTo("CDC Track");
        assertThat(doc.getTrackNumber()).isEqualTo(3);
        assertThat(doc.getExplicit()).isTrue();
        assertThat(doc.getLifecycleStatus()).isEqualTo(LifecycleStatus.CREATED);
        assertThat(doc.getAvailabilityStatus()).isEqualTo(EntityStatus.ACTIVE);
    }

    @Test
    void trackAddedToAlbum_setsAlbumEmbed() {
        AlbumCreatedEventOuterClass.AlbumCreatedEvent albumEvent =
                AlbumCreatedEventOuterClass.AlbumCreatedEvent.newBuilder()
                        .setId("album-020")
                        .setTitle("Album For Track")
                        .setType(AlbumTypeOuterClass.AlbumType.FULL)
                        .setAvailabilityStatus(AvailabilityStatusOuterClass.AvailabilityStatus.ACTIVE)
                        .setLifecycleStatus(LifecycleStatusOuterClass.LifecycleStatus.CREATED)
                        .build();
        albumWriteService.handleAlbumCreated(albumEvent);

        TrackCreatedEventOuterClass.TrackCreatedEvent trackEvent =
                TrackCreatedEventOuterClass.TrackCreatedEvent.newBuilder()
                        .setId("track-020")
                        .setTitle("Track In Album")
                        .setTrackNumber(1)
                        .setExplicit(false)
                        .setAvailabilityStatus(AvailabilityStatusOuterClass.AvailabilityStatus.ACTIVE)
                        .setLifecycleStatus(LifecycleStatusOuterClass.LifecycleStatus.CREATED)
                        .build();
        trackWriteService.handleTrackCreated(trackEvent);

        TrackAddedToAlbumEventOuterClass.TrackAddedToAlbumEvent linkEvent =
                TrackAddedToAlbumEventOuterClass.TrackAddedToAlbumEvent.newBuilder()
                        .setTrackId("track-020")
                        .setAlbumId("album-020")
                        .build();
        trackWriteService.handleTrackAddedToAlbum(linkEvent);

        TrackPublicDocument trackDoc = trackDocRepository.findById("track-020").block();
        assertThat(trackDoc.getAlbum()).isNotNull();
        assertThat(trackDoc.getAlbum().getId()).isEqualTo("album-020");
        assertThat(trackDoc.getAlbum().getTitle()).isEqualTo("Album For Track");

        AlbumPublicDocument albumDoc = albumDocRepository.findById("album-020").block();
        assertThat(albumDoc.getTotalTracks()).isEqualTo(1);
    }

    @Test
    void artistAddedToTrack_appendsArtistEmbed() {
        ArtistCreatedEventOuterClass.ArtistCreatedEvent artistEvent =
                ArtistCreatedEventOuterClass.ArtistCreatedEvent.newBuilder()
                        .setId("artist-030")
                        .setName("Track Artist")
                        .setAvailabilityStatus(AvailabilityStatusOuterClass.AvailabilityStatus.ACTIVE)
                        .build();
        artistWriteService.handleArtistCreated(artistEvent);

        AlbumCreatedEventOuterClass.AlbumCreatedEvent albumEvent =
                AlbumCreatedEventOuterClass.AlbumCreatedEvent.newBuilder()
                        .setId("album-030")
                        .setTitle("Parent")
                        .setType(AlbumTypeOuterClass.AlbumType.SINGLE)
                        .setAvailabilityStatus(AvailabilityStatusOuterClass.AvailabilityStatus.ACTIVE)
                        .setLifecycleStatus(LifecycleStatusOuterClass.LifecycleStatus.CREATED)
                        .build();
        albumWriteService.handleAlbumCreated(albumEvent);

        TrackCreatedEventOuterClass.TrackCreatedEvent trackEvent =
                TrackCreatedEventOuterClass.TrackCreatedEvent.newBuilder()
                        .setId("track-030")
                        .setTitle("Track With Artist")
                        .setTrackNumber(1)
                        .setExplicit(false)
                        .setAvailabilityStatus(AvailabilityStatusOuterClass.AvailabilityStatus.ACTIVE)
                        .setLifecycleStatus(LifecycleStatusOuterClass.LifecycleStatus.CREATED)
                        .build();
        trackWriteService.handleTrackCreated(trackEvent);

        ArtistAddedToTrackEventOuterClass.ArtistAddedToTrackEvent linkEvent =
                ArtistAddedToTrackEventOuterClass.ArtistAddedToTrackEvent.newBuilder()
                        .setArtistId("artist-030")
                        .setTrackId("track-030")
                        .setRole(ArtistRoleOuterClass.ArtistRole.FEATURED_ARTIST)
                        .build();
        trackWriteService.handleArtistAddedToTrack(linkEvent);

        TrackPublicDocument doc = trackDocRepository.findById("track-030").block();
        assertThat(doc.getArtists()).isNotNull();
        assertThat(doc.getArtists()).hasSize(1);
        assertThat(doc.getArtists().get(0).getId()).isEqualTo("artist-030");
        assertThat(doc.getArtists().get(0).getRole()).isEqualTo(ArtistRole.FEATURED_ARTIST);
    }
}
