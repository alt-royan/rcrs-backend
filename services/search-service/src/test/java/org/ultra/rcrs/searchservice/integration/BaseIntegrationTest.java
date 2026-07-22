package org.ultra.rcrs.searchservice.integration;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.ExistsRequest;
import co.elastic.clients.elasticsearch.core.GetRequest;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import com.google.protobuf.Any;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.Timestamp;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.ultra.rcrs.events.common.DomainEventOuterClass;
import org.ultra.rcrs.kafka.Topics;

import java.io.StringReader;
import java.util.List;
import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, topics = {
        Topics.CATALOG_CDC_TOPIC,
        Topics.SEARCH_INDEX_TOPIC
})
@DirtiesContext
public abstract class BaseIntegrationTest {

    @Container
    static ElasticsearchContainer elasticsearch =
            new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:9.4.4")
                    .withEnv("xpack.security.enabled", "false")
                    .withEnv("xpack.security.http.ssl.enabled", "false");

    @BeforeEach
    void createIndices() throws Exception {
        var resolver = new PathMatchingResourcePatternResolver(getClass().getClassLoader());
        var resources = resolver.getResources("classpath:index/*-settings.json");
        for (var resource : resources) {
            var indexName = resource.getFilename().replace("-settings.json", "");
            try (var is = resource.getInputStream()) {
                var json = new String(is.readAllBytes());
                if (client.indices().exists(r -> r.index(indexName)).value()) {
                    client.indices().delete(r -> r.index(indexName));
                }
                client.indices().create(r -> r.index(indexName).withJson(new StringReader(json)));
            }
        }
    }

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add(
                "spring.elasticsearch.uris",
                elasticsearch::getHttpHostAddress
        );
        registry.add(
                "spring.kafka.bootstrap-servers",
                () -> System.getProperty("spring.embedded.kafka.brokers")
        );
    }

    @Autowired
    protected ElasticsearchClient client;

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected KafkaTemplate<String, byte[]> kafkaTemplate;

    private void indexDoc(String index, String id, Map<String, Object> body) {
        try {
            client.index(IndexRequest.of(r -> r
                    .index(index)
                    .id(id)
                    .document(body)
            ));
        } catch (Exception e) {
            throw new RuntimeException("Failed to index doc into " + index, e);
        }
    }

    protected void indexArtistPublicDoc(String id, String name, List<String> tags,
                                        String availability, List<Map<String, String>> albums,
                                        List<Map<String, String>> tracks) {
        var body = new java.util.HashMap<String, Object>();
        body.put("name", name);
        body.put("tags", tags);
        body.put("availability", availability);
        if (albums != null) body.put("albums", albums);
        if (tracks != null) body.put("tracks", tracks);
        indexDoc("artists-public", id, body);
    }

    protected void indexAlbumPublicDoc(String id, String title, String year, String availability,
                                       List<Map<String, String>> artists,
                                       List<Map<String, String>> tracks) {
        var body = new java.util.HashMap<String, Object>();
        body.put("title", title);
        body.put("year", year);
        body.put("availability", availability);
        if (artists != null) body.put("artists", artists);
        if (tracks != null) body.put("tracks", tracks);
        indexDoc("albums-public", id, body);
    }

    protected void indexTrackPublicDoc(String id, String title, String availability,
                                       List<Map<String, String>> artists,
                                       Map<String, String> album) {
        var body = new java.util.HashMap<String, Object>();
        body.put("title", title);
        body.put("availability", availability);
        if (artists != null) body.put("artists", artists);
        if (album != null) body.put("album", album);
        indexDoc("tracks-public", id, body);
    }

    protected void indexArtistAdminDoc(String id, String name, List<String> tags,
                                       String availability, String lifecycleStatus,
                                       List<Map<String, String>> albums,
                                       List<Map<String, String>> tracks) {
        var body = new java.util.HashMap<String, Object>();
        body.put("name", name);
        body.put("tags", tags);
        body.put("availability", availability);
        if (albums != null) body.put("albums", albums);
        if (tracks != null) body.put("tracks", tracks);
        indexDoc("artists-admin", id, body);
    }

    protected void indexAlbumAdminDoc(String id, String title, String year, String availability,
                                      String lifecycleStatus,
                                      List<Map<String, String>> artists,
                                      List<Map<String, String>> tracks) {
        var body = new java.util.HashMap<String, Object>();
        body.put("title", title);
        body.put("year", year);
        body.put("availability", availability);
        body.put("lifecycleStatus", lifecycleStatus);
        if (artists != null) body.put("artists", artists);
        if (tracks != null) body.put("tracks", tracks);
        indexDoc("albums-admin", id, body);
    }

    protected void indexTrackAdminDoc(String id, String title, String availability, String lifecycleStatus,
                                      List<Map<String, String>> artists,
                                      Map<String, String> album) {
        var body = new java.util.HashMap<String, Object>();
        body.put("title", title);
        body.put("availability", availability);
        body.put("lifecycleStatus", lifecycleStatus);
        if (artists != null) body.put("artists", artists);
        if (album != null) body.put("album", album);
        indexDoc("tracks-admin", id, body);
    }

    protected void refreshAllIndices() {
        try {
            client.indices().refresh();
            Thread.sleep(1000);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected boolean esDocExists(String index, String id) {
        try {
            return client.exists(ExistsRequest.of(r -> r.index(index).id(id))).value();
        } catch (Exception e) {
            throw new RuntimeException("Failed to check doc existence", e);
        }
    }

    protected Map<String, Object> getEsDoc(String index, String id) {
        try {
            GetResponse<Map> response = client.get(GetRequest.of(r -> r.index(index).id(id)), Map.class);
            if (!response.found()) {
                return null;
            }
            return response.source();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get doc from " + index, e);
        }
    }

    protected void sendEvent(DomainEventOuterClass.EventType eventType,
                             DomainEventOuterClass.AggregateType aggregateType,
                             String aggregateId,
                             GeneratedMessage payload) throws Exception {
        Timestamp ts = Timestamp.newBuilder()
                .setSeconds(System.currentTimeMillis() / 1000)
                .build();

        DomainEventOuterClass.DomainEvent event =
                DomainEventOuterClass.DomainEvent.newBuilder()
                        .setEventId(java.util.UUID.randomUUID().toString())
                        .setEventType(eventType)
                        .setAggregateType(aggregateType)
                        .setAggregateId(aggregateId)
                        .setOccurredAt(ts)
                        .setProducer("search-service-test")
                        .setPayload(Any.pack(payload))
                        .build();

        kafkaTemplate.send("search.index.topic", event.toByteArray()).get();
    }

    protected void waitForProcessing() throws Exception {
        Thread.sleep(3000);
    }

    protected static Map<String, String> nested(String id, String name) {
        return Map.of("id", id, "name", name);
    }

    protected static Map<String, String> nestedAlbum(String id, String title) {
        return Map.of("id", id, "title", title);
    }

    protected static Map<String, String> nestedTrack(String id, String title) {
        return Map.of("id", id, "title", title);
    }
}
