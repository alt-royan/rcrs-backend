package org.ultra.rcrs.metadata.integration;

import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.ultra.rcrs.enums.AlbumType;
import org.ultra.rcrs.enums.ArtistRole;
import org.ultra.rcrs.enums.EntityStatus;
import org.ultra.rcrs.enums.LifecycleStatus;
import org.ultra.rcrs.events.common.DomainEventOuterClass;
import org.ultra.rcrs.kafka.Topics;
import org.ultra.rcrs.metadata.model.*;
import org.ultra.rcrs.metadata.repository.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, topics = {
        Topics.CATALOG_CDC_TOPIC,
        Topics.SEARCH_INDEX_TOPIC,
        Topics.CATALOG_UPDATE_STATUS_TOPIC
})
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}"
})
@DirtiesContext
public abstract class BaseIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected EmbeddedKafkaBroker kafkaBroker;

    @Autowired
    protected ArtistRepository artistRepository;

    @Autowired
    protected AlbumRepository albumRepository;

    @Autowired
    protected TrackRepository trackRepository;

    @Autowired
    protected ArtistToAlbumRepository artistToAlbumRepository;

    @Autowired
    protected ArtistToTrackRepository artistToTrackRepository;

    @Autowired
    protected OtherArtistRepository otherArtistRepository;

    protected Consumer<String, byte[]> kafkaConsumer;

    @BeforeEach
    void setUpKafkaConsumer() {
        Map<String, Object> props = KafkaTestUtils.consumerProps(
                kafkaBroker, "test-group-" + UUID.randomUUID(), true);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);

        kafkaConsumer = new DefaultKafkaConsumerFactory<>(
                props, new StringDeserializer(), new ByteArrayDeserializer()
        ).createConsumer();
        kafkaConsumer.subscribe(List.of(Topics.CATALOG_CDC_TOPIC, Topics.SEARCH_INDEX_TOPIC));
    }

    @AfterEach
    void tearDownKafkaConsumer() {
        if (kafkaConsumer != null) {
            kafkaConsumer.close();
        }
    }

    protected record TopicEvent(String topic, DomainEventOuterClass.DomainEvent event) {}

    protected List<TopicEvent> drainEvents(int expectedCount) throws InvalidProtocolBufferException {
        List<TopicEvent> events = new ArrayList<>();
        long deadline = System.currentTimeMillis() + 10_000;
        while (events.size() < expectedCount && System.currentTimeMillis() < deadline) {
            ConsumerRecords<String, byte[]> records = kafkaConsumer.poll(Duration.ofMillis(500));
            for (ConsumerRecord<String, byte[]> record : records) {
                events.add(new TopicEvent(record.topic(),
                        DomainEventOuterClass.DomainEvent.parseFrom(record.value())));
            }
        }
        return events;
    }

    protected List<DomainEventOuterClass.DomainEvent> eventsOnTopic(List<TopicEvent> all, String topic) {
        return all.stream().filter(te -> te.topic().equals(topic)).map(TopicEvent::event).toList();
    }

    protected Artist createArtistInDb(String name, EntityStatus status) {
        return artistRepository.save(Artist.builder()
                .name(name)
                .availabilityStatus(status)
                .socialLinks(new SocialLinks(List.of()))
                .build());
    }

    protected Album createAlbumInDb(String title, EntityStatus status) {
        return albumRepository.save(Album.builder()
                .title(title)
                .type(AlbumType.FULL)
                .lifecycleStatus(LifecycleStatus.CREATED)
                .availabilityStatus(status)
                .build());
    }

    protected Track createTrackInDb(String title, EntityStatus status, UUID albumId) {
        return trackRepository.save(Track.builder()
                .title(title)
                .trackNumber(1)
                .explicit(false)
                .lifecycleStatus(LifecycleStatus.CREATED)
                .availabilityStatus(status)
                .albumId(albumId)
                .build());
    }

    protected void linkArtistToAlbum(UUID artistId, UUID albumId) {
        artistToAlbumRepository.save(ArtistToAlbum.builder()
                .artistId(artistId)
                .albumId(albumId)
                .artistRole(ArtistRole.MAIN_ARTIST)
                .build());
    }
}
