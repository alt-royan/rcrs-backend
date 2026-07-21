package org.ultra.rcrs.metadata.integration;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.ultra.rcrs.enums.AlbumType;
import org.ultra.rcrs.enums.ArtistRole;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.enums.LifecycleStatus;
import org.ultra.rcrs.events.common.DomainEventOuterClass;
import org.ultra.rcrs.metadata.dto.ArtistDto;
import org.ultra.rcrs.metadata.dto.request.AlbumUploadRequest;
import org.ultra.rcrs.metadata.dto.request.ArtistCreateRequest;
import org.ultra.rcrs.metadata.dto.request.TrackUploadRequest;
import org.ultra.rcrs.metadata.model.Album;
import org.ultra.rcrs.metadata.model.Artist;
import org.ultra.rcrs.metadata.model.Track;
import org.ultra.rcrs.metadata.repository.AlbumRepository;
import org.ultra.rcrs.metadata.repository.ArtistRepository;
import org.ultra.rcrs.metadata.repository.TrackRepository;
import org.ultra.rcrs.metadata.service.AlbumService;
import org.ultra.rcrs.metadata.service.ArtistService;
import org.ultra.rcrs.metadata.service.TrackService;
import org.ultra.rcrs.utils.Url62;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EmbeddedKafka(
        topics = {"catalog.cdc.topic", "search.index.topic", "catalog.update.status.topic", "global.dlq"},
        partitions = 1
)
@Testcontainers
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.application.name=test-service"
})
class KafkaEmissionIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("postgres")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("db/changelog/init.sql");

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> postgres.getJdbcUrl() + "&currentSchema=rcrs_catalog");
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.liquibase.url", postgres::getJdbcUrl);
        registry.add("spring.liquibase.user", postgres::getUsername);
        registry.add("spring.liquibase.password", postgres::getPassword);
        registry.add("spring.liquibase.enabled", () -> "false");
    }

    @Autowired
    private ArtistService artistService;
    @Autowired
    private AlbumService albumService;
    @Autowired
    private TrackService trackService;
    @Autowired
    private ArtistRepository artistRepository;
    @Autowired
    private AlbumRepository albumRepository;
    @Autowired
    private TrackRepository trackRepository;

    @Value("${spring.embedded.kafka.brokers}")
    private String brokers;

    private Consumer<String, byte[]> consumer;
    private UUID testAlbumId;

    @BeforeEach
    void setUp() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-cdc-" + UUID.randomUUID());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, ByteArrayDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        DefaultKafkaConsumerFactory<String, byte[]> cf = new DefaultKafkaConsumerFactory<>(props);
        consumer = cf.createConsumer();
        consumer.subscribe(List.of("catalog.cdc.topic"));

        AlbumUploadRequest albumReq = new AlbumUploadRequest();
        albumReq.setTitle("Test Album");
        albumReq.setType(AlbumType.FULL);
        albumReq.setReleaseDate(LocalDateTime.of(2026, 6, 1, 0, 0));
        testAlbumId = albumService.createAlbum(albumReq);

        consumer.poll(java.time.Duration.ofMillis(500));
    }

    @AfterEach
    void tearDown() {
        if (consumer != null) {
            consumer.close();
        }
    }

    @Test
    void createArtist_savesToDbAndEmitsArtistCreatedEvent() throws Exception {
        ArtistCreateRequest request = new ArtistCreateRequest();
        request.setName("Kafka Test Artist");
        request.setTags(List.of("rock"));

        UUID artistId = artistService.createArtist(request);

        Optional<Artist> saved = artistRepository.findById(artistId);
        assertThat(saved).isPresent();
        assertThat(saved.get().getName()).isEqualTo("Kafka Test Artist");

        ConsumerRecord<String, byte[]> record =
                KafkaTestUtils.getSingleRecord(consumer, "catalog.cdc.topic");

        DomainEventOuterClass.DomainEvent event =
                DomainEventOuterClass.DomainEvent.parseFrom(record.value());
        assertThat(event.getEventType())
                .isEqualTo(DomainEventOuterClass.EventType.ARTIST_CREATED);
        assertThat(event.getAggregateType())
                .isEqualTo(DomainEventOuterClass.AggregateType.ARTIST);
        assertThat(event.getProducer()).isEqualTo("test-service");
    }

    @Test
    void markArtistDelete_cascadesAndEmitsArtistDeletedEvent() throws Exception {
        ArtistCreateRequest artistReq = new ArtistCreateRequest();
        artistReq.setName("Delete Test Artist");
        artistReq.setTags(List.of("pop"));
        UUID artistId = artistService.createArtist(artistReq);

        AlbumUploadRequest albumReq = new AlbumUploadRequest();
        albumReq.setTitle("Delete Test Album");
        albumReq.setType(AlbumType.FULL);
        albumReq.setReleaseDate(LocalDateTime.now());
        UUID albumId = albumService.createAlbum(albumReq);

        TrackUploadRequest trackReq = new TrackUploadRequest();
        trackReq.setAlbumId(Url62.encode(albumId));
        trackReq.setTitle("Delete Test Track");
        trackReq.setTrackNumber(1);
        trackReq.setExplicit(false);
        UUID trackId = trackService.createTrack(trackReq);

        var aDto = new ArtistDto();
        aDto.setId(Url62.encode(artistId));
        aDto.setRole(ArtistRole.MAIN_ARTIST);
        albumService.addAllArtistToAlbum(List.of(aDto), albumId);
        trackService.addAllArtistToTrack(List.of(aDto), trackId);

        artistService.markArtistDelete(artistId);

        assertThat(artistRepository.findById(artistId).orElseThrow().getAvailabilityStatus()).isEqualTo(EntityStatus.DELETED);
        assertThat(albumRepository.findById(albumId).orElseThrow().getAvailabilityStatus()).isEqualTo(EntityStatus.DELETED);
        assertThat(trackRepository.findById(trackId).orElseThrow().getAvailabilityStatus()).isEqualTo(EntityStatus.DELETED);

        List<DomainEventOuterClass.DomainEvent> events = drainCdcEvents(3);
        List<DomainEventOuterClass.EventType> eventTypes = events.stream()
                .map(DomainEventOuterClass.DomainEvent::getEventType)
                .toList();
        assertThat(eventTypes).contains(DomainEventOuterClass.EventType.ARTIST_DELETED);
        assertThat(eventTypes).contains(DomainEventOuterClass.EventType.ALBUM_DELETED);
        assertThat(eventTypes).contains(DomainEventOuterClass.EventType.TRACK_DELETED);
    }

    @Test
    void hideArtist_cascadesAndEmitsHideEvent() throws Exception {
        ArtistCreateRequest artistReq = new ArtistCreateRequest();
        artistReq.setName("Hide Test Artist");
        artistReq.setTags(List.of("pop"));
        UUID artistId = artistService.createArtist(artistReq);

        AlbumUploadRequest albumReq = new AlbumUploadRequest();
        albumReq.setTitle("Hide Test Album");
        albumReq.setType(AlbumType.FULL);
        albumReq.setReleaseDate(LocalDateTime.now());
        UUID albumId = albumService.createAlbum(albumReq);

        TrackUploadRequest trackReq = new TrackUploadRequest();
        trackReq.setAlbumId(Url62.encode(albumId));
        trackReq.setTitle("Hide Test Track");
        trackReq.setTrackNumber(1);
        trackReq.setExplicit(false);
        UUID trackId = trackService.createTrack(trackReq);

        var aDto = new ArtistDto();
        aDto.setId(Url62.encode(artistId));
        aDto.setRole(ArtistRole.MAIN_ARTIST);
        albumService.addAllArtistToAlbum(List.of(aDto), albumId);
        trackService.addAllArtistToTrack(List.of(aDto), trackId);

        artistService.hideArtist(artistId);

        assertThat(artistRepository.findById(artistId).orElseThrow().getAvailabilityStatus()).isEqualTo(EntityStatus.HIDDEN);
        assertThat(albumRepository.findById(albumId).orElseThrow().getAvailabilityStatus()).isEqualTo(EntityStatus.HIDDEN);
        assertThat(trackRepository.findById(trackId).orElseThrow().getAvailabilityStatus()).isEqualTo(EntityStatus.HIDDEN);

        List<DomainEventOuterClass.DomainEvent> events = drainCdcEvents(3);
        List<DomainEventOuterClass.EventType> eventTypes = events.stream()
                .map(DomainEventOuterClass.DomainEvent::getEventType)
                .toList();
        assertThat(eventTypes).contains(DomainEventOuterClass.EventType.ARTIST_HIDDEN);
        assertThat(eventTypes).contains(DomainEventOuterClass.EventType.ALBUM_HIDDEN);
        assertThat(eventTypes).contains(DomainEventOuterClass.EventType.TRACK_HIDDEN);
    }

    @Test
    void createTrack_emitsTrackCreatedAndTrackAddedToAlbumEvents() throws Exception {
        TrackUploadRequest trackReq = new TrackUploadRequest();
        trackReq.setAlbumId(Url62.encode(testAlbumId));
        trackReq.setTitle("Kafka Test Track");
        trackReq.setTrackNumber(1);
        trackReq.setExplicit(true);

        UUID trackId = trackService.createTrack(trackReq);

        Optional<Track> saved = trackRepository.findById(trackId);
        assertThat(saved).isPresent();
        assertThat(saved.get().getTitle()).isEqualTo("Kafka Test Track");
        assertThat(saved.get().getExplicit()).isTrue();
        assertThat(saved.get().getAlbumId()).isEqualTo(testAlbumId);

        List<DomainEventOuterClass.DomainEvent> events = drainCdcEvents(2);
        List<DomainEventOuterClass.EventType> eventTypes = events.stream()
                .map(DomainEventOuterClass.DomainEvent::getEventType)
                .toList();
        assertThat(eventTypes).contains(
                DomainEventOuterClass.EventType.TRACK_CREATED,
                DomainEventOuterClass.EventType.TRACK_ADDED_TO_ALBUM
        );
    }

    @Test
    void updateAlbumLifecycleStatus_emitsEvent() throws Exception {
        albumService.updateLifecycleStatus(LifecycleStatus.TRANSCODING, testAlbumId);

        Album album = albumRepository.findById(testAlbumId).orElseThrow();
        assertThat(album.getLifecycleStatus()).isEqualTo(LifecycleStatus.TRANSCODING);

        ConsumerRecord<String, byte[]> record =
                KafkaTestUtils.getSingleRecord(consumer, "catalog.cdc.topic");

        DomainEventOuterClass.DomainEvent event =
                DomainEventOuterClass.DomainEvent.parseFrom(record.value());
        assertThat(event.getEventType())
                .isEqualTo(DomainEventOuterClass.EventType.ALBUM_LIFECYCLE_STATUS_UPDATED);
    }

    private List<DomainEventOuterClass.DomainEvent> drainCdcEvents(int expectedCount) throws Exception {
        List<DomainEventOuterClass.DomainEvent> events = new ArrayList<>();
        long deadline = System.currentTimeMillis() + 10_000;
        while (events.size() < expectedCount && System.currentTimeMillis() < deadline) {
            ConsumerRecords<String, byte[]> records =
                    consumer.poll(java.time.Duration.ofMillis(200));
            for (ConsumerRecord<String, byte[]> record : records) {
                events.add(DomainEventOuterClass.DomainEvent.parseFrom(record.value()));
            }
        }
        return events;
    }
}
